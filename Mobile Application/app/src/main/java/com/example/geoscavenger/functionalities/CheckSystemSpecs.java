package com.example.geoscavenger.functionalities;

import android.content.Context;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.example.geoscavenger.R;

public class CheckSystemSpecs {
    private static final String TAG = "CheckSystemSpecs";

    public CheckSystemSpecs() {
    }

    public boolean checkNetworkConnection(ConnectivityManager manager, Context packegeContex) {
        NetworkInfo activeNetInfo = manager.getActiveNetworkInfo();
        if (activeNetInfo == null) {
            Log.d(TAG, "checkInternetConnection: No internet Connection");
            Toast.makeText(packegeContex, R.string.network_connection_failed,
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    public boolean checkGPSConnection(LocationManager manager, Context packegeContex) {
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "checkGPSConnection: No internet Connection");
            Toast.makeText(packegeContex, R.string.gps_connection_failed,
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    public boolean darkModeOn(Context context){
        boolean isDark = false;
        int nightModeFlags = context.getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                isDark = true;
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                isDark = false;
                break;

            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                isDark = false;
                break;
        }

        return isDark;
    }
}

