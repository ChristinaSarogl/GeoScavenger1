package com.example.geoscavenger;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditProfileFragment extends Fragment {
    private static final String TAG = "EditProfileFragment";
    private static final String USER_COL = "users";
    private static final String IMAGES_FOL ="images";
    private static final String PHOTO_KEY = "photoUrl";
    private static final String USERNAME_KEY = "username";
    private static final String EMAIL_KEY ="email";
    private static final int REQUEST_IMAGE = 2;

    private CircularImageView mProfilePic;
    private TextInputEditText mUsername;
    private TextInputLayout mEmailError;
    private TextInputEditText mEmail;
    private Button mSave;
    private ProgressBar mProgressBar;

    private String mPhotoUrl;
    private boolean uploadImage = false;
    ProgressDialog progressDialog;

    //Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseStorage mFirebaseStorage;
    private CheckSystemSpecs mCheckSystemSpecs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.edit_profile_fragment, container, false);

        //Initialize Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = mFirebaseStorage.getReference();

        mCheckSystemSpecs = new CheckSystemSpecs();

        mProfilePic = (CircularImageView) v.findViewById(R.id.new_profile_pic);
        mUsername = (TextInputEditText) v.findViewById(R.id.new_username);
        mEmailError = (TextInputLayout) v.findViewById(R.id.email_error);
        mEmail = (TextInputEditText) v.findViewById(R.id.new_email);
        mSave = (Button) v.findViewById(R.id.profile_info_btn);
        mProgressBar = (ProgressBar) v.findViewById(R.id.profile_progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        //Check internet connection
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!mCheckSystemSpecs.checkNetworkConnection(connectivityManager, getActivity())) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mSave.setEnabled(false);
        } else {
            //Load picture from database
            loadProfilePic();
        }
        mUsername.setText(mFirebaseUser.getDisplayName());
        mEmail.setText(mFirebaseUser.getEmail());

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

        mProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent,REQUEST_IMAGE);
            }
        });

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Save changes
                String username = mUsername.getText().toString().trim();
                String email = mEmail.getText().toString().trim();

                if (!email.isEmpty() && !email.matches(mFirebaseUser.getEmail())){
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                        mEmailError.setError(getString(R.string.invalid_email_str));
                    } else {
                        showAuthenticationPopup(v, email,username, storageReference);
                    }
                } else {
                    checkOtherFieldChanged(username, storageReference);
                }
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"onActivityResult:requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode == REQUEST_IMAGE){
            if (resultCode == RESULT_OK){
                if (data != null){
                    final Uri uri = data.getData();
                    Log.d(TAG, "Uri:" + uri.toString());
                    mProfilePic.setImageURI(uri);
                    mProfilePic.setTag(uri.toString());
                    mProfilePic.setBorderWidth(0);
                    uploadImage = true;
                }

            }
        }
    }

    public void checkOtherFieldChanged(String username, StorageReference storageReference){
        if (uploadImage == true) {
            //If new image was uploaded, save to database
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            String stringUri = mProfilePic.getTag().toString();
            Uri uri = Uri.parse(stringUri);
            putImageInStorage(storageReference, uri);
        }

        if (!username.isEmpty() && !username.matches(mFirebaseUser.getDisplayName())) {
            //Update username
            changeUsername(username);
        }
    }

    public void loadProfilePic(){
        //Load image from storage
        mFirebaseFirestore.collection(USER_COL).document(mFirebaseUser.getUid())
            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot doc) {
                    if (doc.get(PHOTO_KEY) == null){
                        mProgressBar.setVisibility(View.INVISIBLE);
                        return;
                    }
                    mPhotoUrl = doc.get(PHOTO_KEY).toString();
                    StorageReference imageRef = FirebaseStorage.getInstance()
                            .getReferenceFromUrl(mPhotoUrl);
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUri) {
                            Log.d(TAG,"loadProfilePic: loaded picture.");
                            String downloadUrl = downloadUri.toString();
                            Glide.with(mProfilePic.getContext()).load(downloadUrl)
                                    .into(mProfilePic);
                            mProfilePic.setBorderWidth(0);
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            });
    }

    private void putImageInStorage(StorageReference storageReference,Uri uri){
        //Save new image to storage
        storageReference.child(mFirebaseUser.getUid())
            .child(IMAGES_FOL).child(UUID.randomUUID().toString())
            .putFile(uri)
            .addOnCompleteListener(getActivity(), new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        //Save URL to firestore
                        task.getResult().getMetadata().getReference().getDownloadUrl()
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()){
                                        Log.d(TAG,"putImageInStorage: uploaded picture to firestore.");
                                        DocumentReference userDoc = mFirebaseFirestore.collection(USER_COL)
                                                .document(mFirebaseUser.getUid());
                                        Map<String,Object> photoInfo = new HashMap<>();
                                        photoInfo.put(PHOTO_KEY, task.getResult().toString());
                                        userDoc.update(photoInfo);

                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(),getString(R.string.profile_uploaded_str),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    } else {
                        Log.d(TAG,"putImageInStorage: failed to upload picture to firestore.");
                        Toast.makeText(getActivity(),getString(R.string.profile_upload_error_str),
                                Toast.LENGTH_SHORT).show();
                        mProfilePic.setImageURI(null);
                    }
                    uploadImage = false;
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress
                            = (100.0
                            * snapshot.getBytesTransferred()
                            / snapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int)progress + "%");
                }
            });
    }

    private void changeUsername(String newUsername){
        DocumentReference userDoc = mFirebaseFirestore.collection(USER_COL)
            .document(mFirebaseUser.getUid());
        userDoc.update(USERNAME_KEY, newUsername)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Log.d(TAG,"changeUsername: Changed username.");

                        //Set user's display name
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(newUsername)
                                .build();
                        mFirebaseUser.updateProfile(profileUpdate);

                        Toast.makeText(getActivity(),getString(R.string.username_update_str),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG,"changeUsername: Failed to change username");
                        Toast.makeText(getActivity(),getString(R.string.username_update_failed),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void showAuthenticationPopup(View view, String email, String username, StorageReference storageReference){
        //Inflate the layout of the popup
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.authentification_popup, null);

        //Create popup
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popUp= new PopupWindow(popupView,width,height,focusable);
        popUp.showAtLocation(view, Gravity.CENTER,0,0);

        boolean changeToDark = mCheckSystemSpecs.darkModeOn(getActivity());
        if (changeToDark){
            popupView.findViewById(R.id.delete_frame_layout).setBackgroundResource(R.drawable.customborder_dark);
        }

        TextView promp = popupView.findViewById(R.id.popup_prompt);
        promp.setText(getString(R.string.authenticate_user_str));
        TextInputLayout error = popupView.findViewById(R.id.popup_error);
        TextInputEditText password = popupView.findViewById(R.id.popup_password);
        Button button = popupView.findViewById(R.id.popup_btn);
        button.setText(getString(R.string.authenticate_btn_str));

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
                    error.setError(null);
                }

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
                String pass = password.getText().toString().trim();

                if (pass.isEmpty()){
                    error.setError(getString(R.string.empty_password_str));
                } else {
                    AuthCredential credential = EmailAuthProvider.getCredential(mFirebaseUser.getEmail(), pass);
                    mFirebaseUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Log.d(TAG, "showAuthenticationPopup: Auth completed");
                                    popUp.dismiss();
                                    changeEmail(email);
                                    checkOtherFieldChanged(username, storageReference);
                                } else {
                                    Log.d(TAG, "showAuthenticationPopup: Error auth failed");
                                    error.setError(getString(R.string.old_password_wrong_str));
                                }
                            }
                        });
                }
            }
        });
    }

    private void changeEmail(String newEmail){
        mFirebaseUser.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.d(TAG, "changeEmail: Email updated");

                    DocumentReference userDoc = mFirebaseFirestore.collection(USER_COL)
                            .document(mFirebaseUser.getUid());
                    userDoc.update(EMAIL_KEY, newEmail);

                    Toast.makeText(getActivity(),getString(R.string.email_update_str),
                            Toast.LENGTH_SHORT).show();

                } else {
                    Log.d(TAG, "changeEmail: Email failed to update");
                    mEmail.setText(mFirebaseUser.getEmail());
                    Toast.makeText(getActivity(),getString(R.string.email_update_failed_str),
                            Toast.LENGTH_SHORT).show();
                }
                mProgressBar.setVisibility(View.INVISIBLE);

            }
        });
    }
}
