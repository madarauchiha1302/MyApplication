package com.example.fancontrollapp;

import static android.content.ContentValues.TAG;
import android.Manifest;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;

    private BluetoothDevice device;
    private TextView textViewConn;
    private Context context;
    private BluetoothLeService bluetoothService;
    private BluetoothLeScanner bluetoothLeScanner;


    private static final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT};

    private void requestBluetoothPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.BLUETOOTH_SCAN)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.BLUETOOTH_CONNECT)) {
            // Show an explanation to the user if needed (optional)
            new AlertDialog.Builder(this)
                    .setTitle("Bluetooth Permission")
                    .setMessage("This app requires Bluetooth permission to function properly.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        // Request permission again
                        ActivityCompat.requestPermissions(this, permissions,
                                REQUEST_BLUETOOTH_PERMISSION);
                    }).show();
            // Request permission again
            ActivityCompat.requestPermissions(this, permissions, REQUEST_BLUETOOTH_PERMISSION);
        } else {
            //  if user hit "never show this again" or "deny", still grant permission
            ActivityCompat.requestPermissions(this, permissions, REQUEST_BLUETOOTH_PERMISSION);
            Log.d(TAG, "permissions not granted");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        context = this;

        Button connButton = findViewById(R.id.button_conn);
        textViewConn = findViewById(R.id.textView_conn);

        Button LedMaxButton = findViewById(R.id.button_ledmax);

        Button ledOffButton = findViewById(R.id.button_ledOff);

        Button ledMedButton = findViewById(R.id.button_ledmed);

        Button ledlowButton = findViewById(R.id.button_ledlow);

        requestBluetoothPermission();

        bluetoothLeScanner = ((BluetoothManager) getSystemService(BLUETOOTH_SERVICE))
                .getAdapter().getBluetoothLeScanner();

        connButton.setOnClickListener(view -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // findDevice();
                    scanLeDevice();
                }
            }).start();
        });



        ledOffButton.setOnClickListener(view -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Lef off
                    int WriteValue = 0;
                    bluetoothService.ChangeFanSpeed(WriteValue);
                }
            }).start();
        });

        ledlowButton.setOnClickListener(view -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Intensity low.
                    int WriteValue = 8000;
                    bluetoothService.ChangeFanSpeed(WriteValue);
                }
            }).start();
        });

        ledMedButton.setOnClickListener(view -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Intensity medium
                    int WriteValue = 10000;
                    bluetoothService.ChangeFanSpeed(WriteValue);
                }
            }).start();
        });

        LedMaxButton.setOnClickListener(view -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // High intensity
                    int WriteValue = 20000;
                    bluetoothService.ChangeFanSpeed(WriteValue);
                }
            }).start();
        });



    }

    private ScanCallback leScanCallback =

            new ScanCallback() {


                @Override

                public void onScanResult(int callbackType, ScanResult result) throws SecurityException{
                    super.onScanResult(callbackType, result);
                    Log.d(TAG, "enter scan call back");


                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "no BLUETOOTH_SCAN permission");
                        return;
                    }
                    if(result.getDevice().toString().equals("F8:20:74:F7:2B:82")){
                        Log.d(TAG, "Found Device!");
                        textViewConn.setText("Found Device.");
                        bluetoothLeScanner.stopScan(leScanCallback);
                        device = result.getDevice();
                        startLeService();
                    }

                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    Log.d(TAG, "scann failed." + errorCode);
                }

            };


    private boolean scanning;
    private Handler handler = new Handler();
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private void scanLeDevice() {

        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "no BLUETOOTH_SCAN permission");
                        requestBluetoothPermission();
                        return;
                    }
                    bluetoothLeScanner.stopScan( leScanCallback);
                }
            }, SCAN_PERIOD);
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                requestBluetoothPermission();
            }
            scanning = true;
            Log.d(TAG, "start scan");
            bluetoothLeScanner.startScan(leScanCallback);

        } else {
            scanning = false;
            Log.d(TAG, "stop scan");
            bluetoothLeScanner.stopScan(leScanCallback);

        }
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothService = ((BluetoothLeService.LocalBinder) service).getService();
            if (bluetoothService != null) {
                // call functions on service to check connection and connect to devices
                if (!bluetoothService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }

                String address = device.getAddress();
                bluetoothService.connect(address);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothService = null;
        }
    };
    private void startLeService() {
        Log.d(TAG, "start le service func start");
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


}