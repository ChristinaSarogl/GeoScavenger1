package com.example.geoscavenger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class SettingsOptionsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsOptionsActivity";
    public static final String EXTRA_OPTION = "option";

    public static Intent newIntent(Context packageContext, String option){
        Intent intent = new Intent(packageContext, SettingsOptionsActivity.class);
        intent.putExtra(EXTRA_OPTION, option);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_options_activity);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String option = (String) getIntent().getSerializableExtra(EXTRA_OPTION);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container_settings);
        //If container is empty, load fragment
        if (fragment==null){
            if(option.matches("profile")){
                getSupportActionBar().setTitle(getString(R.string.edit_profile_str));
                fm.beginTransaction()
                        .add(R.id.fragment_container_settings, new EditProfileFragment())
                        .commit();
            } else if(option.matches("password")){
                getSupportActionBar().setTitle(getString(R.string.change_password_str));
                fm.beginTransaction()
                        .add(R.id.fragment_container_settings,new EditPasswordFragment())
                        .commit();
            }
        }
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
