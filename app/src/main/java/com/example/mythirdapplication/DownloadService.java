package com.example.mythirdapplication;

import static android.service.controls.ControlsProviderService.TAG;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;
import android.os.Process;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

public class DownloadService extends Service {
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private String path;

    public DownloadService() {


    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.

            // TODO: download a file
            download();

            /*
            try {
                Thread.sleep(5000);

            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }
            */

            // download finished, send a broadcast
            Intent intent = new Intent("MY_DOWNLOAD_APP_BROADCAST");
            intent.setComponent(new ComponentName("com.example.mythirdapplication", "com.example.mythirdapplication.MyBroadcastReceiver"));

            intent.putExtra("data","Download finished!");
            sendBroadcast(intent);

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        // path = intent.getExtras().get("uri").toString();
        // Log.d(TAG, "Get path: :" + path + " in service");
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    private void download(){
        //String downloadPath = "http://www.aspu.edu.sy/laravel-filemanager/files/18/%D9%85%D8%B1%D8%A7%D8%AC%D8%B9%20%D9%83%D9%84%D9%8A%D8%A9%20%D8%A7%D9%84%D9%87%D9%86%D8%AF%D8%B3%D8%A9%20%D8%A7%D9%84%D9%85%D8%B9%D9%84%D9%88%D9%85%D8%A7%D8%AA%D9%8A%D8%A9/DISTRIBUTED%20SYSTEMS%20Concepts%20and%20Design.pdf";
        String downloadPath = "https://javadl.oracle.com/webapps/download/AutoDL?BundleId=248242_ce59cff5c23f4e2eaf4e778a117d4c5b";

        URLConnection conn = null;
        try {
            conn = new URL(downloadPath).openConnection();
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
        }
        // String filename = "myfile.txt";
        Calendar rightnow = Calendar.getInstance();
        String filename = String.valueOf(rightnow.get(Calendar.DAY_OF_MONTH)) +
                String.valueOf(rightnow.get(Calendar.HOUR_OF_DAY)) +
                String.valueOf(rightnow.get(Calendar.MINUTE)) +
                String.valueOf(rightnow.get(Calendar.SECOND));
        Log.i(TAG, "filename = " +filename);


        File file = new File(this.getFilesDir(), filename);
        Log.d(TAG,file.getPath());

        /*
        String fileContents = "Hello world!";
        try (FileOutputStream fos = this.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(fileContents.getBytes());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        */


        InputStream is = null;
        try {
            is = conn.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FileOutputStream fos = null;
        try {
            fos = this.openFileOutput(filename, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


        int len;
        byte[] bs = new byte[1024];
        try {
            while((len = is.read(bs)) != -1){
                fos.write(bs, 0, len);
            }
            fos.close();
            is.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }




    }



    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "download service is destroyed.");
    }
}