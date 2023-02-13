package com.example.activityrecognizer;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

public class CollectedData{
    // array to maintain the prediction coming from the server
    ArrayList<Integer> activityArray;  // 0:walking 1:jogging 2:sit 3:standing 4:upstairs 5:downstairs
    private final long startTime;
    private long endTime;
    ArrayList<LatLng> latlngArray;  // array to maintain the location

    public CollectedData(){
        activityArray = new ArrayList<>();
        latlngArray = new ArrayList<>();
        startTime=System.currentTimeMillis();
        for(int i=0; i<6; i++){
            activityArray.add(0);
        }
    }

    public void stopTime(){
        endTime=System.currentTimeMillis();
    }

    public long getStartTime(){
        return startTime;
    }

    public long getEndTime(){
        return endTime;
    }

    @Override
    public String toString() {
        return "CollectedData{" +
                "activityArray=" + activityArray +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", latlngArray=" + latlngArray +
                '}';
    }

    public void addPrediction(String prediction){
        switch(prediction){
            case "'wlk'":
                activityArray.set(0,1+activityArray.get(0));
                break;
            case "'jog'":
                activityArray.set(1,1+activityArray.get(1));
                break;
            case "'sit'":
                activityArray.set(2,1+activityArray.get(2));
                break;
            case "'std'":
                activityArray.set(3,1+activityArray.get(3));
                break;
            case "'ups'":
                activityArray.set(4,1+activityArray.get(4));
                break;
            case "'dwn'":
                activityArray.set(5,1+activityArray.get(5));
                break;
        }
    }
}
