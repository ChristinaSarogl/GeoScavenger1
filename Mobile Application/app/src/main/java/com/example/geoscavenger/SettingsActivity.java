package com.example.geoscavenger;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geoscavenger.functionalities.CheckSystemSpecs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    private static final String USER_COL = "users";
    private static final String IMAGES_FOL ="images";

    private TextView mEditProfile;
    private TextView mChangePassword;
    private TextView mDeleteAccount;
    private ProgressBar mProgressBar;
    private CheckSystemSpecs mCheckSystemSpecs;

    //Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseStorage mFirebaseStorage;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle(R.string.settings_str);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initialize Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mCheckSystemSpecs = new CheckSystemSpecs();

        mEditProfile = (TextView) findViewById(R.id.settings_edit_profile);
        mChangePassword = (TextView) findViewById(R.id.settings_change_password);
        mDeleteAccount = (TextView) findViewById(R.id.settings_delete_account);
        mProgressBar = (ProgressBar) findViewById(R.id.settings_progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);

        mEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = SettingsOptionsActivity.newIntent(SettingsActivity.this,"profile");
                startActivity(intent);
            }
        });

        mChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = SettingsOptionsActivity.newIntent(SettingsActivity.this,"password");
                startActivity(intent);
            }
        });

        mDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeletePopup(view);
            }
        });
    }

    private void showDeletePopup(View view){
        //Inflate the layout of the popup
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.authentification_popup, null);

        //Create popup
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popUp= new PopupWindow(popupView,width,height,focusable);
        popUp.showAtLocation(view, Gravity.CENTER,0,0);

        boolean changeToDark = mCheckSystemSpecs.darkModeOn(this);
        if (changeToDark){
            popupView.findViewById(R.id.delete_frame_layout).setBackgroundResource(R.drawable.customborder_dark);
        }

        TextInputLayout deleteError = popupView.findViewById(R.id.popup_error);
        TextInputEditText password = popupView.findViewById(R.id.popup_password);
        Button deleteBtn = popupView.findViewById(R.id.popup_btn);

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (password.getText().toString().trim().length() > 0){
                    deleteError.setError(null);
                }

            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Check internet connection
                ConnectivityManager connectivityManager
                        = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (!mCheckSystemSpecs.checkNetworkConnection(connectivityManager, SettingsActivity.this)){
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);
                String pass = password.getText().toString().trim();

                if (pass.isEmpty()){
                    deleteError.setError(getString(R.string.empty_password_str));
                } else {
                    AuthCredential credential = EmailAuthProvider.getCredential(mFirebaseUser.getEmail(), pass);
                    mFirebaseUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    //Delete images from storage
                                    StorageReference storageRef = mFirebaseStorage.getReference()
                                            .child(mFirebaseUser.getUid()).child(IMAGES_FOL);
                                    storageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                        @Override
                                        public void onSuccess(ListResult listResult) {
                                            for (StorageReference item : listResult.getItems()){
                                                Log.d(TAG,item.getName().toString());
                                                item.delete();
                                            }
                                            Log.d(TAG, "Images deleted");
                                        }
                                    });

                                    mFirebaseFirestore.collection(USER_COL).document(mFirebaseUser.getUid())
                                            .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> delCollection) {
                                            if (delCollection.isSuccessful()){
                                                Log.d(TAG,"Firestore document deleted");
                                            }
                                        }
                                    });

                                    mFirebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> delUser) {
                                            if (delUser.isSuccessful()){
                                                mProgressBar.setVisibility(View.INVISIBLE);
                                                Log.d(TAG,"User deleted");
                                                Toast.makeText(SettingsActivity.this,getString(R.string.delete_user_str),
                                                        Toast.LENGTH_SHORT).show();
                                                popUp.dismiss();
                                                mFirebaseAuth.signOut();
                                                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    });

                                } else {
                                    Log.d(TAG, "Error auth failed");
                                    deleteError.setError(getString(R.string.old_password_wrong_str));
                                }
                            }
                        });
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
