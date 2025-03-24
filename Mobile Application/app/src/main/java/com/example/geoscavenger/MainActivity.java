package com.example.geoscavenger;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.geoscavenger.functionalities.CheckSystemSpecs;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int GRANT_LOCATION_PERMISSIONS = 1;
    private static final int GRANT_BACKGROUND_PERMISSION = 2;
    private static final int GRANT_FINE_LOCATION_PERMISSION = 3;

    private LinearLayout mExplore;
    private LinearLayout mProfile;
    private LinearLayout mJoin;
    private ImageView mExploreImageView;
    private ImageView mProfileImageView;
    private ImageView mJoinImageView;
    private boolean permissionsGranted;
    private CheckSystemSpecs mCheckSystemSpecs;

    private String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        getSupportActionBar().setElevation(0);

        mCheckSystemSpecs = new CheckSystemSpecs();
        boolean changeToDark = mCheckSystemSpecs.darkModeOn(this);
        if (changeToDark){
            LinearLayout navBarContainer = findViewById(R.id.nav_bar);
            navBarContainer.setBackgroundColor(getColor(R.color.lighter_black));
        }

        mExplore = (LinearLayout) findViewById(R.id.explore_nav_layout);
        mProfile = (LinearLayout) findViewById(R.id.profile_nav_layout);
        mJoin = (LinearLayout) findViewById(R.id.join_nav_layout);
        mExploreImageView = (ImageView) findViewById(R.id.explore_nav_image_view);
        mProfileImageView = (ImageView) findViewById(R.id.profile_nav_image_view);
        mJoinImageView = (ImageView) findViewById(R.id.join_nav_image_view);

        mExplore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissions(getApplicationContext());
                if (permissionsGranted){
                    changeFragment(ExploreFragment.class);
                } else {
                    changeFragment(PermissionsFragment.class);
                }

                mExploreImageView.setImageResource(R.drawable.nav_explore_green);
                mProfileImageView.setImageResource(R.drawable.nav_profile);
                mJoinImageView.setImageResource(R.drawable.nav_join);
            }
        });

        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFragment(ProfileFragment.class);
                mProfileImageView.setImageResource(R.drawable.nav_profile_green);
                mExploreImageView.setImageResource(R.drawable.nav_explore);
                mJoinImageView.setImageResource(R.drawable.nav_join);
            }
        });

        mJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFragment(JoinFragment.class);
                mJoinImageView.setImageResource(R.drawable.nav_join_green);
                mProfileImageView.setImageResource(R.drawable.nav_profile);
                mExploreImageView.setImageResource(R.drawable.nav_explore);
            }
        });

        checkPermissions(getApplicationContext());
        if (permissionsGranted){
            changeFragment(ExploreFragment.class);
        } else {
            changeFragment(PermissionsFragment.class);
        }
    }

    private void checkPermissions(Context context){
        if(hasPemissions(context,PERMISSIONS)) {
            permissionsGranted = true;
        } else {
            getPermissions(context);
        }
    }

    private boolean hasPemissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                for (String permission : permissions) {
                    if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        mJoin.setClickable(false);
                        return false;
                    }
                }
            } else {
                if (ActivityCompat.checkSelfPermission(context, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void getPermissions(Context context){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.access_location_str)
                    .setNeutralButton("Continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();

                            requestPermissions(PERMISSIONS, GRANT_LOCATION_PERMISSIONS);
                        }
                    })
                    .create().show();
        } else {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, GRANT_FINE_LOCATION_PERMISSION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GRANT_LOCATION_PERMISSIONS) {
            Log.d(TAG,"Grant background and fine test");
            if (grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG,"Granted both");

                    mJoin.setClickable(true);
                    changeFragment(ExploreFragment.class);

                } else {
                    Toast.makeText(this, "Background location access is necessary for geofences to trigger...",
                            Toast.LENGTH_LONG).show();
                    requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, GRANT_BACKGROUND_PERMISSION);
                }

            } else {
                permissionsGranted = false;
                changeFragment(PermissionsFragment.class);

            }
        } else if (requestCode == GRANT_BACKGROUND_PERMISSION){
            Log.d(TAG,"Background test");
            if(grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mJoin.setClickable(true);
                changeFragment(ExploreFragment.class);
            } else {
                permissionsGranted = false;
                changeFragment(PermissionsFragment.class);
            }
        } else if (requestCode == GRANT_FINE_LOCATION_PERMISSION){
            Log.d(TAG,"Fine granted test");
            if(grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mJoin.setClickable(true);
                changeFragment(ExploreFragment.class);
            } else {
                permissionsGranted = false;
                changeFragment(PermissionsFragment.class);
            }
        }
    }

    private void changeFragment(Class className){
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container_main);
        //If container is empty load the fragment
        if (fragment == null){
            try {
                fragment = (Fragment) className.newInstance();
                fm.beginTransaction().add(R.id.fragment_container_main, fragment)
                        .commit();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

        } else {
            //If the container is not empty replace the previous fragment
            try {
                Fragment new_fragment = (Fragment) className.newInstance();
                fm.beginTransaction().replace(R.id.fragment_container_main,new_fragment, null)
                        .commit();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }
}
