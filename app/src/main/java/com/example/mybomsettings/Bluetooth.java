package com.example.mybomsettings;

import android.bluetooth.BluetoothDevice;

public class Bluetooth {

    private BluetoothDevice device; // 블루투스 기기
    private CharSequence name; // 블루투스 기기 이름
    private CharSequence address; // 블루투스 MAC 주소

    public Bluetooth(BluetoothDevice device) {
        this.device = device;
        this.name = device.getName();
        this.address = device.getAddress();
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public CharSequence getName() {
        return name;
    }

    public void setName(CharSequence name) {
        this.name = name;
    }

    public CharSequence getAddress() {
        return address;
    }

    public void setAddress(CharSequence address) {
        this.address = address;
    }

}
