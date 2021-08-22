package com.example.mybomsettings.wifi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.mybomsettings.R;

public class WifiConnectDialogActivity extends AppCompatActivity {
    /**
     * WiFi 네트워크를 직접 추가할 때 사용하는 연결 다이얼로그 액티비티입니다.
     * SSID, 보안 프로토콜 등을 입력받습니다.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_connect_dialog);
    }
}