package com.example.mybomsettings.bluetooth;

import android.bluetooth.BluetoothDevice;

public class Bluetooth {
    /**
     * 등록된 디바이스 목록을 관리하기 위한 Bluetooth 객체
     *  - device : 블루투스 디바이스 객체
     *  - name : 블루투스 기기 이름
     *  - address : 블루투스 MAC 주소
     *  - connected : 현재 연결된 기기인지 여부 (true/false)
     */
    private BluetoothDevice device; // 블루투스 기기
    private CharSequence name; // 블루투스 기기 이름
    private CharSequence address; // 블루투스 MAC 주소

    private Boolean connected; // 연결 유무

    public Bluetooth(BluetoothDevice device) {
        this.device = device;
        this.name = device.getName();
        this.address = device.getAddress();
        this.connected = false;
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

    public Boolean getConnected() {
        return connected;
    }

    public void setConnected(Boolean connected) {
        this.connected = connected;
    }

}
