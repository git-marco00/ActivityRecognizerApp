package com.example.activityrecognizer;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MyForegroundService extends Service implements SensorEventListener {
    public String ip;
    private ThreadServerCommunication tsc;
    public ArrayList<ArrayList<Float>> sensorsReadingsList; //0:linaccx 1:linaccy, 2:linaccz 3:rotX 4:rotY 5:rotZ 6:gravX 7:gravY 8:gravZ
    private Sensor linAccSensor;
    private Sensor gyroSensor;
    private Sensor gravSensor;
    static public CollectedData cd;
    FusedLocationProviderClient myClient;
    private PowerManager.WakeLock wakeLock;

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    public int onStartCommand(Intent intent, int flags, int startId) {
        // takes ip and port from the intent
        ip = intent.getStringExtra("ip");
        int port = intent.getIntExtra("port", Constants.PORTNUMBER);

        configureParams();

        return super.onStartCommand(intent, flags, startId);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void configureParams() {
        // wake lock acquisition
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.WAKELOCKTAG);
        wakeLock.acquire(10*60*1000L /*10 minutes*/);

        // sensors managing
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            linAccSensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        }
        if (sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            gyroSensor = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
        if (sm.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            gravSensor = sm.getDefaultSensor(Sensor.TYPE_GRAVITY);
        }
        sm.registerListener(this, linAccSensor, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(this, gravSensor, SensorManager.SENSOR_DELAY_GAME);

        // creation of class to keep collected data
        cd = new CollectedData();
        sensorsReadingsList = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ArrayList<Float> al = new ArrayList<>();
            sensorsReadingsList.add(al);
        }
        tsc = new ThreadServerCommunication(this);

        // managing of location data
        myClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest myRequest = LocationRequest.create();
        myRequest.setInterval(10000);
        myRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        Intent intent = new Intent(this, LocationBroadcastReceiver.class);
        intent.setAction(Constants.BROADCAST_LOCATION);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            myClient.requestLocationUpdates(myRequest, pi);
        } catch (SecurityException se){
            System.err.println(se.getMessage());
        }

        // creation of the notification channel
        NotificationChannel channel = new NotificationChannel(
                Constants.CHANNELID,
                Constants.CHANNELID,
                NotificationManager.IMPORTANCE_LOW
        );
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, Constants.CHANNELID)
                .setContentText("Activity Recognition is running....")
                .setContentTitle("Content Title");
        //.setSmallIcon(R.drawable.icon);
        startForeground(Constants.FOREGROUNDSERVICEID, notification.build());

        // thread to collect data is started
        tsc.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
        // i have to signal the thread
        tsc.interrupt();
        // i have to remove the wake lock on the cpu
        wakeLock.release();
        // i have to remove location updates
        Intent i = new Intent(this, LocationBroadcastReceiver.class);
        i.setAction(Constants.BROADCAST_LOCATION);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        myClient.removeLocationUpdates(pi);
        // i prepare the intent to send to the MainActivity the results of the foreground service, that are inside CollectedData
        Gson gson = new Gson();
        cd.stopTime();
        String jsonString = gson.toJson(cd);
        Intent intent = new Intent();
        intent.setAction(Constants.BROADCAST_RESULTS);
        intent.putExtra("cdInputExtra", jsonString);
        sendBroadcast(intent);
        // i destroy the foreground service.
        super.onDestroy();
    }

    @Override
    public synchronized void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION){
            sensorsReadingsList.get(0).add(event.values[0]);
            sensorsReadingsList.get(1).add(event.values[1]);
            sensorsReadingsList.get(2).add(event.values[2]);
        }
        if(event.sensor.getType()==Sensor.TYPE_GYROSCOPE){
            sensorsReadingsList.get(3).add(event.values[0]);
            sensorsReadingsList.get(4).add(event.values[1]);
            sensorsReadingsList.get(5).add(event.values[2]);
        }
        if(event.sensor.getType()==Sensor.TYPE_GRAVITY){
            sensorsReadingsList.get(6).add(event.values[0]);
            sensorsReadingsList.get(7).add(event.values[1]);
            sensorsReadingsList.get(8).add(event.values[2]);
        }
    }

    public synchronized String getValues(){
        String s = "";
        s=s.concat(sensorsReadingsList.get(0).toString()).concat("|").concat(sensorsReadingsList.get(1).toString()).concat("|").concat(sensorsReadingsList.get(2).toString()).concat("|");
        s=s.concat(sensorsReadingsList.get(3).toString()).concat("|").concat(sensorsReadingsList.get(4).toString()).concat("|").concat(sensorsReadingsList.get(5).toString()).concat("|");
        s=s.concat(sensorsReadingsList.get(6).toString()).concat("|").concat(sensorsReadingsList.get(7).toString()).concat("|").concat(sensorsReadingsList.get(8).toString());
        for(int i=0; i<9; i++){
            sensorsReadingsList.get(i).clear();
        }
        return s;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        System.out.println("Accuracy changed");
    }
}
