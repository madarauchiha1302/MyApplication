package com.example.mythirdapplication;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.service.controls.ControlsProviderService;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private DownloadManager downloadManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonDownloadSmallFile = (Button) findViewById(R.id.button_download);
        Button buttonDownloadLargeFile = (Button) findViewById(R.id.button_largeFile);

        context = this;
        downloadManager = (DownloadManager)this.getSystemService(DOWNLOAD_SERVICE);



        buttonDownloadSmallFile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //  an apk file 180kb
                Log.d("BUTTONS", "User tapped the downloadSmallButton");
                String downloadPath = "https://apkpure.com/cn/roblox-for-android/com.roblox.client/download";

                // async download, to avoid blocking the main thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        download(downloadPath);
                    }
                }).start();
            }
        });

        buttonDownloadLargeFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  an apk file 1.3gb
                Log.d("BUTTONS", "User tapped the downloadLargeButton");
                String downloadPath = "https://d.apkpure.com/b/APK/com.tencent.ig?version=latest";

                // async download, to avoid blocking the main thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        download(downloadPath);
                    }
                }).start();

            }
        });





    }

    private void download(String downloadPath){

        Uri uri = Uri.parse(downloadPath);

        //  prepare the file path and name to store the file
        Calendar rightnow = Calendar.getInstance();
        String filename = String.valueOf(rightnow.get(Calendar.DAY_OF_MONTH)) +
                String.valueOf(rightnow.get(Calendar.HOUR_OF_DAY)) +
                String.valueOf(rightnow.get(Calendar.MINUTE)) +
                String.valueOf(rightnow.get(Calendar.SECOND)) + ".apk";

        //  send request to download manager
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, filename);

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);

        //  start download
        Log.d(TAG,"download started.");
        downloadManager.enqueue(request);



    }




}