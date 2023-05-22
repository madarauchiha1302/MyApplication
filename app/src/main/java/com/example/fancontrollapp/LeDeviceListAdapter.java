package com.example.fancontrollapp;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import java.util.ArrayList;

public class LeDeviceListAdapter {
    private ArrayList<BluetoothDevice> mLeDevices;

    public LeDeviceListAdapter(){
        mLeDevices = new ArrayList<BluetoothDevice>();

    }

    public void addDevice(BluetoothDevice device){
        mLeDevices.add(device);
    }

    public BluetoothDevice getDevice(int index){

        return mLeDevices.get(index);
    }

    public void notifyDataSetChanged(){

    }
}
