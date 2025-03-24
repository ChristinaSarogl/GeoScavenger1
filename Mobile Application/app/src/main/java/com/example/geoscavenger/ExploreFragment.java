package com.example.geoscavenger;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.geoscavenger.functionalities.CheckSystemSpecs;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExploreFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {
    private static final String TAG = "ExploreFragment";
    private static final float DEFAULT_ZOOM = 16.0f;
    private static final String HUNTS_COL = "hunts";
    private static final String CHECKPOINTS = "checkpoints";
    private static final String USERS_COL = "users";

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private CheckSystemSpecs mCheckSystemSpecs;

    private List<HashMap> mHunts = new ArrayList<>();
    private Button mEnterHunt;
    private String mHuntID;
    private long mPlayers;
    private long mEnteredHunts;

    //Firebase instance variables
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore mFirebaseFirestore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.explore_fragment, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mCheckSystemSpecs = new CheckSystemSpecs();
        //Check if GPS is on
        mCheckSystemSpecs.checkGPSConnection((LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE ),
                getActivity());
        //Check internet connection
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        mCheckSystemSpecs.checkNetworkConnection(connectivityManager, getActivity());

        mFusedProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        buildLocationRequest();
        buildLocationCallback();
        mFusedProviderClient.requestLocationUpdates(mLocationRequest,mLocationCallback,null);

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mEnterHunt = (Button) v.findViewById(R.id.explore_enter_btn);
        mEnterHunt.setEnabled(false);
        mEnterHunt.setVisibility(View.GONE);

        mEnterHunt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!mCheckSystemSpecs.checkNetworkConnection(connectivityManager, getActivity())){
                    return;
                }

                if(!mCheckSystemSpecs.checkGPSConnection((LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE ),
                        getActivity())){
                    return;
                }

                mFirebaseFirestore.collection(HUNTS_COL).document(mHuntID).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                mPlayers = (long) task.getResult().get("players");
                                mPlayers = mPlayers + 1;

                                mFirebaseFirestore.collection(USERS_COL).document(mFirebaseUser.getUid()).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                mEnteredHunts = (long) task.getResult().get("joined_hunts");

                                                mEnteredHunts = mEnteredHunts + 1;

                                                mFirebaseFirestore.collection(HUNTS_COL).document(mHuntID)
                                                        .update("players", mPlayers);
                                                mFirebaseFirestore.collection(USERS_COL).document(mFirebaseUser.getUid())
                                                        .update("joined_hunts", mEnteredHunts);

                                                Intent intent = GameActivity.newIntent(getActivity(), mHuntID);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                getActivity().finish();
                                            }
                                        }
                                    });
                            }
                        }
                    });

            }
        });

        return v;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        try {
            boolean success;
            boolean changeToDark = mCheckSystemSpecs.darkModeOn(getActivity());
            if(changeToDark) {
                success = mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                getActivity(), R.raw.dark_map));
            } else {
                success = mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                getActivity(), R.raw.light_map));
            }

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        getCurrentLocation();
    }

    private void getCurrentLocation(){
        mMap.setMyLocationEnabled(true);
        mFusedProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng coordinates = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, DEFAULT_ZOOM));

                        } else {
                            Log.d(TAG, "getCurrentLocation() : Location null");
                        }
                    }
                });
        getAvailableHunts();
    }

    private void getAvailableHunts() {
        mFirebaseFirestore.collection(HUNTS_COL).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documents) {
                for (QueryDocumentSnapshot doc : documents) {
                    String name = doc.get("name").toString();
                    String documentID = doc.getId();
                    ArrayList<String> checkpoints = (ArrayList<String>) doc.get(CHECKPOINTS);
                    String firstCheck = checkpoints.get(0);
                    HashMap<String, Object> huntInfo = new HashMap<>();
                    huntInfo.put("documentID", documentID);
                    huntInfo.put("name", name);
                    huntInfo.put("checkpoint", firstCheck);
                    mHunts.add(huntInfo);
                }
                displayHunts();
            }
        });
    }

    private void displayHunts() {
        for (int i = 0; i < mHunts.size(); i++) {
            HashMap<String, Object> tmp = mHunts.get(i);
            String name = tmp.get("name").toString();
            String checkpoint = tmp.get("checkpoint").toString();
            String id = tmp.get("documentID").toString();
            mFirebaseFirestore.collection(CHECKPOINTS).document(checkpoint).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot doc) {
                            GeoPoint geoPoint = (GeoPoint) doc.get("location");
                            LatLng location = new LatLng(geoPoint.getLatitude(),
                                    geoPoint.getLongitude());
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .title(name)
                                    .position(location));
                            marker.setTag(id);
                        }
                    });
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mEnterHunt.setEnabled(true);
        mEnterHunt.setVisibility(View.VISIBLE);
        mHuntID = marker.getTag().toString();

        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mEnterHunt.setEnabled(false);
        mEnterHunt.setVisibility(View.GONE);
    }

    private void buildLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100);

        mLocationRequest.setFastestInterval(100);
        mLocationRequest.setSmallestDisplacement(1);
    }

    private void buildLocationCallback(){
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
            }
        };
    }
}
