package com.example.mybomsettings;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.HardwarePropertiesManager;
import android.os.PowerManager;
import android.util.Log;
import android.widget.TextView;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class DeviceInfoListActivity extends AppCompatActivity {

    /**
     * 하드 코딩 된 것 : 모델, Android 버전
     * 직접 구현 된 것 : 배터리 상태, 배터리 수준, IP주소, WiFi맥주소, 블루투스 주소, 빌드번호
     */
    TextView batteryState; // 배터리 상태
    TextView batteryLevel; // 배터리 수준
    TextView ip; //IP주소
    TextView wifiMacAddress; // wifi 맥 주소
    TextView bluetoothAddress; // 블루투스 주소
    TextView buildId; // 빌드번호

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);

        batteryState = findViewById(R.id.device_battery_state);
        batteryLevel = findViewById(R.id.device_battery_level);
        ip = findViewById(R.id.device_ip);
        wifiMacAddress = findViewById(R.id.device_wifi_mac_address);
        bluetoothAddress = findViewById(R.id.device_bluetooth_address);
        buildId = findViewById(R.id.device_bluetooth_buildID);

        batteryState.setText(getBatteryState(this));
        batteryLevel.setText(getBatteryLevel(this));
        ip.setText(getIpAddress());
        wifiMacAddress.setText(getMacAddress());
        bluetoothAddress.setText(getBluetoothMac(this));
        buildId.setText(getBuildID(this));
    }

    private String getIpAddress() {
        // Device에 있는 모든 네트워크에 대해 for문을 돔
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();

                //네트워크 중에서 IP가 할당된 객체에 대해 for문을 또 돔
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {

                    InetAddress inetAddress = enumIpAddr.nextElement();

                    //네트워크에는 항상 Localhost 즉, 루프백(LoopBack)주소가 있으며, 우리가 원하는 것이 아닙니다.
                    //IP는 IPv6와 IPv4가 있습니다.
                    //IPv6의 형태 : fe80::64b9::c8dd:7003
                    //IPv4의 형태 : 123.234.123.123
                    //어떻게 나오는지는 찍어보세요.
                    if(inetAddress.isLoopbackAddress()) {
                        Log.i("IPAddress", intf.getDisplayName() + "(loopback) | " + inetAddress.getHostAddress());
                    }
                    else
                    {
                        Log.i("IPAddress", intf.getDisplayName() + " | " + inetAddress.getHostAddress());
                    }

                    ///루프백이 아니고, IPv4가 맞다면 리턴~~~
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getBatteryState(final Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, ifilter);

        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        // How are we charging?
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        if (isCharging && usbCharge) {
            return "충전 중 (USB 전원)";
        } else if (isCharging && acCharge) {
            return "충전 중 (AC 전원)";
        } else if (isCharging) {
            return "충전 중";
        } else {
            return "충전 안 함";
        }
    }

    private String getBatteryLevel(final Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, ifilter);
        // battery level
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int batteryPct = (int)(level * 100 / (float)scale);
        return String.valueOf(batteryPct)+"%";
    }

    private String getBluetoothMac(final Context context) {

        String result = null;
        if (context.checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH)
                == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Hardware ID are restricted in Android 6+
                // https://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id
                // Getting bluetooth mac via reflection for devices with Android 6+
                result = android.provider.Settings.Secure.getString(context.getContentResolver(),
                        "bluetooth_address");
            } else {
                BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
                result = bta != null ? bta.getAddress() : "";
            }
        }
        return result;
    }


    //getting mac address from mobile
    private String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    // res1.append(Integer.toHexString(b & 0xFF) + ":");
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "";
    }

    private String getBuildID(final Context context) {
        return Build.FINGERPRINT;
    }
}