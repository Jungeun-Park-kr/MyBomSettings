package com.example.mybomsettings.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

public class WiFi {
    public static final int WIFI_NONE = 0;
    public static final int WIFI_CONNECTED = 1;
    public static final int WIFI_SAVED = 2;
    public static final int WIFI_CONNECTING = 3;
    public static final int WIFI_ERROR = 4;
    public static final int WIFI_AUTH_ERROR = 5;

    private ScanResult scanResult;
    private int state; // 해당 와이파이 저장 유무
    private int level; // 신호 강도
    private String ssid; // WiFi SSID (이름)

    public WiFi(ScanResult scanResult) {
        this.scanResult = scanResult;
        this.ssid = scanResult.SSID;
        this.state = WIFI_NONE;
        this.level = WifiManager.calculateSignalLevel(scanResult.level, 4);
    }

    public WiFi(ScanResult scanResult, int state) {
        this.scanResult = scanResult;
        this.ssid = scanResult.SSID;
        this.state = state;
        this.level = WifiManager.calculateSignalLevel(scanResult.level, 4);
    }

    public String getSsid() {return ssid;}

    public void setSsid(String ssid) {this.ssid = ssid;}

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
