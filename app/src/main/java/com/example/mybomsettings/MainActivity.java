package com.example.mybomsettings;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    static final String[] SETTINGS_MENU = {"시스템 설정", "디스플레이", "Wi-Fi", "블루투스", "날짜 및 시간", "휴대전화 정보"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                         settingSound();
                        break;
                    case 1: // 디스플레이
                        settingDisplay();
                        break;
                    case 2: // Wi-Fi
                        // settingWiFi();
                        break;
                    case 3: // 블루투스
                        // settingBluetooth();
                        break;
                    case 4: // 날짜 및 시간
//                        settingTime();
                        break;
                    case 5: // 휴대전화 정보
//                        settingInfo();
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
    }

    public void settingDisplay() { // 디스플레이 설정
        Intent displayIntent = new Intent(getApplicationContext(), DisplayList.class);
        startActivity(displayIntent);
    }

    public void settingSound() {
        Intent displayIntent = new Intent(getApplicationContext(), SoundList.class);
        startActivity(displayIntent);
    }
}