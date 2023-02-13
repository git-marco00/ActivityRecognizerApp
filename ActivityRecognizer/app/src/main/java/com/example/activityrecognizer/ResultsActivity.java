package com.example.activityrecognizer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

public class ResultsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private CollectedData cd;  // data collected and passed into the intent
    MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // collecting the data passed into the intent
        Intent intent = getIntent();
        String cdString = intent.getStringExtra("cd");
        Gson gson=new Gson();
        cd= gson.fromJson(cdString, CollectedData.class);

        // putting collected data into the GUI
        GUIdata();

        // maps managing
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(Constants.MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(cd.latlngArray);
        googleMap.addPolyline(polylineOptions);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i=0; i<cd.latlngArray.size(); i++){
            builder.include(cd.latlngArray.get(i));
        }
        int padding=50;
        LatLngBounds bounds= builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.setOnMapLoadedCallback(() -> {
            googleMap.animateCamera(cu);
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void GUIdata(){
        // collect TextViews to modify
        TextView TVwalk = findViewById(R.id.idWalk);
        TextView TVstand = findViewById(R.id.idStand);
        TextView TVsit = findViewById(R.id.idSit);
        TextView TVjog = findViewById(R.id.idJog);
        TextView TVupst = findViewById(R.id.idUpst);
        TextView TVdown = findViewById(R.id.idDown);
        TextView TVtime = findViewById(R.id.idTime);
        TextView TVdistance = findViewById(R.id.idDistance);

        // timer
        long millis = cd.getEndTime() - cd.getStartTime();
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) (millis / 1000 / 60) % 60;
        int hours = (int) (millis / 1000 / 60 / 60);
        String timeStr = hours + ":" + minutes + ":" + seconds;
        TVtime.setText(timeStr);

        // activities computation
        int totalActivities=0;
        for(int i=0; i<6; i++){
            totalActivities += cd.activityArray.get(i);
        }
        int walkPerc = cd.activityArray.get(0)*100/totalActivities;
        int jogPerc = cd.activityArray.get(1)*100/totalActivities;
        int sitPerc = cd.activityArray.get(2)*100/totalActivities;
        int standPerc = cd.activityArray.get(3)*100/totalActivities;
        int upsPerc = cd.activityArray.get(4)*100/totalActivities;
        int downPerc = cd.activityArray.get(5)*100/totalActivities;

        String walkStr = walkPerc + " %";
        String jogStr = jogPerc + " %";
        String sitStr = sitPerc + " %";
        String standStr = standPerc + " %";
        String upsStr = upsPerc + " %";
        String downStr = downPerc + " %";

        TVwalk.setText(walkStr);
        TVjog.setText(jogStr);
        TVsit.setText(sitStr);
        TVstand.setText(standStr);
        TVupst.setText(upsStr);
        TVdown.setText(downStr);

        // distance computation
        float distance=0;
        float [] distArray = new float[3];
        LatLng startPoint=cd.latlngArray.get(0);
        for (int i=1; i<cd.latlngArray.size(); i++){
            Location.distanceBetween(startPoint.latitude, startPoint.longitude, cd.latlngArray.get(i).latitude, cd.latlngArray.get(i).longitude, distArray);
            distance+=distArray[0];
            startPoint=cd.latlngArray.get(i);
        }
        String distStr=(int)distance + " m";
        TVdistance.setText(distStr);
    }
}