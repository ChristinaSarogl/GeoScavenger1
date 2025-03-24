package com.example.geoscavenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.geoscavenger.functionalities.CheckSystemSpecs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private TextView mError;
    private TextInputLayout mEmailError;
    private TextInputLayout mPasswordError;
    private TextInputEditText mEmail;
    private TextInputEditText mPassword;
    private TextView mForgotPassword;
    private Button mLogin;
    private TextView mSignup;

    //Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onStart() {
        super.onStart();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        //Check if the user has already logged-in in the past
        if(mFirebaseUser!=null){
            Intent enterApplication = new Intent(getApplicationContext(), MainActivity.class);
            enterApplication.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            enterApplication.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(enterApplication);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        getSupportActionBar().hide();

        CheckSystemSpecs checkSystemSpecs = new CheckSystemSpecs();

        mError = (TextView) findViewById(R.id.login_error_messages);
        mEmailError = (TextInputLayout) findViewById(R.id.login_email_error_message);
        mPasswordError = (TextInputLayout) findViewById(R.id.login_password_error_message);
        mEmail = (TextInputEditText) findViewById(R.id.login_email);
        mPassword = (TextInputEditText) findViewById(R.id.login_password);
        mForgotPassword = (TextView) findViewById(R.id.login_forgot_password);
        mLogin = (Button) findViewById(R.id.login_btn);
        mSignup = (TextView) findViewById(R.id.login_signup);

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

        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);

            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (!checkSystemSpecs.checkNetworkConnection(connectivityManager, getApplicationContext())){
                    return;
                }

                //Check inputted data
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if (email.isEmpty()){
                    mEmailError.setError(getString(R.string.empty_email_str));
                    return;
                }
                if (password.isEmpty()){
                    mPasswordError.setError(getString(R.string.empty_password_str));
                    return;
                }

                //Login user
                loginUser(email, password);
            }
        });

        mSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser(String email, String password){
        mFirebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        //Sign in success
                        Intent enterApplication = new Intent(LoginActivity.this, MainActivity.class);
                        enterApplication.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        enterApplication.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(enterApplication);
                        finish();

                    } else {
                        //Sign in fail
                        mError.setText(getString(R.string.wrong_credentials_str));
                        mError.setVisibility(View.VISIBLE);
                    }
                }
            });
    }
}