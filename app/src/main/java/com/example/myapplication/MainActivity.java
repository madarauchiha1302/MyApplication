package com.example.myapplication;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private TextView locationAddressText;
    private Button locationButton;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int  REQUEST_PERMISSION = 1;
    private Button startButton;
    private Button stopButton;
    public Messenger mService;
    public boolean mBound;
    private ServiceConnection mConnection;

    private Button testButton;

    private static final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            MANAGE_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationAddressText = findViewById(R.id.textView_location);
        locationButton = findViewById(R.id.button_getlocation);
        startButton = findViewById(R.id.button_start);
        stopButton = findViewById(R.id.button_stop);
        testButton = findViewById(R.id.button3);
        mBound = false;
        requestPermissions();


        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mBound) return;
                // Create and send a message to the service, using a supported 'what' value.
                Message msg = Message.obtain(null, LogFileService.MSG_LOCATION, 0, 0);
                try {
                    mService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                locationAddressText.setText("Latitude: " + latitude + "\nLongitude: " + longitude);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Toast.makeText(MainActivity.this, "Location status changed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(MainActivity.this, "Location provider enabled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(MainActivity.this, "Location provider disabled", Toast.LENGTH_SHORT).show();
            }
        };

        mConnection = new ServiceConnection() {
            // Called when the connection with the service is established.
            public void onServiceConnected(ComponentName className, IBinder service) {
                // Because we have bound to an explicit
                // service that is running in our own process, we can
                // cast its IBinder to a concrete class and directly access it.
                mService = new Messenger(service);
                mBound = true;
            }

            // Called when the connection with the service disconnects unexpectedly.
            public void onServiceDisconnected(ComponentName className) {
                Log.e(TAG, "onServiceDisconnected");
                mBound = false;
                mService = null;
            }
        };

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mBound){
                    bindToService();
                }

                // sayHello();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unbindService(mConnection);
            }
        });
    }



    private void requestPermissions(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected, and what
            // features are disabled if it's declined. In this UI, include a
            // "cancel" or "no thanks" button that lets the user continue
            // using your app without granting the permission.
            new AlertDialog.Builder(this)
                    .setTitle("Read/Write Storage Permission")
                    .setMessage("This app requires permission to function properly.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        // Request permission again
                        ActivityCompat.requestPermissions(this, permissions,
                                REQUEST_PERMISSION);
                    }).show();
            ActivityCompat.requestPermissions(this,permissions,REQUEST_PERMISSION);
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            Log.d(TAG, "request permission in request func.");
            ActivityCompat.requestPermissions(this,
                    permissions,
                    REQUEST_PERMISSION);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
        for (int i = 0; i < grantResults.length; i++) {
            // Log.d(TAG, "Granting result for permission: " + permissions[i] + " is " + grantResults[i]);
            if (permissions[i].equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
                Log.d(TAG, "Granting result for permission: " + permissions[i] + " is " + grantResults[i]);
            }
        }

    }
    public void bindToService(){
        // Bind to the service.
        bindService(new Intent(this, LogFileService.class), mConnection,
                Context.BIND_AUTO_CREATE);
        mBound = true;
    }

    public void sayHello() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value.
        Message msg = Message.obtain(null, LogFileService.MSG_SAY_HELLO, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "on Resume function is called");
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        else {
            //requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION);
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private void getCurrentLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                double latitude = lastKnownLocation.getLatitude();
                double longitude = lastKnownLocation.getLongitude();
                locationAddressText.setText("Latitude: " + latitude + "\nLongitude: " + longitude);
            }
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private String getLongitude(){
        return "longitude data";
    }

    private String getLatitude(){
        return "latitude data";
    }

    private String getDistance(){
        return "distance data";
    }

    private String getAveSpeed(){
        return "ave speed data";
    }

    protected void onStop(){
        super.onStop();
        if(mBound){
            unbindService(mConnection);
            mBound = false;
        }
    }
}
