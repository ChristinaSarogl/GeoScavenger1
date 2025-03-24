package com.example.geoscavenger;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.geoscavenger.functionalities.CheckSystemSpecs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class JoinFragment extends Fragment {
    private static String TAG = "JoinFragment";
    private static final String HUNTS_COL = "hunts";
    private static final String USERS_COL = "users";

    private TextInputLayout mError;
    private TextInputEditText mHuntId;
    private Button mEnter;

    private long mPlayers;

    //Firebase variables
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseUser mFirebaseUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.join_fragment, container, false);

        CheckSystemSpecs checkSystemSpecs = new CheckSystemSpecs();

        //Check if GPS is on
        checkSystemSpecs.checkGPSConnection((LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE ),
                getActivity());

        //Check internet connection
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        checkSystemSpecs.checkNetworkConnection(connectivityManager, getActivity());

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mError = (TextInputLayout) v.findViewById(R.id.join_error_message);
        mHuntId = (TextInputEditText) v.findViewById(R.id.join_hunt_id);
        mEnter = (Button) v.findViewById(R.id.join_btn);

        mHuntId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mHuntId.getText().toString().trim().length() > 0){
                    mError.setError(null);
                }
            }
        });

        mEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkSystemSpecs.checkNetworkConnection(connectivityManager, getActivity())){
                    return;
                }

                if(!checkSystemSpecs.checkGPSConnection((LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE ),
                        getActivity())){
                    return;
                }

                //Check inputted data
                String huntId = mHuntId.getText().toString().trim();

                if (huntId.isEmpty()){
                    mError.setError(getString(R.string.join_hunt_id_error_str));
                    return;
                }

                mFirebaseFirestore.collection(HUNTS_COL).document(huntId).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot doc = task.getResult();
                                if (doc.exists()){
                                    mPlayers = (long) task.getResult().get("players");
                                    mPlayers = mPlayers + 1;

                                    Log.d(TAG, String.valueOf(mPlayers));

                                    mFirebaseFirestore.collection(USERS_COL).document(mFirebaseUser.getUid()).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    long enteredHunts = (long) task.getResult().get("joined_hunts");

                                                    enteredHunts = enteredHunts + 1;

                                                    Log.d(TAG, String.valueOf(enteredHunts));

                                                    mFirebaseFirestore.collection(HUNTS_COL).document(huntId)
                                                            .update("players", mPlayers);
                                                    mFirebaseFirestore.collection(USERS_COL).document(mFirebaseUser.getUid())
                                                            .update("joined_hunts", enteredHunts);

                                                    Intent intent = GameActivity.newIntent(getActivity(), huntId);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    getActivity().finish();
                                                }
                                            }
                                        });

                                } else {
                                    mError.setError(getString(R.string.join_invalid_hunt_id_str));
                                    return;
                                }
                            }
                        }
                    });
            }
        });

        return v;
    }
}
