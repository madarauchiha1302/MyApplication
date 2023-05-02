package com.example.mythirdapplication;

import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;



public class MyBroadcastReceiver extends BroadcastReceiver {
    // private static final String TAG = "MyBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "receive a broadcast");

        Toast.makeText(context, "Download Finished.",Toast.LENGTH_SHORT).show();


    }
}