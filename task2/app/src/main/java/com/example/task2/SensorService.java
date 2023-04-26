package com.example.task2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class SensorService extends Service implements SensorEventListener {
    public static final String ACCELEROMETER_INTENT_NAME = "com.example.task2.ACCELEROMETER_UPDATE";
    public static final String GYROSCOPE_INTENT_NAME = "com.example.task2.GYROSCOPE_UPDATE";
    private SensorManager sensorManager;

    @Override
    public void onCreate() {
        // fetch the device's sensors and create listeners for them
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        // a foreground service runs in the background when the user exits the app.
        startForeground(1, createNotification());
    }

    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // onBind not needed
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Intent intent = new Intent();
        // check which sensor triggered onSensorChanged
        switch(sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                intent.setAction(ACCELEROMETER_INTENT_NAME);
                break;
            case Sensor.TYPE_GYROSCOPE:
                intent.setAction(GYROSCOPE_INTENT_NAME);
                break;
        }
        // x: values[0]
        // y: values[1]
        // z: values[2]
        intent.putExtra("x", sensorEvent.values[0]);
        intent.putExtra("y", sensorEvent.values[1]);
        intent.putExtra("z", sensorEvent.values[2]);
        // broadcast the data to all listeners (in this case only the MainActivity)
        sendBroadcast(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // leaving this empty because it's irrelevant for this task
    }

    /**
     * Foreground services need notifications as parameter
     */
    private Notification createNotification() {
        String channelId = "sensor_service_channel";
        NotificationChannel channel = new NotificationChannel(channelId, "Sensor Service", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Sensor Service")
                .setContentText("Sensor data is being collected in the background")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);

        return builder.build();
    }
}