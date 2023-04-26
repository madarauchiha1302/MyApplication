package com.example.task2;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView acc_x;
    private TextView acc_y;
    private TextView acc_z;
    private TextView gyro_x;
    private TextView gyro_y;
    private TextView gyro_z;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        acc_x = findViewById(R.id.acc_x);
        acc_y = findViewById(R.id.acc_y);
        acc_z = findViewById(R.id.acc_z);
        gyro_x = findViewById(R.id.gyro_x);
        gyro_y = findViewById(R.id.gyro_y);
        gyro_z = findViewById(R.id.gyro_z);

        // runs the SensorService that periodically fetches sensor data and broadcasts it
        getApplicationContext().startForegroundService(new Intent(getApplicationContext(), SensorService.class));

        // Register the broadcast receiver to receive sensor data from SensorService
        registerReceiver(sensorDataReceiver, new IntentFilter(SensorService.ACCELEROMETER_INTENT_NAME));
        registerReceiver(sensorDataReceiver, new IntentFilter(SensorService.GYROSCOPE_INTENT_NAME));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // when the app is destroyed, it does not have to fetch the data anymore.
        unregisterReceiver(sensorDataReceiver);
    }

    private final BroadcastReceiver sensorDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // get the x, y, and z data from the sensor that triggered the broadcast
            if (intent.getAction().equals(SensorService.ACCELEROMETER_INTENT_NAME)) {
                // Update the TextView objects with the sensor data
                acc_x.setText(String.format("x: %s", String.format("%.3f", intent.getFloatExtra("x", 0.0f))));
                acc_y.setText(String.format("y: %s", String.format("%.3f", intent.getFloatExtra("y", 0.0f))));
                acc_z.setText(String.format("z: %s", String.format("%.3f", intent.getFloatExtra("z", 0.0f))));
            }
            if (intent.getAction().equals(SensorService.GYROSCOPE_INTENT_NAME)) {
                // Update the TextView objects with the sensor data
                gyro_x.setText(String.format("x: %s", String.format("%.3f", intent.getFloatExtra("x", 0.0f))));
                gyro_y.setText(String.format("y: %s", String.format("%.3f", intent.getFloatExtra("y", 0.0f))));
                gyro_z.setText(String.format("z: %s", String.format("%.3f", intent.getFloatExtra("z", 0.0f))));
            }
        }
    };

}