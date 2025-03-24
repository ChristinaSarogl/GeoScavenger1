package com.example.geoscavenger;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.geoscavenger.functionalities.CheckSystemSpecs;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private static final String USER_COL = "users";
    private static final String PHOTO_KEY = "photoUrl";

    private CircularImageView mProfilePic;
    private TextView mUsername;
    private TextView mEmail;
    private TextView mJoinedHunts;
    private CardView mSettings;
    private CardView mCredits;
    private CardView mLogOut;
    private ProgressBar mProgressBar;
    private String mPhotoUrl;
    private String joinedHunts;

    //Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseStorage mFirebaseStorage;
    private CheckSystemSpecs mCheckSystemSpecs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_fragment, container, false);

        //Initialize Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mCheckSystemSpecs = new CheckSystemSpecs();

        mProfilePic = (CircularImageView) v.findViewById(R.id.profile_image);
        mUsername = (TextView) v.findViewById(R.id.profile_username);
        mEmail = (TextView) v.findViewById(R.id.profile_email);
        mJoinedHunts = (TextView) v.findViewById(R.id.profile_joined_hunts);

        mSettings = (CardView) v.findViewById(R.id.profile_settings);
        mCredits = (CardView) v.findViewById(R.id.profile_credits);
        mLogOut = (CardView) v.findViewById(R.id.profile_logout);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        boolean changeToDark = mCheckSystemSpecs.darkModeOn(getContext());
        if (changeToDark){
            mSettings.setCardBackgroundColor(Color.parseColor("#2E5150"));
            mCredits.setCardBackgroundColor(Color.parseColor("#2E5150"));
            mLogOut.setCardBackgroundColor(Color.parseColor("#2E5150"));
        }

        //Check internet connection
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!mCheckSystemSpecs.checkNetworkConnection(connectivityManager, getActivity())){
            mProgressBar.setVisibility(View.INVISIBLE);
            mSettings.setEnabled(false);
        } else {
            //Load picture from database
            getJoinedHunts();
            loadProfilePic();
            mUsername.setText(mFirebaseUser.getDisplayName());
            mEmail.setText(mFirebaseUser.getEmail());
        }

        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        mCredits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CreditsActivity.class);
                startActivity(intent);
            }
        });

        mLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFirebaseAuth.signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        return v;
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

    private void getJoinedHunts(){
        mFirebaseFirestore.collection(USER_COL).document(mFirebaseUser.getUid())
            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                joinedHunts = doc.get("joined_hunts").toString();
                mJoinedHunts.setText(getString(R.string.hunts_joined_str) + " " + joinedHunts);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfilePic();
        mUsername.setText(mFirebaseUser.getDisplayName());
        mEmail.setText(mFirebaseUser.getEmail());
    }
}
