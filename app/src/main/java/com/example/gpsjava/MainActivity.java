package com.example.gpsjava;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    public static final int RESULT_CODE = 100;
    public Button startButton, stopButton, updateButton, exitButton;
    public TextView latitude, longitude , distance, average;
    Handler h = new Handler();
    public String result = "Wait Wait Wait Wait";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.start);
        stopButton = findViewById(R.id.stop);
        updateButton = findViewById(R.id.update);
        exitButton = findViewById(R.id.exit);

        latitude = findViewById(R.id.latText);
        longitude = findViewById(R.id.lonText);
        distance = findViewById(R.id.distText);
        average = findViewById(R.id.avgText);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            },100);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            },100);
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Service_Start();
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Service_Stop();
            }
        });
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] loc_data = result.split(" ");
                latitude.setText(loc_data[0]);
                longitude.setText(loc_data[1]);
                average.setText(loc_data[2]);
                distance.setText(loc_data[3]);
            }
        });
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void Service_Start(){
        Intent serviceIntent = new Intent(this, LocationLogService.class);
        ResultReceiver r = new myReceiver(null);
        serviceIntent.putExtra("receiver",r);
        startService(serviceIntent);
    }
    public void Service_Stop(){
        Intent serviceIntent = new Intent(this, LocationLogService.class);
        stopService(serviceIntent);
    }

    public class myReceiver extends ResultReceiver{

        public myReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == RESULT_CODE){
                if(resultData!=null){
                    final String msg = resultData.getString("result");
                    result = msg;

                }
            }
        }


    }

}