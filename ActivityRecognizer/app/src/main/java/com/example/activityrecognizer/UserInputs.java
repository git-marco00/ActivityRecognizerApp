package com.example.activityrecognizer;

import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class UserInputs {
    Button startBtn;
    Button endBtn;
    Button connBtn;
    EditText ip;

    UserInputs(AppCompatActivity a){
        startBtn = a.findViewById(R.id.startBtn);
        endBtn = a.findViewById(R.id.endBtn);
        connBtn = a.findViewById(R.id.conBtn);
        ip = a.findViewById(R.id.ipTextview);
    }


}
