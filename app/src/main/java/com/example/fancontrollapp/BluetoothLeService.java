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
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.UUID;

public class BluetoothLeService extends Service {
    public BluetoothLeService() {
    }

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

    public void connect(final String address) {
        Log.d(TAG, "enter service connect");

        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return;
        }
        BluetoothDevice device;

        try {
            device = bluetoothAdapter.getRemoteDevice(address);
        } catch (IllegalArgumentException exception) {
            Log.w(TAG, "Device not found with provided address.");
            return;
        }

        // connect to the GATT server on the device
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "No BLUETOOTH_CONNECT permission!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "No BLUETOOTH_CONNECT permission!");
        }

        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);

    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                Log.d(TAG, "successfully connected to the GATT Server");

                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "no BLUETOOTH_CONNECT permission.");
                }
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                Log.d(TAG, "disconnected from the GATT Server");
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                Log.d(TAG, "gatt service discovered.");

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
    };

    public void writeCharacteristicValue(int value, BluetoothGattCharacteristic characteristic) {
        if (bluetoothGatt != null && characteristic != null) {
            characteristic.setValue(value, BluetoothGattCharacteristic.FORMAT_UINT16,0);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "no BLUETOOTH_CONNECT permission.");
                return;
            }
            bluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    public void ChangeFanSpeed(int WriteValue)
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