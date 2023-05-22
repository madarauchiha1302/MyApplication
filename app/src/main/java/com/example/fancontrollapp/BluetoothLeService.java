package com.example.fancontrollapp;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.aware.Characteristics;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service {
    public BluetoothLeService() {
    }

    private static final String uuid = "00000001-0000-0000-FDFD-FDFDFDFDFDFD";
    private static final UUID SERVICE_UUID = UUID.fromString("00000001-0000-0000-FDFD-FDFDFDFDFDFD");
    private static final UUID CHAR_UUID = UUID.fromString("10000001-0000-0000-FDFD-FDFDFDFDFDFD");
    private Binder binder = new LocalBinder();
    private BluetoothGatt bluetoothGatt;

    public static final String TAG = "BluetoothLeService";

    private BluetoothAdapter bluetoothAdapter;

    private Context context;

    public boolean initialize() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        context = this;
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    public boolean connect(final String address) {
        Log.d(TAG, "enter service connect");

        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        BluetoothDevice device;

        try {
            device = bluetoothAdapter.getRemoteDevice(address);
        } catch (IllegalArgumentException exception) {
            Log.w(TAG, "Device not found with provided address.");
            return false;
        }

        // connect to the GATT server on the device
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "No BLUETOOTH_CONNECT permission!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "No BLUETOOTH_CONNECT permission!");
        }

        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);

        return true;
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                Log.d(TAG, "successfully connected to the GATT Server");

                // TODO: broadcast update UI

                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions

                }
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                Log.d(TAG, "disconnected from the GATT Server");
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // TODO: broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                Log.d(TAG, "ACTION_GATT_SERVICES_DISCOVERED");

                BluetoothGattService service = bluetoothGatt.getService(SERVICE_UUID);

                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHAR_UUID);

                    Log.w(TAG, "On SD Characteristics: " + service.getCharacteristic(CHAR_UUID));

                    readCharacteristic(characteristic);

                } else {
                    Log.d(TAG, "service is null");
                }

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        public void onCharacteristicRead(
                BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic,
                int status
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // TODO: broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                Log.d(TAG, "read char success");

                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
//                TurnOnFan();

//                Log.d(TAG, "value To Write: " + WriteValue);
            } else {
                Log.d(TAG, "GATT not success on read.");
            }
        }
    };

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "Func called: read characteristic func started.");
        if (bluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }
        bluetoothGatt.readCharacteristic(characteristic);

    }

    public void writeCharacteristicValue(int value, BluetoothGattCharacteristic characteristic) {
        if (bluetoothGatt != null && characteristic != null) {
            characteristic.setValue(value, BluetoothGattCharacteristic.FORMAT_UINT16,0);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            bluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    public void ControlLed(int WriteValue)
    {
        BluetoothGattService service = bluetoothGatt.getService(SERVICE_UUID);

        if (service != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHAR_UUID);

            writeCharacteristicValue(WriteValue, characteristic);
        }
        else {
            Log.d(TAG, "service is null");
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    private void close() {
        if (bluetoothGatt == null) {
            Log.d(TAG, "no bluetooth Gatt in close.");
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "No BLUETOOTH_CONNECT permission!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "No BLUETOOTH_CONNECT permission!");
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }



}