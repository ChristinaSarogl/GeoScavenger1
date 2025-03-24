package com.example.geoscavenger;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.geoscavenger.functionalities.ChatMessage;
import com.example.geoscavenger.functionalities.CheckSystemSpecs;
import com.example.geoscavenger.functionalities.Checkpoint;
import com.example.geoscavenger.functionalities.GeofenceHelper;

import com.example.geoscavenger.functionalities.NotificationHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String TAG = "GameActivity";
    public static final String HUNT = "hunt";
    private static final String CHECKPOINTS = "checkpoints";
    private static final String PLAYERS_CHILD = "players";
    private static final String MESSAGES_CHILD = "messages";
    private static final String EXTRA_GEOFENCE_ID = "geofence_id";
    private static final float DEFAULT_ZOOM = 20.0f;
    private static final float GEOFENCE_RADIUS = 10f;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private CheckSystemSpecs mCheckSystemSpecs;

    private ChildEventListener mMessageListener;
    private NotificationHelper mNotificationHelper;

    private static String mHunt;
    private List<Checkpoint> mCheckpoints = new ArrayList<>();
    private List<String> mAnswered = new ArrayList<>();

    private GeofencingClient mGeofencingClient;
    private GeofenceHelper mGeofenceHelper;

    private RadioGroup radioGroup;
    private int mScore = 0;
    private int mCurrentCheckpoint = 0;

    private Button mHelp;
    private Button mCancelHelp;
    private Button mAssistance;
    private Button mClue;
    private CircularImageView mExit;
    private ProgressBar mProgressBar;
    private static boolean inChat = false;
    boolean mChangeToDark;

    //Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirebaseFirestore;
    private DatabaseReference mRealtimeDatabase;

    public static Intent newIntent(Context packageContext, String hunt){
        Intent intent = new Intent(packageContext, GameActivity.class);
        intent.putExtra(HUNT, hunt);
        return intent;
    }

    public static void cancelNotifications(){
        Log.d(TAG,"cancelNotification");
        inChat = true;
    }

    public static void startNotifications(){
        Log.d(TAG,"startNotification");
        inChat = false;
    }

    private BroadcastReceiver mGeofenceReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String mGeofenceID = intent.getStringExtra(EXTRA_GEOFENCE_ID);
            Log.d(TAG,"Geofence received: " + mGeofenceID);
            Log.d(TAG,"Current checkpoint: " + mCheckpoints.get(mCurrentCheckpoint).getCheckpointID());

            if (!mAnswered.contains(mGeofenceID)){
                if (mGeofenceID.equals(mCheckpoints.get(mCurrentCheckpoint).getCheckpointID())){
                    mAnswered.add(mGeofenceID);
                    showQuestionPopup(mCurrentCheckpoint);
                }
            } else {
                Log.d(TAG, "Answered");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);
        getSupportActionBar().hide();

        mProgressBar = (ProgressBar) findViewById(R.id.game_progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_game);
        mapFragment.getMapAsync(this);

        mFusedProviderClient = LocationServices.getFusedLocationProviderClient(this);
        buildLocationRequest();
        buildLocationCallback();
        mFusedProviderClient.requestLocationUpdates(mLocationRequest,mLocationCallback,null);


        mCheckSystemSpecs = new CheckSystemSpecs();
        //Check internet connection
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!mCheckSystemSpecs.checkNetworkConnection(connectivityManager, GameActivity.this)){
            mFusedProviderClient.removeLocationUpdates(mLocationCallback);
            exitHunt();
        }

        mRealtimeDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        mHunt = (String) getIntent().getSerializableExtra(HUNT);

        mGeofencingClient = LocationServices.getGeofencingClient(this);
        mGeofenceHelper = new GeofenceHelper(this);

        //Notifications
        mNotificationHelper = new NotificationHelper(this);

        mRealtimeDatabase.child(mHunt).child(PLAYERS_CHILD).child(mFirebaseAuth.getCurrentUser().getUid())
            .child("name").setValue(mFirebaseAuth.getCurrentUser().getDisplayName());

        loadHuntInfo(mHunt);
        attachDatabaseListener();

        mHelp = (Button) findViewById(R.id.game_help_btn);
        mCancelHelp = (Button) findViewById(R.id.game_cancel_help_btn);
        mAssistance = (Button) findViewById(R.id.game_assistance_btn);
        mClue = (Button) findViewById(R.id.game_clue_btn);
        mExit = (CircularImageView) findViewById(R.id.game_exit_btn);

        mChangeToDark = mCheckSystemSpecs.darkModeOn(this);
        if (mChangeToDark){
            mHelp.setBackgroundColor(getColor(R.color.dark_sos_red));
            mCancelHelp.setBackgroundColor(getColor(R.color.dark_pastel_red));
            mAssistance.setBackgroundColor(getColor(R.color.dark_pastel_blue));
            mClue.setBackgroundColor(getColor(R.color.dark_pastel_teal));
        }

        mHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRealtimeDatabase.child(mHunt).child(PLAYERS_CHILD).child(mFirebaseAuth.getCurrentUser().getUid()).child("HELP").setValue(true);
                Toast.makeText(GameActivity.this, getString(R.string.send_help_str), Toast.LENGTH_LONG).show();
                mHelp.setVisibility(View.GONE);
                mCancelHelp.setVisibility(View.VISIBLE);
            }
        });

        mCancelHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRealtimeDatabase.child(mHunt).child(PLAYERS_CHILD).child(mFirebaseAuth.getCurrentUser().getUid()).child("HELP").removeValue();
                Toast.makeText(GameActivity.this, getString(R.string.cancel_help_str), Toast.LENGTH_LONG).show();
                mHelp.setVisibility(View.VISIBLE);
                mCancelHelp.setVisibility(View.GONE);
            }
        });

        mAssistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAssistance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_messages, 0, 0, 0);
                Intent intent = AssistanceActivity.newIntent(GameActivity.this, mHunt);
                startActivity(intent);
            }
        });

        mClue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCluePopup(mCurrentCheckpoint);
            }
        });

        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopUpdatingLocation();
                exitHunt();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            boolean success;
            if(mChangeToDark) {
                success = mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                this, R.raw.dark_map));
            } else {
                success = mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                this, R.raw.light_map));
            }

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        mMap.setMyLocationEnabled(true);
        mFusedProviderClient.getLastLocation()
            .addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng coordinates = new LatLng(location.getLatitude(), location.getLongitude());
                        mRealtimeDatabase.child(mHunt).child(PLAYERS_CHILD).child(mFirebaseAuth.getCurrentUser().getUid())
                                .child("location").setValue(new GeoPoint(coordinates.latitude,coordinates.longitude));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, DEFAULT_ZOOM));
                    } else {
                        Log.d(TAG, "getCurrentLocation() : Location null");
                    }
                }
            });
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
                for (Location location : locationResult.getLocations()){
                    mRealtimeDatabase.child(mHunt).child(PLAYERS_CHILD).child(mFirebaseAuth.getCurrentUser().getUid()).child("location")
                            .setValue(new GeoPoint(location.getLatitude(),location.getLongitude()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(
                            new LatLng(location.getLatitude(),location.getLongitude())));
                }
            }
        };
    }

    private void loadHuntInfo(String docID){
        mFirebaseFirestore.collection("hunts").document(docID)
            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    //Save name of hunt in Realtime database
                    mRealtimeDatabase.child(mHunt).child(PLAYERS_CHILD).child(mFirebaseAuth.getCurrentUser().getUid()).child("hunt_name")
                            .setValue(doc.get("name"));

                    List<String> checkpointNames = (ArrayList<String>) doc.get(CHECKPOINTS);
                    int checkpointNumber = checkpointNames.size();

                    //Save checkpoints in random order
                    Random rand = new Random();
                    int index;

                    while(checkpointNames.size() != 0){
                        index = rand.nextInt(checkpointNames.size());

                        mFirebaseFirestore.collection(CHECKPOINTS).document(checkpointNames.get(index))
                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Checkpoint checkpoint = documentSnapshot.toObject(Checkpoint.class);
                                checkpoint.setCheckpointID(documentSnapshot.getId());

                                List<String> answers = new ArrayList<>();
                                answers.add(0, documentSnapshot.get("0").toString());
                                answers.add(1, documentSnapshot.get("1").toString());
                                answers.add(2, documentSnapshot.get("2").toString());
                                answers.add(3, documentSnapshot.get("3").toString());
                                checkpoint.setAnswers(answers);
                                mCheckpoints.add(checkpoint);

                                //Add checkpoint
                                LatLng location = new LatLng(checkpoint.getLocation().getLatitude(),
                                        checkpoint.getLocation().getLongitude());
                                //addCircle(location);

                                Geofence geofence = mGeofenceHelper.getGeofence(checkpoint.getCheckpointID(),
                                        location, GEOFENCE_RADIUS);
                                GeofencingRequest geofencingRequest = mGeofenceHelper.getGeofencingRequest(geofence);
                                PendingIntent pendingIntent = mGeofenceHelper.getPendingIntent();
                                mGeofencingClient.addGeofences(geofencingRequest,pendingIntent)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Log.d(TAG,"onSuccess: Geofence added");
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception e) {
                                        String error = mGeofenceHelper.getErrorString(e);
                                        Log.d(TAG,"onFail: " + error);
                                    }
                                });

                                if(mCheckpoints.size() == checkpointNumber) {
                                    mProgressBar.setVisibility(View.GONE);
                                    showCluePopup(mCurrentCheckpoint);
                                }

                            }
                        });

                        checkpointNames.remove(index);
                    }
                }
            }
        });
    }

    public void attachDatabaseListener(){
        mMessageListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                Log.d(TAG, String.valueOf(snapshot));
                ChatMessage message = snapshot.getValue(ChatMessage.class);
                if(message.getName().equals("Admin")){
                    if(!inChat) {
                        mNotificationHelper.sendNotification("New Message!", message.getText(), mHunt);
                        mAssistance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_messages_new, 0, 0, 0);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {

            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        };

        mRealtimeDatabase.child(mHunt).child(MESSAGES_CHILD).child(mFirebaseAuth.getCurrentUser().getUid())
            .addChildEventListener(mMessageListener);
    }

    private void stopUpdatingLocation(){
        mRealtimeDatabase.child(mHunt).child(MESSAGES_CHILD).child(mFirebaseAuth.getCurrentUser().getUid())
                .removeEventListener(mMessageListener);
        mFusedProviderClient.removeLocationUpdates(mLocationCallback);
    }

    public void showQuestionPopup(int index){
        Checkpoint checkpoint = mCheckpoints.get(index);

        //Inflate the layout of the popup
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.question_popup, null);

        //Create popup
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popUp= new PopupWindow(popupView,width,height,false);
        popUp.showAtLocation(this.findViewById(R.id.game_activity), Gravity.CENTER,0,0);

        if (mChangeToDark){
            popupView.findViewById(R.id.question_linear_layout).setBackgroundResource(R.drawable.customborder_dark);
        }

        TextView question = popupView.findViewById(R.id.question_question);
        question.setText(checkpoint.getQuestion());
        TextView qError = popupView.findViewById(R.id.question_error);

        List<String> answers = checkpoint.getAnswers();
        RadioButton answer1 = popupView.findViewById(R.id.question_answer0);
        answer1.setText(answers.get(0));
        RadioButton answer2 = popupView.findViewById(R.id.question_answer1);
        answer2.setText(answers.get(1));
        RadioButton answer3 = popupView.findViewById(R.id.question_answer2);
        answer3.setText(answers.get(2));
        RadioButton answer4 = popupView.findViewById(R.id.question_answer3);
        answer4.setText(answers.get(3));

        radioGroup = (RadioGroup) popupView.findViewById(R.id.question_radio_group);
        Button submitBtn = popupView.findViewById(R.id.question_submit_btn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int checked = radioGroup.getCheckedRadioButtonId();
                RadioButton radioButton = (RadioButton) popupView.findViewById(checked);
                if (radioButton != null){
                    Log.d(TAG,"Checked:" + radioButton.getText());
                    popUp.dismiss();
                    //mMarkers.get(index).remove();
                    String tag = (String) radioButton.getTag();
                    if (Long.parseLong(tag) == checkpoint.getRightAnswerIndex()){
                        mScore++;
                    }
                    Log.d(TAG,"Score: " + mScore);

                    if (mAnswered.size() == mCheckpoints.size()){
                        stopUpdatingLocation();
                        Intent finishHunt = FinishHuntActivity.newIntent(getApplicationContext(), mScore);
                        finishHunt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        finishHunt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(finishHunt);
                        finish();
                    } else {
                        mCurrentCheckpoint++;
                        showCluePopup(mCurrentCheckpoint);
                    }

                } else {
                    qError.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    public void showCluePopup(int index){
        Checkpoint checkpoint = mCheckpoints.get(index);
        mClue.setEnabled(false);

        //Inflate the layout of the popup
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.clue_popup, null);

        //Create popup
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popUp= new PopupWindow(popupView,width,height,false);
        popUp.showAtLocation(this.findViewById(R.id.game_activity), Gravity.CENTER,0,0);

        if (mChangeToDark){
            popupView.findViewById(R.id.clue_frame_layout).setBackgroundResource(R.drawable.customborder_dark);
        }

        TextView clue = popupView.findViewById(R.id.game_next_clue);
        clue.setText(checkpoint.getClue());

        ImageButton exitBtn = popupView.findViewById(R.id.clue_exit_btn);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClue.setEnabled(true);
                popUp.dismiss();
            }
        });
    }

    private void addCircle(LatLng latLng){
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(GEOFENCE_RADIUS);
        circleOptions.strokeColor(Color.argb(255,255,0,0));
        circleOptions.fillColor(Color.argb(64,255,0,0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
    }

    private void exitHunt(){
        Intent exit = new Intent(GameActivity.this, MainActivity.class);
        exit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        exit.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(exit);
        finish();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mFusedProviderClient.removeLocationUpdates(mLocationCallback);
        mRealtimeDatabase.child(mHunt).child(PLAYERS_CHILD).child(mFirebaseAuth.getCurrentUser().getUid())
                .child("disconnected").setValue(true);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime now = LocalDateTime.now();
        mRealtimeDatabase.child(mHunt).child(PLAYERS_CHILD).child(mFirebaseAuth.getCurrentUser().getUid())
                .child("last_online").setValue(dtf.format(now));

        try {
            if(mGeofenceReceiver != null){
                unregisterReceiver(mGeofenceReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.GeofenceReceiver");
        registerReceiver(mGeofenceReceiver , filter);
        startNotifications();

        mRealtimeDatabase.child(mHunt).child(PLAYERS_CHILD).child(mFirebaseAuth.getCurrentUser().getUid())
                .child("disconnected").setValue(false);

        mFusedProviderClient.requestLocationUpdates(mLocationRequest,mLocationCallback,null);

        mFusedProviderClient.getLastLocation()
            .addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng coordinates = new LatLng(location.getLatitude(), location.getLongitude());
                        mRealtimeDatabase.child(mHunt).child(PLAYERS_CHILD).child(mFirebaseAuth.getCurrentUser().getUid())
                                .child("location").setValue(new GeoPoint(coordinates.latitude,coordinates.longitude));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, DEFAULT_ZOOM));
                    } else {
                        Log.d(TAG, "getCurrentLocation() : Location null");
                    }
                }
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealtimeDatabase.child(mHunt).child(PLAYERS_CHILD).child(mFirebaseAuth.getCurrentUser().getUid()).removeValue();
        mRealtimeDatabase.child(mHunt).child(MESSAGES_CHILD).child(mFirebaseAuth.getCurrentUser().getUid()).removeValue();
    }
}
