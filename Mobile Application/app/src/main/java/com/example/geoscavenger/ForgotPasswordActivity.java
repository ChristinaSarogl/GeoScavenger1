package com.example.geoscavenger;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geoscavenger.functionalities.CheckSystemSpecs;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ForgotPasswordActivity";

    private TextInputLayout mEmailError;
    private TextInputEditText mEmail;
    private Button mReset;

    //Firebase instance variables
    private FirebaseAuth mFirebaseAuth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password_activity);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle(R.string.reset_password_str);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFirebaseAuth = FirebaseAuth.getInstance();

        CheckSystemSpecs checkSystemSpecs = new CheckSystemSpecs();

        mEmailError = (TextInputLayout) findViewById(R.id.reset_email_error_message);
        mEmail = (TextInputEditText) findViewById(R.id.reset_email);
        mReset = (Button) findViewById(R.id.reset_btn);

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

        mReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (!checkSystemSpecs.checkNetworkConnection(connectivityManager, getApplicationContext())){
                    return;
                }

                //Check inputted data
                String email = mEmail.getText().toString().trim();

                if (email.isEmpty()){
                    mEmailError.setError(getString(R.string.empty_email_str));
                    return;
                }

                mFirebaseAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(ForgotPasswordActivity.this, "Reset password email sent.",
                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Email sent.");
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mEmailError.setError("Please enter a valid email.");
                        }
                    });

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
