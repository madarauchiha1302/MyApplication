package com.example.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final long SCAN_PERIOD = 10000; // stop scanning after 10 seconds
    private final BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
    private final BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
    private boolean scanning = true;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    private static final String[] permissions = new String[]{android.Manifest.permission.BLUETOOTH_CONNECT
            , android.Manifest.permission.BLUETOOTH_CONNECT};

    // Device scan callback.
    private final ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    // TODO: do something with this
                    result.getDevice();
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // no need to check if the device itself supports bluetooth because of
        // <uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
        // in manifest

        checkBluetoothTurnedOn();
        scanLeDevice();
    }

    private void checkBluetoothTurnedOn() {
        if (!bluetoothAdapter.isEnabled()) {
            // ask user if bluetooth can be turned on
            // Close the app
            ActivityResultLauncher<Intent> bluetoothEnableLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            Toast.makeText(this, "Scanning for devices...",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "ok :(", Toast.LENGTH_SHORT).show();
                            finish(); // Close the app
                        }
                    });

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothEnableLauncher.launch(enableBtIntent);
        }
    }
    private void scanLeDevice() {
        BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        Handler handler = new Handler(Looper.myLooper());

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                .BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            requestBluetoothPermission(this);
        }

        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(() -> {
                scanning = false;
                bluetoothLeScanner.stopScan(leScanCallback);
            }, SCAN_PERIOD);

            scanning = true;

            UUID desiredUUID = UUID.fromString("00000002-0000-0000-FDFD-FDFDFDFDFDFD");
            ScanFilter scanFilter = new ScanFilter.Builder()
                    .setServiceUuid(new ParcelUuid(desiredUUID))
                    .build();

            List<ScanFilter> scanFilters = new ArrayList<>();
            scanFilters.add(scanFilter);

            // Create the scan settings
            ScanSettings scanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .build();

            bluetoothLeScanner.startScan(scanFilters, scanSettings, leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    static void requestBluetoothPermission(Activity context) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(context,
                android.Manifest.permission.BLUETOOTH_SCAN)
            || ActivityCompat.shouldShowRequestPermissionRationale(context,
                android.Manifest.permission.BLUETOOTH_CONNECT)) {
            // Show an explanation to the user if needed (optional)
            new AlertDialog.Builder(context)
                    .setTitle("Bluetooth Permission")
                    .setMessage("This app requires Bluetooth permission to function properly.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        // Request permission again
                        ActivityCompat.requestPermissions(context, permissions,
                                REQUEST_BLUETOOTH_PERMISSION);
                    }).show();
            // Request permission again
            ActivityCompat.requestPermissions(context, permissions, REQUEST_BLUETOOTH_PERMISSION);
        } else {
            // Request the permission directly
            ActivityCompat.requestPermissions(context, permissions, REQUEST_BLUETOOTH_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                requestBluetoothPermission(this);
            }
        }
    }

}