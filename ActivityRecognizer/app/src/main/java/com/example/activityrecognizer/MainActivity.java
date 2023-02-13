package com.example.activityrecognizer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ProgressBar;

import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    public boolean connected=false;
    private String ipStr;
    ResultsBroadcastReceiver rbr;
    LocationBroadcastReceiver lbr;

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserInputs ui = new UserInputs(this);  // class to declare and control buttons in the Main Activity
        configureParams(ui); // to configure general parameters
        configureButtons(ui); // to configure buttons' functions
    }

    @TargetApi(Build.VERSION_CODES.O)
    public boolean foregroundServiceRunning(){
        // if the foreground service is running return true. Alternatively return false.
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(MyForegroundService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void configureParams(UserInputs ui){
        // filter for the results' intent
        IntentFilter filterrbr = new IntentFilter();
        filterrbr.addAction(Constants.BROADCAST_RESULTS);
        rbr = new ResultsBroadcastReceiver(this);
        registerReceiver(rbr, filterrbr);

        // filter for the location's intent
        IntentFilter filterlbr = new IntentFilter();
        filterlbr.addAction(Constants.BROADCAST_LOCATION);
        lbr = new LocationBroadcastReceiver();
        registerReceiver(lbr, filterlbr);

        // to permit the connection trial on the main thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // if i re-open my application and the foreground service is still running
        if(foregroundServiceRunning()){
            ui.endBtn.setEnabled(true);
            ui.startBtn.setEnabled(false);
            ProgressBar pb = findViewById(R.id.progressBar);
            pb.setVisibility(View.VISIBLE);
            pb.animate();
            ui.ip.setTextColor(Color.GREEN);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void configureButtons(UserInputs ui){
        // intent to create the foreground service, that will run in the background
        Intent serviceIntent = new Intent(this, MyForegroundService.class);

        // start button
        ui.startBtn.setOnClickListener(view ->{
            serviceIntent.putExtra("ip", ipStr);
            serviceIntent.putExtra("port", Constants.PORTNUMBER);
            // starts the foreground service
            startForegroundService(serviceIntent);
            // enable and disable buttons
            ui.endBtn.setEnabled(true);
            ui.startBtn.setEnabled(false);
            ProgressBar pb = findViewById(R.id.progressBar);
            pb.setVisibility(View.VISIBLE);
            pb.animate();
        });

        // end button
        ui.endBtn.setOnClickListener(view -> {
            stopService(serviceIntent);
        });

        // connection button
        ui.connBtn.setOnClickListener(view ->{
            String ipStr = String.valueOf(ui.ip.getText());
            try{
                // test to see if server is up or not.
                Socket s=new Socket(ipStr, Constants.PORTNUMBER);
                connected=true;
                this.ipStr=ipStr;
                ui.ip.setTextColor(Color.GREEN);
                ui.startBtn.setEnabled(true);
                s.close();
            } catch (IOException e){
                System.err.println(e.getMessage());
            }
        });
    }
}