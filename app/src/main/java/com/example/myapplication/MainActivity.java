package com.example.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneTLM;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneUID;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneURL;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final ParcelUuid serviceUid = ParcelUuid.fromString("0000feaa-0000-1000-8000-00805f9b34fb");
    private static final long SCAN_PERIOD = 10000; // stop scanning after 10 seconds
    private static final String TAG = "MainActivity";
    TextView tvDistance;
    TextView tvVoltage;
    TextView tvUrl;
    TextView tvBeaconId;
    TextView tvTemperature;
    private Button startScanButton;
    private boolean scanning = false;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    private static final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT};
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothLeScanner bluetoothLeScanner;

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) throws SecurityException{
            super.onScanResult(callbackType, result);
            if (result.getDevice() != null) {
                if(result.getScanRecord().getServiceUuids() != null) {
                    String deviceIdentifier = result.getScanRecord().getServiceUuids().get(0).getUuid().toString();
                    Log.d(TAG, "Found device: " + deviceIdentifier);
                    if (deviceIdentifier.equals("0000fee0-0000-1000-8000-00805f9b34fb")) {
                        Log.d(TAG, "entering the if statement");
                        Log.d(TAG, Arrays.toString(result.getScanRecord().getBytes()));
                        List<ADStructure> structures =
                                ADPayloadParser.getInstance().parse(result.getScanRecord().getBytes());

                        int rssi = result.getRssi();

                        // taken from: http://darutk-oboegaki.blogspot.com/2015/08/eddystone-android.html
                        for (ADStructure structure : structures) {
                            Log.d(TAG, "structure: " + structure);
                            if (structure instanceof EddystoneUID) {
                                EddystoneUID es = (EddystoneUID) structure;
                                tvDistance.setText(String.valueOf(Math.pow(10.0, (rssi + 66) / -20.0)));
                                tvBeaconId.setText(es.getBeaconIdAsString());
                            } else if (structure instanceof EddystoneURL) {
                                EddystoneURL es = (EddystoneURL) structure;
                                tvUrl.setText(es.getURL().toString());
                            } else if (structure instanceof EddystoneTLM) {
                                EddystoneTLM es = (EddystoneTLM) structure;
                                tvVoltage.setText(es.getBatteryVoltage());
                                tvTemperature.setText(String.valueOf(es.getBeaconTemperature()));
                            }
                        }
                    }
                }
            }
        }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkBluetoothTurnedOn(bluetoothAdapter);
        requestBluetoothPermission();

        this.tvVoltage = findViewById(R.id.voltage);
        this.tvTemperature = findViewById(R.id.temperature);
        this.tvBeaconId = findViewById(R.id.beaconID);
        this.tvUrl = findViewById(R.id.url);
        this.tvDistance = findViewById(R.id.distance);
        this.startScanButton = findViewById(R.id.scan_button);
        this.bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        Log.d(TAG, "bluetoothLeScanner: " + bluetoothLeScanner);


        startScanButton.setOnClickListener(view -> {
            showToast("Scanning for bluetooth device", Toast.LENGTH_SHORT);
            startScanButton.setEnabled(false);
            tvTemperature.setText("scanning...");
            tvBeaconId.setText("scanning...");
            tvDistance.setText("scanning...");
            tvUrl.setText("scanning...");
            tvVoltage.setText("scanning...");
            scanLeDevice();
            requestBluetoothPermission();
        });

        // no need to check if the device itself supports bluetooth because of
        // <uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
        // in manifest

        //IntentFilter filter = new IntentFilter();
        //filter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        //filter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        //filter.addAction(BluetoothLeService.ACTION_TEMPERATURE_UPDATE);
        //filter.addAction(BluetoothLeService.ACTION_HUMIDITY_UPDATE);
        //filter.addAction(BluetoothLeService.ACTION_DENIED_PERMISSION);
        //this.registerReceiver(gattUpdateReceiver, filter);
    }

    private void checkBluetoothTurnedOn(@NonNull BluetoothAdapter bluetoothAdapter) {
       if (!bluetoothAdapter.isEnabled()) {
            // ask user if bluetooth can be turned on
            ActivityResultLauncher<Intent> bluetoothEnableLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            scanLeDevice();
                        }
                    });

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothEnableLauncher.launch(enableBtIntent);
        }
    }


    private void scanLeDevice() {
        Handler handler = new Handler(Looper.myLooper());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) !=
                PackageManager.PERMISSION_GRANTED) {
            requestBluetoothPermission();
        }
            // Source: https://proandroiddev.com/scanning-google-eddystone-in-android-application-cf181e0a8648
            //List<ScanFilter> filters = new ArrayList<> ();
            //filters.add(new ScanFilter.Builder().setDeviceName("F6:B6:2A:79:7B:5D").build());
            //ScanSettings settings = new ScanSettings.Builder()
            //        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            //        .build();

            bluetoothLeScanner.startScan(leScanCallback);
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
            ActivityCompat.requestPermissions(this, permissions, REQUEST_BLUETOOTH_PERMISSION);
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

    private void showToast(String message, Integer length) {
        Toast.makeText(this, message, length).show();
    }

}
