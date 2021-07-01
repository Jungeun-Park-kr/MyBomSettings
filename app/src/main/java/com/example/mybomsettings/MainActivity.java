package com.example.mybomsettings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

//    static final String[] SETTINGS_MENU = {"시스템 설정", "디스플레이", "Wi-Fi", "블루투스", "날짜 및 시간", "휴대전화 정보"};
static final String[] SETTINGS_MENU = {"시스템 설정", "Wi-Fi", "블루투스", "날짜 및 시간", "휴대전화 정보"};
    private static final String TAG = "MainActivity MyTag";
    private final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION=1; // 블루투스 권한
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck != getPackageManager().PERMISSION_GRANTED) {
            // ask permissions here using below code
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
        }
        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
           // Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "BLE 지원안됨");
            finish();
        } else {
            Log.e(TAG, "BLE 지원됨");
        }

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, SETTINGS_MENU) ;

        ListView listview = (ListView) findViewById(R.id.settingsListView) ;
        listview.setAdapter(adapter) ;

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {

                // get TextView's Text.
                String strText = (String) parent.getItemAtPosition(position) ;

                //Toast.makeText(v.getContext(), strText+", position: "+position, Toast.LENGTH_SHORT).show();
                // TODO : use strText
                switch(position) {
                    case 0: // 소리 -> 시스템 설정
                        settingSystem();
                        break;
                    case 1: // Wi-Fi
                         settingWiFi();
                        break;
                    case 2: // 블루투스
                         settingBluetooth();
                        break;
                    case 3: // 날짜 및 시간
                        settingTime();
                        break;
                    case 4: // 휴대전화 정보
                        settingDeviceInfo();
                        break;
                }
            }
        }) ;


        NotificationManager notificationManager =
                (NotificationManager)getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !notificationManager.isNotificationPolicyAccessGranted()) {
            Log.e("MyTag:", "권한 없음");
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }

        /*// Bluetooth Service 시작
        Intent intent = new Intent(this, BluetoothService.class);
        startService(intent);*/

    }

    public void settingDisplay() { // 디스플레이 설정
        Intent displayIntent = new Intent(getApplicationContext(), DisplayList.class);
        startActivity(displayIntent);
    }

    public void settingSystem() { // 시스템 세팅 (디스플레이, 소리)
        Intent systemIntent = new Intent(getApplicationContext(), SystemList.class);
        startActivity(systemIntent);
    }

    public void settingBluetooth() {
        Intent bluetoothIntent = new Intent(getApplicationContext(), BluetoothList.class); //BluetoothList
        startActivity(bluetoothIntent);
//        startActivity( new Intent( Settings.ACTION_BLUETOOTH_SETTINGS ));

        /*Dialog dilaog01 = new Dialog(MainActivity.this);       // Dialog 초기화
        dilaog01.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dilaog01.setContentView(R.layout.dialog_bluetooth_search);             // xml 레이아웃 파일과 연결
        dilaog01.show();*/
    }

    public void settingWiFi() {
//        startActivity( new Intent( Settings.ACTION_WIFI_SETTINGS ));
        Intent wifiIntent = new Intent(getApplicationContext(), WifiList.class); //BluetoothList
        startActivity(wifiIntent);
    }

    public void settingDeviceInfo() {
        startActivity( new Intent( Settings.ACTION_DEVICE_INFO_SETTINGS ));
    }

    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == getPackageManager().PERMISSION_GRANTED) {
                    Log.e(TAG, "Permission Granted!");
                } else {
                    Log.e(TAG,"Permission Denied!");
                }
        }
    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }

    public void settingTime() {
        startActivity( new Intent( Settings.ACTION_DATE_SETTINGS ));
    }
}