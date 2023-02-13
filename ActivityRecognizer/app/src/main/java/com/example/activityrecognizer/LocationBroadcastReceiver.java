package com.example.activityrecognizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class LocationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent!=null){
            final String action = intent.getAction();
            if(Constants.BROADCAST_LOCATION.equals(action)){
                LocationResult result = LocationResult.extractResult(intent);
                if(result!=null){
                    List<Location> locations = result.getLocations();
                    for(int i=0; i<locations.size(); i++){
                        double latitude = locations.get(i).getLatitude();
                        double longitude = locations.get(i).getLongitude();
                        LatLng ll = new LatLng(latitude, longitude);
                        MyForegroundService.cd.latlngArray.add(ll);
                    }
                }
            }
        }
    }
}
