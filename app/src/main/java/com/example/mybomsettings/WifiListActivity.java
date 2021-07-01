package com.example.mybomsettings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

@SuppressLint("LongLogTag")
public class WifiListActivity extends AppCompatActivity {

    private static Context baseContext;
    private static final String TAG = "WifiListActivity MyTag";

    // WiFi
    IntentFilter intentFilter = new IntentFilter();
    WifiManager wifiManager;
    ConnectivityManager connManager;
    NetworkInfo mWifi;
    // UI
    SwitchMaterial wifiSwitch; // 블루투스 사용 유무 스위치
    RecyclerView recyclerView;
    Button searchWifiBtn;
    Button addWifiBtn;
    LinearLayout contents; // WiFi on/off에 따라 본문 가릴때 사용
    
    // Adapter
    WifiRAdapter wifiRAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_list);

        baseContext = getApplicationContext();

        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        initializeAll();

        //Wifi Scan 관련
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);

        wifiSwitch.setOnCheckedChangeListener(new wifiSwitchListener()); // 블루투스 ON/OFF 스위치 리스너

    }


    class wifiSwitchListener implements CompoundButton.OnCheckedChangeListener {     // WiFi ON/OFF 스위치 리스너
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked && !checkWifiOnAndConnected()) { // 스위치 ON
                wifiManager.setWifiEnabled(true);
                contents.setVisibility(View.VISIBLE);
            } else { // 스위치 OFF
                wifiManager.setWifiEnabled(false);
                contents.setVisibility(View.GONE);
            }
        }
    }

    private boolean checkWifiOnAndConnected() { // Wi-Fi ON/OFF 확인
        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) { // Wi-Fi adapter is ON
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if( wifiInfo.getNetworkId() == -1 ){
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        }
        else {
            return false; // Wi-Fi adapter is OFF
        }
    }

    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {   // wifiManager.startScan(); 시  발동되는 메소드 ( 예제에서는 버튼을 누르면 startScan()을 했음. )
            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false); //스캔 성공 여부 값 반환
            if (success) {
                scanSuccess();
            } else { // scan failure handling
                scanFailure();
            }
        }// onReceive()..
    };

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
        }
    };

    //버튼을 눌렀을 때
    public void clickWifiScan(View view) {
        boolean success = wifiManager.startScan();
        if (!success) {
            Log.e(TAG, "\"Wifi Scan에 실패하였습니다.");
        }
    }// clickWifiScan()..

    private void scanSuccess() {    // Wifi검색 성공
        List<ScanResult> results = wifiManager.getScanResults(); // 검색된 WiFi 목록들
//        Log.i(TAG, "test)"+results.get(0).toString());


        /*List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks(); // 저장된 Wifi 목록들
        Log.i(TAG, "test) ssid:"+configurations.get(0).SSID+", priority:"+configurations.get(0).priority);*/

        /*mAdapter=new MyAdapter(results);
        recyclerView.setAdapter(mAdapter);*/
    }

    private void scanFailure() {    // Wifi검색 실패
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
//  ... potentially use older scan results ...
    }

    private void initializeAll() {
        contents = findViewById(R.id.wifi_contents);
        wifiSwitch = findViewById(R.id.switch_wifi);
        recyclerView = findViewById(R.id.recyclerview_wifi);
        searchWifiBtn = findViewById(R.id.btn_wifi_search);
        addWifiBtn = findViewById(R.id.btn_wifi_add);
        
        if (checkWifiOnAndConnected()) { // Wi-Fi 유무에 따라 스위치 초기화
            wifiSwitch.setChecked(true);
        } else {
            wifiSwitch.setChecked(false);
        }
    }
}