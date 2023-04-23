package com.example.mythirdapplication;

import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class MyBroadcastReceiver extends BroadcastReceiver {
    // private static final String TAG = "MyBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "receive a broadcast");
        String msg = intent.getExtras().getString("data");
        Toast.makeText(context, msg,Toast.LENGTH_SHORT).show();

        /*
        StringBuilder sb = new StringBuilder();
        sb.append("Action: " + intent.getAction() + "\n");
        sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
        String log = sb.toString();
        Log.d(TAG, log);

        ActivityNameBinding binding =
                ActivityNameBinding.inflate(layoutInflater);
        val view = binding.root;
        setContentView(view);

        Snackbar.make(view, log, Snackbar.LENGTH_LONG).show();

         */
    }
}