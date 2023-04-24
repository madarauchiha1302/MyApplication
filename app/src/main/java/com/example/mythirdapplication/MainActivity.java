package com.example.mythirdapplication;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final int OPEN_DIR = 1;
    private TextView textDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonDownload = (Button) findViewById(R.id.button_download);
        textDir = (TextView) findViewById(R.id.text_dir);

        Intent intent = new Intent(this, DownloadService.class);

        // intent.setData(Uri.parse(fileUrl));

        buttonDownload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("BUTTONS", "User tapped the downloadButton");
                startService(intent);
            }
        });

        Button buttonDir = (Button) findViewById(R.id.button_dir);
        buttonDir.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                openDirectory();
            }
        });


    }

    public void openDirectory() {
        // Choose a directory using the system's file picker.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when it loads.
        // intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriToLoad);

        startActivityForResult(intent, OPEN_DIR);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == OPEN_DIR
                && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                // Perform operations on the document using its URI.
                Log.d(TAG, "user select dir: " + uri.getPath());
                updateDir(uri);
            }
        }
    }

    private void updateDir(Uri uri){
        textDir.setText(uri.getPath());
    }





}