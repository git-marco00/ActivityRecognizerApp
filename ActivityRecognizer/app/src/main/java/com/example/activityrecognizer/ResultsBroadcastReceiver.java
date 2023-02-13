package com.example.activityrecognizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;

public class ResultsBroadcastReceiver extends BroadcastReceiver {
    // this class is in charge of receiving the results from the foreground service, and to start the new activity.
    MainActivity ma;

    public ResultsBroadcastReceiver(MainActivity ma){
        this.ma=ma;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent!=null){
            final String action = intent.getAction();
            if(Constants.BROADCAST_RESULTS.equals(action)){
                // collect results from the foreground service
                String message =intent.getStringExtra("cdInputExtra");

                // Open new activity to show the results
                Intent showResults = new Intent(ma, ResultsActivity.class);
                showResults.putExtra("cd", message);
                ma.startActivity(showResults);
            }
        }
    }
}
