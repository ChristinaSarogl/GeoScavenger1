package com.example.geoscavenger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FinishHuntActivity extends AppCompatActivity {
    public static final String TAG = "FinishHuntActivity";
    private static final String EXTRA_SCORE = "score";

    private int mScore;

    private TextView mScoreTextView;
    private Button mExit;

    public static Intent newIntent(Context packageContext, int score){
        Intent intent = new Intent(packageContext, FinishHuntActivity.class);
        intent.putExtra(EXTRA_SCORE, score);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finish_hunt_activity);
        getSupportActionBar().setElevation(0);

        mScore = (int) getIntent().getSerializableExtra(EXTRA_SCORE);

        mScoreTextView = (TextView) findViewById(R.id.finish_score);
        mScoreTextView.setText("Score: " + mScore);

        mExit = (Button) findViewById(R.id.finish_btn);
        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent enterApplication = new Intent(getApplicationContext(), MainActivity.class);
                enterApplication.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                enterApplication.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(enterApplication);
                finish();
            }
        });
    }
}
