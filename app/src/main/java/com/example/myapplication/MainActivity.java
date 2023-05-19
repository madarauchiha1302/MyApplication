package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final long SCAN_PERIOD = 10000; // stop scanning after 10 seconds
    private static final String TAG = "MainActivity";
    private TextView temperature;
    private TextView humidity;
    private Button startScanButton;
    private boolean scanning = false;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    private static final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT};
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice device;
    private BluetoothLeService bluetoothLeService;
    private BluetoothLeScanner bluetoothLeScanner;

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) throws SecurityException{
            super.onScanResult(callbackType, result);
            if (result.getDevice() != null) {
                String deviceIdentifier = result.getDevice().toString();
                Log.d(TAG, "Found device: " + deviceIdentifier);
                if (deviceIdentifier.equals("F6:B6:2A:79:7B:5D")) {
                    Log.d(TAG, "Target device found. Stopping scan.");
                    bluetoothLeScanner.stopScan(leScanCallback);
                    scanning = false;
                    device = result.getDevice();
                    startLeService();
                    startScanButton.setVisibility(View.INVISIBLE);
                    temperature.setText("connected!");
                    humidity.setText("connected!");
                }
            } else {
                Log.w(TAG, "onScanResult: Device is null");
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "scan failed. " + errorCode);
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            Log.i(TAG,"onConnectService called.");
            if (bluetoothLeService != null) {
                // call functions on service to check connection and connect to devices
                if (!bluetoothLeService.initialize(bluetoothAdapter)) {
                    Log.d(TAG,"bluetoothLeService.initialize returned null. Closing app.");
                    finish();
                }

                if (!bluetoothLeService.connect(device.getAddress())) {
                    Log.d(TAG,"bluetoothLeService.connect returned null. Closing app.");
                    finish();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothLeService = null;
        }
    };

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                showToast("GATT connection successful.", Toast.LENGTH_LONG);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                showToast("GATT connection has not been successful.", Toast.LENGTH_LONG);
            } else if (BluetoothLeService.ACTION_TEMPERATURE_UPDATE.equals(action)) {
                temperature.setText(String.format("%.2f", (float) intent.getIntExtra(BluetoothLeService.INTENT_TEMPERATURE_EXTRA, 0) / 100f));
            } else if (BluetoothLeService.ACTION_HUMIDITY_UPDATE.equals(action)) {
                humidity.setText(String.format("%.1f", (float) intent.getIntExtra(BluetoothLeService.INTENT_HUMIDITY_EXTRA, 0) / 10f));
            } else if (BluetoothLeService.ACTION_DENIED_PERMISSION.equals(action)) {
                requestBluetoothPermission();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.temperature = findViewById(R.id.temperature);
        this.humidity = findViewById(R.id.humidity);
        this.startScanButton = findViewById(R.id.scan_button);
        this.bluetoothAdapter = ((BluetoothManager) getSystemService(BLUETOOTH_SERVICE)).getAdapter();
        this.bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        startScanButton.setOnClickListener(view -> {
            showToast("Scanning for bluetooth device", Toast.LENGTH_SHORT);
            startScanButton.setEnabled(false);
            temperature.setText("scanning...");
            humidity.setText("scanning...");
            scanLeDevice();
        });

        // no need to check if the device itself supports bluetooth because of
        // <uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
        // in manifest

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        filter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        filter.addAction(BluetoothLeService.ACTION_TEMPERATURE_UPDATE);
        filter.addAction(BluetoothLeService.ACTION_HUMIDITY_UPDATE);
        filter.addAction(BluetoothLeService.ACTION_DENIED_PERMISSION);
        this.registerReceiver(gattUpdateReceiver, filter);

        requestBluetoothPermission();
        checkBluetoothTurnedOn(bluetoothAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(gattUpdateReceiver);
        this.unbindService(serviceConnection);
    }

    private void checkBluetoothTurnedOn(BluetoothAdapter bluetoothAdapter) {
        // TODO: this code crashes the app when the user clicks the scan button while bluetooth
        //  is turned off
        if (!bluetoothAdapter.isEnabled()) {
            // ask user if bluetooth can be turned on
            ActivityResultLauncher<Intent> bluetoothEnableLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            scanLeDevice();
                        } else {
                            temperature.setText("turn bluetooth on and restart the app");
                            humidity.setText("turn bluetooth on and restart the app");
                            startScanButton.setEnabled(false);
                        }
                    });

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothEnableLauncher.launch(enableBtIntent);
        }
    }

    private void scanLeDevice() {
        Handler handler = new Handler(Looper.myLooper());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            requestBluetoothPermission();
        }

        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(() -> {
                // This should not be called when target device was found (see onScanResult).
                if (scanning) {
                    scanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                    showToast("No device found, scan stopped.", Toast.LENGTH_LONG);
                    startScanButton.setEnabled(true);
                    temperature.setText("device_not_connected");
                    humidity.setText("device_not_connected");
                }
            }, SCAN_PERIOD);

            scanning = true;

            ScanFilter scanFilter = new ScanFilter.Builder().setDeviceName("IPVSWeather").build();

            List<ScanFilter> scanFilters = new ArrayList<>();
            scanFilters.add(scanFilter);

            // Create the scan settings
            ScanSettings scanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .build();

            // bluetoothLeScanner.startScan(scanFilters, scanSettings, leScanCallback);
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

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
            // TODO
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                requestBluetoothPermission();
            }
        }
    }

    private void startLeService() {
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.i(TAG, "Bound with service.");
    }

    private void showToast(String message, Integer length) {
        Toast.makeText(this, message, length).show();
    }

}