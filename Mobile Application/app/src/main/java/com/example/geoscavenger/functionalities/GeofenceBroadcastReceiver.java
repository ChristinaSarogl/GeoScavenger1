package com.example.geoscavenger.functionalities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceBroadcastReceiver";
    private static final String EXTRA_GEOFENCE_ID = "geofence_id";

    @Override
    public void onReceive(Context context, Intent intent) {
//        Toast.makeText(context, "Geofence triggered", Toast.LENGTH_SHORT)
//                .show();

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()){
            Log.d(TAG,"onReceive: Error receiving geofence event");
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();

        Log.d(TAG,"onReceive: " + geofenceList.get(0).getRequestId());
        Intent i = new Intent("android.intent.action.GeofenceReceiver")
                .putExtra(EXTRA_GEOFENCE_ID, geofenceList.get(0).getRequestId());
        context.sendBroadcast(i);
    }
}
