package com.example.activityrecognizer;

public class Constants {
    // general constants
    static final int SLEEPTIME = 3000;  // it controls the time where the thread stays in sleeping mode before collecting data

    // server connection constants
    static final int PORTNUMBER = 1060;  // server's port
    static final String IP = "0.0.0.0";  // server's ip  (never used, could be useful for future developments

    // broadcast receiver ids
    static final String BROADCAST_LOCATION = "com.example.activityrecognizer.locationbroadcastreceiver";
    static final String BROADCAST_RESULTS = "com.example.activityrecognizer.resultsbroadcast";

    // foreground service constants
    static final String CHANNELID = "Foreground Service ID";
    static final String WAKELOCKTAG = "activityRecognizer::Wakelock";
    static final int FOREGROUNDSERVICEID = 1001;

    // maps constants
    static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
}
