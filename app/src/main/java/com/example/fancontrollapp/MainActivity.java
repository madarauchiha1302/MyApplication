package com.example.fancontrollapp;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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

    private LeDeviceListAdapter leDeviceListAdapter;
    private TextView textViewConn;
    private Context context;
    private BluetoothLeService bluetoothService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        leDeviceListAdapter = new LeDeviceListAdapter();
        context = this;

        Button connButton = findViewById(R.id.button_conn);
        textViewConn = findViewById(R.id.textView_conn);

        connButton.setOnClickListener(view -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    findDevice();
                }
            });
        });
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };

    }


    // Device scan callback.
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    leDeviceListAdapter.addDevice(result.getDevice());
                    leDeviceListAdapter.notifyDataSetChanged();
                    textViewConn.setText("Found Device.");
                    Toast.makeText(context, "Found Device!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Found Device!");
                    startLeService();
                }
            };

    public void findDevice() {
        BluetoothLeScanner bluetoothLeScanner = ((BluetoothManager) getSystemService(BLUETOOTH_SERVICE))
                .getAdapter().getBluetoothLeScanner();
        ArrayList<ScanFilter> scanFilters = new ArrayList<ScanFilter>();
        ParcelUuid parcelUuid = ParcelUuid.fromString(uuid);
        scanFilters.add(new ScanFilter.Builder().setServiceUuid(parcelUuid).build());

        ScanSettings scanSettings = new ScanSettings.Builder().build();
        // scanLeDevice(scanFilters);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            // return ;
            Toast.makeText(this, "No bluetooth_scan permission!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "No bluetooth_scan permission!");
        }
        bluetoothLeScanner.startScan(scanFilters, scanSettings, leScanCallback);
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
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }



}