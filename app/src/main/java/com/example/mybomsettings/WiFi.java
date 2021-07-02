package com.example.mybomsettings;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

public class WiFi {
    public static final int WIFI_NONE = 0;
    public static final int WIFI_CONNECTED = 1;
    public static final int WIFI_SAVED = 2;

    private ScanResult scanResult;
    private int state; // 해당 와이파이 저장 유무
    private int level; // 신호 강도

    public WiFi(ScanResult scanResult) {
        this.scanResult = scanResult;
        this.state = WIFI_NONE;
        this.level = WifiManager.calculateSignalLevel(scanResult.level, 4);
    }

    public WiFi(ScanResult scanResult, int state) {
        this.scanResult = scanResult;
        this.state = state;
        this.level = WifiManager.calculateSignalLevel(scanResult.level, 4);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public void setScanResult(ScanResult scanResult) {
        this.scanResult = scanResult;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }


}