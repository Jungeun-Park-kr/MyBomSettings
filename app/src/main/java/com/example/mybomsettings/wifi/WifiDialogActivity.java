package com.example.mybomsettings.wifi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.mybomsettings.R;

public class WifiDialogActivity extends AppCompatActivity {
    /**
     * 현재 연결된 WiFi의 정보를 보여주기 위한 다이얼로그 액티비티입니다.
     *  - 연결상태, 신호강도 등등을 보여줍니다.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_dialog);
    }
}