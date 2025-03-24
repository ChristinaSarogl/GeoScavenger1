package com.example.geoscavenger;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geoscavenger.functionalities.CheckSystemSpecs;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    public static final String TAG = "SignUpActivity";
    private static final String USER_COL = "users";
    private static final String PHOTO_KEY = "photoUrl";

    private TextInputLayout mUsernameError;
    private TextInputLayout mDOBError;
    private TextInputLayout mEmailError;
    private TextInputLayout mPasswordError;
    private TextInputLayout mRepeatPasswordError;
    private TextInputEditText mUsername;
    private TextInputEditText mDOB;
    private TextInputEditText mEmail;
    private TextInputEditText mPassword;
    private TextInputEditText mRepeatPassword;
    private Button mSignUp;
    private TextView mLogin;
    private DatePickerDialog mPickerDialog;
    private CheckSystemSpecs mCheckSystemSpecs;

    //Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore mFirebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle(R.string.signUp_str);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCheckSystemSpecs = new CheckSystemSpecs();

        mUsernameError = (TextInputLayout) findViewById(R.id.signup_username_error_message);
        mDOBError = (TextInputLayout) findViewById(R.id.signup_dob_error_message);
        mEmailError = (TextInputLayout) findViewById(R.id.signup_email_error_message);
        mPasswordError = (TextInputLayout) findViewById(R.id.signup_password_error_message);
        mRepeatPasswordError = (TextInputLayout) findViewById(R.id.signup_repeat_password_error_message);

        mUsername = (TextInputEditText) findViewById(R.id.signup_username);
        mDOB = (TextInputEditText) findViewById(R.id.signup_dob);
        mEmail = (TextInputEditText) findViewById(R.id.signup_email);
        mPassword = (TextInputEditText) findViewById(R.id.signup_password);
        mRepeatPassword = (TextInputEditText) findViewById(R.id.signup_repeat_password);

        mSignUp = (Button) findViewById(R.id.signup_btn);
        mLogin = (TextView) findViewById(R.id.signup_login);

        //Initialize Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        mUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mUsername.getText().toString().trim().length() > 0){
                    mUsernameError.setError(null);
                }
            }
        });

        mDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDOBError.setError(null);

                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                //Date picker dialog
                mPickerDialog = new DatePickerDialog(SignUpActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int PicYear, int PicMonth, int PicDay) {
                                mDOB.setText(PicDay + "/" + (PicMonth+1) + "/" + PicYear);
                            }
                        },year, month,day);

                mPickerDialog.setTitle(getString(R.string.birthday_str));
                mPickerDialog.show();
            }
        });

        mEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mEmail.getText().toString().trim().length() > 0){
                    mEmailError.setError(null);
                }

            }
        });

        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mPassword.getText().toString().trim().length() > 0){
                    mPasswordError.setError(null);
                }
            }
        });

        mRepeatPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mRepeatPassword.getText().toString().trim().length() > 0){
                    mRepeatPasswordError.setError(null);
                }

            }
        });

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check internet connection
                ConnectivityManager connectivityManager
                        = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (!mCheckSystemSpecs.checkNetworkConnection(connectivityManager, SignUpActivity.this)){
                    return;
                }

                //Check inputted data
                String username = mUsername.getText().toString().trim();
                String dob = mDOB.getText().toString();
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String repeatPassword = mRepeatPassword.getText().toString().trim();
                LocalDate birthday;

                if (username.isEmpty()){
                    mUsernameError.setError(getString(R.string.username_error_str));
                    return;
                }

                if(dob.isEmpty() || Pattern.matches("[a-zA-Z]+", dob)){
                    mDOBError.setError(getString(R.string.birthday_error_str));
                    return;
                } else {
                    //Check if the user is above 12 years old
                    String[] dobString = dob.split("/");
                    if(dobString.length < 3){
                        mDOBError.setError(getString(R.string.birthday_error_str));
                        return;
                    }
                    int day = Integer.valueOf(dobString[0]);
                    int month = Integer.valueOf(dobString[1]);
                    int year = Integer.valueOf(dobString[2]);

                    LocalDate now = LocalDate.now();
                    birthday = LocalDate.of(year, month, day);

                    Period period = Period.between(birthday, now);

                    if (period.getYears() < 12){
                        mDOBError.setError(getString(R.string.too_young_error_str));
                        return;
                    }

                }

                if (email.isEmpty()){
                    mEmailError.setError(getString(R.string.email_error_str));
                    return;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    mEmailError.setError(getString(R.string.invalid_email_str));
                    return;
                }

                if (password.isEmpty()){
                    mPasswordError.setError(getString(R.string.password_required_str));
                    return;
                }

                if (repeatPassword.isEmpty()) {
                    mRepeatPasswordError.setError(getString(R.string.repeat_password_empty_str));
                    return;

                } else if (!repeatPassword.matches(password)){
                    mRepeatPasswordError.setError(getString(R.string.mismatch_passwords_error_str));
                    return;
                }

                //Register user to firebase
                registerUser(username,dob,email,password);

            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void registerUser(String username, String dob, String email, String password){
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //Registration success
                        mFirebaseUser = mFirebaseAuth.getCurrentUser();

                        //Set user's display name
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build();
                        mFirebaseUser.updateProfile(profileUpdate);

                        DocumentReference userDoc = mFirebaseFirestore.collection(USER_COL)
                                .document(mFirebaseUser.getUid());
                        Map<String,Object> userInfo = new HashMap<>();
                        userInfo.put("username", username);
                        userInfo.put(PHOTO_KEY,null);
                        userInfo.put("dob",dob);
                        userInfo.put("email", mFirebaseUser.getEmail());
                        userInfo.put("joined_hunts",0);

                        userDoc.set(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d(TAG, "registerUser: Stored user information successfully.");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //If information is not saved, delete user
                                mFirebaseAuth.getCurrentUser().delete();
                                Log.d(TAG,"registerUser: Something went wrong with the database.");
                                Toast.makeText(SignUpActivity.this,getString(R.string.registration_error_str),
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                        });

                        Toast.makeText(SignUpActivity.this, "Account created!",
                                Toast.LENGTH_SHORT).show();
                        //Transfer to login activity
                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();

                    }
                }). addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Task wasn't successful
                Log.d(TAG, String.valueOf(e));
                if (e instanceof FirebaseAuthWeakPasswordException) {
                    mPasswordError.setError(getString(R.string.weak_password_str));
                } else if (e instanceof FirebaseAuthUserCollisionException) {
                    Toast.makeText(SignUpActivity.this, getString(R.string.registration_error_str),
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SignUpActivity.this, getString(R.string.unknown_error_str),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
