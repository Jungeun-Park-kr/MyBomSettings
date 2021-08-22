package com.example.mybomsettings.bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.mybomsettings.R;

public class BluetoothDialogActivity extends AppCompatActivity {
    /**
     * 페어링된 블루투스 다이얼로그 액티비티입니다.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_dialog);
    }
}