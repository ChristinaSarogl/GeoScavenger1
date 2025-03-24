package com.example.geoscavenger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

public class PermissionsFragment extends Fragment {
    private static final String TAG = "PermissionsFragment";

    private LinearLayout mAndroid9;
    private LinearLayout mAndroidOther;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.permissions_fragment, container, false);

        mAndroid9 = (LinearLayout) v.findViewById(R.id.permissions_android9);
        mAndroidOther = (LinearLayout) v.findViewById(R.id.permissions_other);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q){
            mAndroid9.setVisibility(View.GONE);
        } else {
            mAndroidOther.setVisibility(View.GONE);
        }
        return v;
    }
}
