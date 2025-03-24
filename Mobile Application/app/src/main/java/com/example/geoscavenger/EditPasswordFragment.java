package com.example.geoscavenger;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.geoscavenger.functionalities.CheckSystemSpecs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EditPasswordFragment extends Fragment {
    private static final String TAG = "EditPasswordFragment";

    private TextInputLayout mOldError;
    private TextInputLayout mPasswordError;
    private TextInputLayout mReapeatPasswordError;
    private TextInputEditText mOldPass;
    private TextInputEditText mPassword;
    private TextInputEditText mRepeatPassword;
    private Button mSave;
    private ProgressBar mProgressBar;

    //Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.change_password_fragment, container, false);

        //Initialize Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        CheckSystemSpecs checkSystemSpecs = new CheckSystemSpecs();

        mOldError = (TextInputLayout) v.findViewById(R.id.change_old_password_error_message);
        mPasswordError = (TextInputLayout) v.findViewById(R.id.change_password_error_message);
        mReapeatPasswordError = (TextInputLayout) v.findViewById(R.id.change_repeat_password_error_message);
        mOldPass = (TextInputEditText) v.findViewById(R.id.change_old_password);
        mPassword = (TextInputEditText) v.findViewById(R.id.change_password);
        mRepeatPassword = (TextInputEditText) v.findViewById(R.id.change_repeat_password);
        mSave = (Button) v.findViewById(R.id.change_password_btn);

        mProgressBar = (ProgressBar) v.findViewById(R.id.password_progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);

        mOldPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(mOldPass.getText().toString().trim().length() > 0){
                    mOldError.setError(null);
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
                    mReapeatPasswordError.setError(null);
                }

            }
        });

        //Check internet connection
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!checkSystemSpecs.checkNetworkConnection(connectivityManager, getActivity())) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mSave.setEnabled(false);
        }

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String old = mOldPass.getText().toString().trim();
                String pass = mPassword.getText().toString().trim();
                String repeat = mRepeatPassword.getText().toString().trim();

                if (old.isEmpty()){
                    mOldError.setError(getString(R.string.old_password_missing_str));
                    return;
                }

                if (pass.isEmpty()){
                    mPasswordError.setError(getString(R.string.change_password_missing_str));
                    return;
                }

                if (repeat.isEmpty()){
                    mReapeatPasswordError.setError(getString(R.string.repeat_password_empty_str));
                    return;
                } else if(!repeat.matches(pass)){
                    mReapeatPasswordError.setError(getString(R.string.mismatch_passwords_error_str));
                    return;
                } else if(repeat.matches(old)){
                    mPasswordError.setError(getString(R.string.different_password_str));
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);
                changePassword(old, pass);
            }
        });

        return v;
    }

    private void changePassword(String oldPass, String newPass){
        //update password
        AuthCredential credential = EmailAuthProvider.getCredential(mFirebaseUser.getEmail(), oldPass);
        mFirebaseUser.reauthenticate(credential)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        mFirebaseUser.updatePassword(newPass)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Log.d(TAG,"Updated user password");
                                            mProgressBar.setVisibility(View.INVISIBLE);
                                            Toast.makeText(getActivity(),getString(R.string.password_update_str),
                                                    Toast.LENGTH_SHORT).show();
                                            getActivity().finish();
                                        } else {
                                            Log.d(TAG,"Failed to update user password");
                                            Toast.makeText(getActivity(),getString(R.string.password_update_failed),
                                                    Toast.LENGTH_SHORT).show();
                                            mProgressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    } else {
                        Log.d(TAG,"Error auth failed");
                        mOldError.setError(getString(R.string.old_password_wrong_str));
                        mProgressBar.setVisibility(View.GONE);
                    }
                }
            });

    }
}
