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
                /*
                String filename = "myfile";
                String downloadPath = "https://javadl.oracle.com/webapps/download/AutoDL?BundleId=248242_ce59cff5c23f4e2eaf4e778a117d4c5b";
                Calendar rightnow = Calendar.getInstance();
                filename = String.valueOf(rightnow.get(Calendar.DAY_OF_MONTH)) +
                        String.valueOf(rightnow.get(Calendar.HOUR_OF_DAY)) +
                        String.valueOf(rightnow.get(Calendar.MINUTE)) +
                        String.valueOf(rightnow.get(Calendar.SECOND));

                Log.i(ControlsProviderService.TAG, "filename = " +filename);
                File file = new File(context.getFilesDir(), filename);
                Log.d(ControlsProviderService.TAG,file.getPath());
                String fileContents = "Hello world!";

                try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
                    fos.write(fileContents.getBytes());

                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                /*
                if(uri != null) {
                    Intent intent = new Intent(context, DownloadService.class);
                    intent.putExtra("uri", uri.getPath());
                    startService(intent);
                }
                else {
                    Toast.makeText(context, "Please select a directory", Toast.LENGTH_SHORT).show();
                }

                 */
                Intent intent = new Intent(context, DownloadService.class);
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

            if (resultData != null) {
                uri = resultData.getData();
                // Perform operations on the document using its URI.
                Log.d(TAG, "user select dir: " + uri.getPath());

                textDir.setText(uri.getPath());
            }
        }
    }









}