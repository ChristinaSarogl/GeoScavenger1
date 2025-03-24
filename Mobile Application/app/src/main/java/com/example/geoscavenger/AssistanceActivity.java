package com.example.geoscavenger;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geoscavenger.functionalities.ChatMessage;
import com.example.geoscavenger.functionalities.CheckSystemSpecs;
import com.example.geoscavenger.functionalities.WrapContentLinearLayoutManager;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.GeoPoint;


public class AssistanceActivity extends AppCompatActivity {
    public static final String TAG = "AssistanceActivity";
    private static final String HUNT = "hunt";
    private static final String MESSAGES_CHILD = "messages";
    private static final String PLAYERS_CHILD = "players";

    private CheckSystemSpecs mCheckSystemSpecs;
    private static String mHunt;
    private String mUsername;

    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private EditText mMessageEditText;
    private TextView mInfo;
    boolean changeToDark;

    //Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mRealtimeDatabase;
    private FirebaseRecyclerAdapter<ChatMessage,MessageViewHolder> mFirebaseAdapter;
    private DatabaseReference mMessagesRef;

    //Location variables
    private FusedLocationProviderClient mFusedProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    public static Intent newIntent(Context packageContext, String hunt){
        Intent intent = new Intent(packageContext, AssistanceActivity.class);
        intent.putExtra(HUNT, hunt);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.assistance_activity);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle(R.string.assistance_str);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        //Initialize ProgressBar and RecyclerView
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.assistance_message_recycler);
        mLinearLayoutManager = new WrapContentLinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mHunt = (String) getIntent().getSerializableExtra(HUNT);

        mCheckSystemSpecs = new CheckSystemSpecs();
        changeToDark = mCheckSystemSpecs.darkModeOn(this);

        mMessageEditText  = (EditText) findViewById(R.id.assistance_message_edit);
        mSendButton = (Button) findViewById(R.id.assistance_send_btn);
        mInfo = findViewById(R.id.assistance_info_message);

        //Check internet connection
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!mCheckSystemSpecs.checkNetworkConnection(connectivityManager, AssistanceActivity.this)){
            finish();
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            loadFirebaseMessages();
            mRealtimeDatabase.child(mHunt).child(PLAYERS_CHILD).child(mFirebaseAuth.getCurrentUser().getUid())
                    .child("disconnected").setValue(false);
            GameActivity.cancelNotifications();

            //Location tracking
            mFusedProviderClient = LocationServices.getFusedLocationProviderClient(this);
            buildLocationRequest();
            buildLocationCallback();
            mFusedProviderClient.requestLocationUpdates(mLocationRequest,mLocationCallback,null);
        }



        if (changeToDark){
            mInfo.setBackgroundColor(getColor(R.color.dark_grey));
        }

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMessageEditText.getText().toString().trim().length() != 0) {
                    mInfo.setVisibility(View.GONE);
                    ChatMessage message = new ChatMessage(mMessageEditText.getText().toString(), mUsername,"personal");
                    mMessagesRef.push().setValue(message);
                    mMessageEditText.setText("");
                }
            }
        });
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        TextView mMessage;
        TextView mMessengerInfo;

        public MessageViewHolder(View v){
            super(v);
            mMessage = (TextView) itemView.findViewById(R.id.assistance_message_text);
            mMessengerInfo = (TextView)  itemView.findViewById(R.id.assistance_messenger_info);
        }
    }

    private void loadFirebaseMessages() {
        mRealtimeDatabase = FirebaseDatabase.getInstance().getReference();
        SnapshotParser<ChatMessage> parser = new SnapshotParser<ChatMessage>() {
            @Override
            public ChatMessage parseSnapshot(DataSnapshot dataSnapshot) {
                ChatMessage chatMessage =
                        dataSnapshot.getValue(ChatMessage.class);
                if (chatMessage != null)
                    chatMessage.setId(dataSnapshot.getKey());
                return chatMessage;
            }
        };
        mMessagesRef = mRealtimeDatabase.child(mHunt).child(MESSAGES_CHILD).child(mFirebaseUser.getUid());
        FirebaseRecyclerOptions<ChatMessage> options =
                new FirebaseRecyclerOptions.Builder<ChatMessage>()
                        .setQuery(mMessagesRef, parser).build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder>(options) {
            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                mInfo.setVisibility(View.GONE);
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                if (changeToDark){
                    return new MessageViewHolder(inflater.inflate(R.layout.item_message_dark, viewGroup, false));
                } else {
                    return new MessageViewHolder(inflater.inflate(R.layout.item_message, viewGroup, false));
                }
            }

            @Override
            protected void onBindViewHolder(final MessageViewHolder viewHolder, int position, ChatMessage message) {
                viewHolder.mMessage.setText(message.getText());
                viewHolder.mMessengerInfo.setText(message.getName());
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int
                    itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int ChatMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 || (positionStart >= (ChatMessageCount - 1)
                        && lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
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
                }
            }
        };
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

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAdapter.stopListening();
        GameActivity.startNotifications();
        mFusedProviderClient.removeLocationUpdates(mLocationCallback);
        mRealtimeDatabase.child(mHunt).child(PLAYERS_CHILD).child(mFirebaseAuth.getCurrentUser().getUid())
                .child("disconnected").setValue(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
        GameActivity.cancelNotifications();
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
                    } else {
                        Log.d(TAG, "getCurrentLocation() : Location null");
                    }
                }
            });
    }
}
