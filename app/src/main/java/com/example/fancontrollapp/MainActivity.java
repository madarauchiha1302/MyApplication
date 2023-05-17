package com.example.fancontrollapp;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public BroadcastReceiver broadcastReceiver;
    private static final String uuid = "00000001-0000-0000-FDFD-FDFDFDFDFDFD";
    // private static final String uuid = "00000002-0000-0000-FDFD-FDFDFDFDFDFD";
    private LeDeviceListAdapter leDeviceListAdapter;
    private TextView textViewConn;
    private Context context;
    private BluetoothLeService bluetoothService;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanSettings scanSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        leDeviceListAdapter = new LeDeviceListAdapter();
        context = this;

        Button connButton = findViewById(R.id.button_conn);
        textViewConn = findViewById(R.id.textView_conn);


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
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };

    }

    // public void ActivityCompat.OnRequestPermissionsResultCallback(){

    // }
    // Device scan callback.
    private ScanCallback leScanCallback =

            new ScanCallback() {


                @Override

                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    Log.d(TAG, "enter scan call back");


                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    // Toast.makeText(context, "Found Device!", Toast.LENGTH_SHORT).show();
                    if(result.getDevice().toString().equals("F8:20:74:F7:2B:82")){
                        Log.d(TAG, "Found Device!");
                        textViewConn.setText("Found Device.");
                        bluetoothLeScanner.stopScan(leScanCallback);
                        leDeviceListAdapter.addDevice(result.getDevice());
                        leDeviceListAdapter.notifyDataSetChanged();
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
    private static final int REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_FINE_LOCATION = 2;
    private static final int REQUEST_BLUETOOTH_CONNECT = 3;


    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });
    private void scanLeDevice() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions

            Log.d(TAG, "No ACCESS_FINE_LOCATION permission!");
            // requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);

        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions

            Log.d(TAG, "No ACCESS_FINE_LOCATION permission!");
            // requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION);

        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            Log.d(TAG, "No ACCESS_FINE_LOCATION permission!");
            // requestPermissions(new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
            requestPermissionLauncher.launch(android.Manifest.permission.BLUETOOTH_CONNECT);

        }
        ArrayList<ScanFilter> scanFilters = new ArrayList<ScanFilter>();
        ParcelUuid parcelUuid = ParcelUuid.fromString(uuid);
        scanFilters.add(new ScanFilter.Builder().setServiceUuid(parcelUuid).build());
        scanSettings = new ScanSettings.Builder().build();

        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        Log.d(TAG, "no permission");
                        return;
                    }
                    bluetoothLeScanner.stopScan( leScanCallback);
                }
            }, SCAN_PERIOD);

            scanning = true;
            Log.d(TAG, "start scan");
            bluetoothLeScanner.startScan(leScanCallback);
            // bluetoothLeScanner.startScan(scanFilters, scanSettings, leScanCallback);
        } else {
            scanning = false;
            Log.d(TAG, "start scan");
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

                String address = leDeviceListAdapter.getDevice(0).getAddress();
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