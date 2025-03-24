package com.example.geoscavenger;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CreditsActivity extends AppCompatActivity {
    private static final String TAG = "CreditsActivity";

    private TextView mMapPin;
    private TextView mTreasureMap;
    private TextView mTreasureChest;
    private TextView mCelebrate;
    private TextView mExplore;
    private TextView mProfile;
    private TextView mJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.credits_activity);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle(R.string.credits_str);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMapPin = (TextView) findViewById(R.id.map_pinURL);
        mTreasureMap = (TextView) findViewById(R.id.treasure_mapURL);
        mTreasureChest = (TextView) findViewById(R.id.treasure_chestURL);
        mCelebrate = (TextView) findViewById(R.id.celebrationURL);
        mExplore = (TextView) findViewById(R.id.exploreURL);
        mProfile = (TextView) findViewById(R.id.profileURL);
        mJoin = (TextView) findViewById(R.id.joinURL);

        mMapPin.setMovementMethod(LinkMovementMethod.getInstance());
        mTreasureMap.setMovementMethod(LinkMovementMethod.getInstance());
        mTreasureChest.setMovementMethod(LinkMovementMethod.getInstance());
        mCelebrate.setMovementMethod(LinkMovementMethod.getInstance());
        mExplore.setMovementMethod(LinkMovementMethod.getInstance());
        mProfile.setMovementMethod(LinkMovementMethod.getInstance());
        mJoin.setMovementMethod(LinkMovementMethod.getInstance());

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
