package com.example.myapplication;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogFileService extends Service {
    public LogFileService() {
    }
    static final int MSG_SAY_HELLO = 1;
    static final int MSG_LOCATION = 2;
    static final String TAG = "LogFileService";


    public boolean isNewFile;
    // private static final String GPX_FILE_NAME = "location_data.gpx";
    // private static final String GPX_FILE_PATH = Environment.getExternalStorageDirectory().toString();
    private static final String GPX_FILE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
    // private static final String GPX_FILE_PATH = "/sdcard/Download";
    private String fileDir;
    static String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    static String fileName = "track_" + timestamp + ".gpx";
    private static final String GPX_NAMESPACE = "http://www.topografix.com/GPX/1/1";
    private static final String GPX_SCHEMA_LOCATION = "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd";
    private static final String GPX_VERSION = "1.1";

    @Override
    public IBinder onBind(Intent intent) {

        // Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        mMessenger = new Messenger(new IncomingHandler(this));

        Log.d(TAG, "file path: " + GPX_FILE_PATH);
        try {
            // boolean created = file.createNewFile();
            boolean create = createNewGPXFile();
            Log.d(TAG, "Create a new file: " + create);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return mMessenger.getBinder();
    }
    /*public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Log.d(TAG, "State: " + state);
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }*/

    private boolean createNewGPXFile() throws IOException {
        File gpxFile = new File(GPX_FILE_PATH, fileName);
        fileDir = gpxFile.getPath();
        boolean result = gpxFile.createNewFile();
        return result;
    }

    static class IncomingHandler extends Handler {
        private Context applicationContext;

        IncomingHandler(Context context) {
            applicationContext = context.getApplicationContext();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    Toast.makeText(applicationContext, "hello!", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "RECEIVED message in service");
                    break;

                case MSG_LOCATION:
                    Log.d(TAG, "Received location message in service.");
                    try {
                        // Create a new GPX file
                        File gpxFile = new File(GPX_FILE_PATH, fileName);

                        // Create an XML serializer
                        XmlSerializer serializer = android.util.Xml.newSerializer();
                        FileOutputStream fos = new FileOutputStream(gpxFile);
                        serializer.setOutput(fos, "UTF-8");
                        serializer.startDocument("UTF-8", true);

                        // Start GPX root element
                        serializer.startTag(GPX_NAMESPACE, "gpx");
                        serializer.attribute("", "version", GPX_VERSION);
                        serializer.attribute("", "creator", "Your App");
                        serializer.attribute("", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
                        serializer.attribute("", "xsi:schemaLocation", GPX_SCHEMA_LOCATION);

                        // Write your GPX data here
                        // For example, writing a waypoint
                        double latitude = 37.12345;
                        double longitude = -122.54321;
                        String name = "Example Waypoint";
                        // writeWaypoint(serializer, 37.12345, -122.54321, "Example Waypoint");
                        // Start waypoint element
                        serializer.startTag(GPX_NAMESPACE, "wpt");
                        serializer.attribute("", "lat", String.valueOf(latitude));
                        serializer.attribute("", "lon", String.valueOf(longitude));

                        // Write name
                        serializer.startTag(GPX_NAMESPACE, "name");
                        serializer.text(name);
                        serializer.endTag(GPX_NAMESPACE, "name");

                        // End waypoint element
                        serializer.endTag(GPX_NAMESPACE, "wpt");

                        // End GPX root element
                        serializer.endTag(GPX_NAMESPACE, "gpx");

                        serializer.endDocument();
                        serializer.flush();
                        fos.close();

                        Log.d(TAG, "GPX data written to: " + gpxFile.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }





    Messenger mMessenger;




}