package com.example.myapplication;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.UUID;

public class BluetoothLeService extends Service {

    private static final UUID SERVICE_UUID = UUID.fromString("00000002-0000-0000-FDFD-FDFDFDFDFDFD");
    private static final UUID TEMPERATURE_UUID = UUID.fromString("00002A1C-0000-1000-8000-00805F9B34FB");
    private static final UUID HUMIDITY_UUID = UUID.fromString("00002A6F-0000-1000-8000-00805F9B34FB");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
    public static final String INTENT_TEMPERATURE_EXTRA = "temperature";
    public static final String INTENT_HUMIDITY_EXTRA = "humidity";
    private final Binder binder = new LocalBinder();
    private BluetoothGatt bluetoothGatt;
    public static final String ACTION_GATT_CONNECTED = "com.example.myapplication.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "com.example.myapplication.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_TEMPERATURE_UPDATE = "com.example.myapplication.ACTION_TEMPERATURE_UPDATE";
    public static final String ACTION_HUMIDITY_UPDATE = "com.example.myapplication.ACTION_HUMIDITY_UPDATE";
    public static final String ACTION_DENIED_PERMISSION = "com.example.myapplication.ACTION_PERMISSION_DENIED";
    public static final String TAG = "BluetoothLeService";

    private BluetoothAdapter bluetoothAdapter;

    public boolean initialize(BluetoothAdapter bluetoothAdapter) {
        Log.d(TAG, "called initialize");
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        this.bluetoothAdapter = bluetoothAdapter;
        return true;
    }

    protected boolean connect(final String address) {
        Log.d(TAG, "called connect");

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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            sendBroadcast(new Intent(ACTION_DENIED_PERMISSION));
            return false;
        }
        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
        if(bluetoothGatt == null) {
            Log.w(TAG, "device.connectGatt returned null!");
        }
        return true;
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "called onConnectionStateChange");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                Log.d(TAG, "successfully connected to the GATT Server");
                sendBroadcast(new Intent(ACTION_GATT_CONNECTED));
                // results are available in onServicesDiscovered
                try {
                    bluetoothGatt.discoverServices();
                    Log.d(TAG, "called discoverServices");
                } catch (SecurityException e) {
                    sendBroadcast(new Intent(ACTION_DENIED_PERMISSION));
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                Log.d(TAG, "disconnected from the GATT Server");
                sendBroadcast(new Intent(ACTION_GATT_DISCONNECTED));
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "called onServicesDiscovered");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "ACTION_GATT_SERVICES_DISCOVERED");

                BluetoothGattService weatherService = bluetoothGatt.getService(SERVICE_UUID);
                if (weatherService != null) {
                    BluetoothGattCharacteristic temperatureCharacteristic = weatherService.getCharacteristic(TEMPERATURE_UUID);
                    BluetoothGattCharacteristic humidityCharacteristic = weatherService.getCharacteristic(HUMIDITY_UUID);
                    readAndNotify(temperatureCharacteristic);
                    readAndNotify(humidityCharacteristic);
                } else {
                    Log.d(TAG, "weatherService is null");
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "called onCharacteristicRead");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(characteristic);
            } else {
                Log.d(TAG, "GATT not success on read.");
            }
        }

        @Override
        public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
            Log.d(TAG, "called onCharacteristicChanged");
            broadcastUpdate(characteristic);
        }
    };

    private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "Func called: read characteristic func started.");
        if (bluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return;
        }
        // response is delivered through the BluetoothGattCallback's onCharacteristicRead() method

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            sendBroadcast(new Intent(ACTION_DENIED_PERMISSION));
            return;
        }
        bluetoothGatt.readCharacteristic(characteristic);
    }

    private void readAndNotify(BluetoothGattCharacteristic characteristic) {
        readCharacteristic(characteristic);
        setCharacteristicNotification(characteristic, true);
    }

    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) throws SecurityException {
        Log.d(TAG, "called setCharacteristicNotification");
        if (bluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return;
        }

        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "called onBind.");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "called onUnbind.");
        close();
        return super.onUnbind(intent);
    }

    private void close() {
        if (bluetoothGatt == null) {
            Log.d(TAG, "no bluetooth Gatt in close.");
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            sendBroadcast(new Intent(ACTION_DENIED_PERMISSION));
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    private void broadcastUpdate(final BluetoothGattCharacteristic characteristic) {
        Intent intent = new Intent();
        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (TEMPERATURE_UUID.equals(characteristic.getUuid())) {
            intent.setAction(ACTION_TEMPERATURE_UPDATE);
            final int temperature = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1);
            intent.putExtra(INTENT_TEMPERATURE_EXTRA, temperature);
        } else if (HUMIDITY_UUID.equals(characteristic.getUuid())) {
            intent.setAction(ACTION_HUMIDITY_UPDATE);
            final int humidity = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
            intent.putExtra(INTENT_HUMIDITY_EXTRA, humidity);
        }
        sendBroadcast(intent);
        Log.d(TAG, "sent broadcast for a characteristic in broadcastUpdate");
    }

    class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
}