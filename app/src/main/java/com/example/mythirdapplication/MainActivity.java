package com.example.mythirdapplication;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
    private static final int OPEN_DIR = 1;
    private TextView textDir;
    private Uri uri;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonDownload = (Button) findViewById(R.id.button_download);
        textDir = (TextView) findViewById(R.id.text_dir);
        uri = null;
        context = this;


        // intent.setData(Uri.parse(fileUrl));

        buttonDownload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("BUTTONS", "User tapped the downloadButton");

                Intent intent = new Intent(context, DownloadService.class);
                startService(intent);
            }
        });


    }




}