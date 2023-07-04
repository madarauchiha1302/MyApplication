package com.example.gpsjava;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationLogService extends Service implements LocationListener {
    boolean isCancelled = false;
    String location_data = "Wait Wait Wait Wait";
    String segment = "";
    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("OnStartCommand", "here");
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, (LocationListener) LocationLogService.this);
        ResultReceiver rr = intent.getParcelableExtra("receiver");
        Bundle b = new Bundle();
        Thread sendThread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (!isCancelled) {
                            Log.e("Sending results", "sending");
                            b.putString("result", location_data);
                            rr.send(MainActivity.RESULT_CODE, b);
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
        sendThread.setPriority(Thread.MIN_PRIORITY);
        sendThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "There is an update", Toast.LENGTH_SHORT).show();
        String data = location.getLongitude() + " " + location.getLatitude() + " " + location.getSpeed()+" " + location.getAccuracy();
        location_data = data;
        Log.i("There is an update", location_data);
        generatelocationfile(location);
    }

    @SuppressLint("LongLogTag")
    private void generatelocationfile(Location location) {
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<gpx\n" +
                "  version=\"1.1\"\n" +
                "  creator=\"Runkeeper - http://www.runkeeper.com\"\n" +
                "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "  xmlns=\"http://www.topografix.com/GPX/1/1\"\n" +
                "  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"\n" +
                "  xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\">";

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        segment += "<wpt lat=\"" + location.getLatitude() + "\" lon=\"" + location.getLongitude() + "\"><time>" + df.format(new Date(location.getTime())) + "</time><extensions><gpxtpx:TrackPointExtension><gpxtpx:hr>171</gpxtpx:hr></gpxtpx:TrackPointExtension></extensions></wpt>";
        Log.i("Segment", segment);
        String footer = "</gpx>";
        String data = header + segment + footer;
        String filename = "Location"+timestamp+".gpx";
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
        Log.i("location storage", directory);

        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(directory, filename);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data.getBytes());
            fos.close();
            Log.i("Write", "File written");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        isCancelled = true;
        Log.i("Service", "service stopped...");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
