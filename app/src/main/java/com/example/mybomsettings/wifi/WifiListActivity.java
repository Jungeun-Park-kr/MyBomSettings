package com.example.mybomsettings.wifi;

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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.example.mybomsettings.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

import static android.net.ConnectivityManager.TYPE_WIFI;

@SuppressLint("LongLogTag")
public class WifiListActivity extends AppCompatActivity {

    private static Context baseContext;
    private static final String TAG = "WifiListActivity MyTag";

    // WiFi
    IntentFilter intentFilter = new IntentFilter();
    IntentFilter wifiIntentFilter = new IntentFilter();
    WifiManager wifiManager;
    ConnectivityManager connManager;
    NetworkInfo mWifi;

    public static boolean isConnected = false;


    // UI
    SwitchMaterial wifiSwitch; // 블루투스 사용 유무 스위치
    RecyclerView recyclerView;
    Button searchWifiBtn;
    Button addWifiBtn;
    LinearLayout contents; // WiFi on/off에 따라 본문 가릴때 사용
    
    // Adapter
    WifiRAdapter wifiRAdapter;
    List<WiFi> wifiList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_list);

        baseContext = getApplicationContext();

        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifi = connManager.getNetworkInfo(TYPE_WIFI);
        initializeAll();

        //Wifi Scan 관련
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
        wifiIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        wifiIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getApplicationContext().registerReceiver(wifiConnectReceiver, wifiIntentFilter);


        wifiSwitch.setOnCheckedChangeListener(new wifiSwitchListener()); // 블루투스 ON/OFF 스위치 리스너

    }


    class wifiSwitchListener implements CompoundButton.OnCheckedChangeListener {     // WiFi ON/OFF 스위치 리스너
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked && !checkWifiOnAndConnected()) { // 스위치 ON
                wifiManager.setWifiEnabled(true);
                contents.setVisibility(View.VISIBLE);

                // 바로 스캔 시작
                boolean success = wifiManager.startScan();
                if (!success) {
                    Log.e(TAG, "\"Wifi Scan에 실패하였습니다.");
                }
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
                try {
                    Thread.sleep(3000);
                    //getApplicationContext().unregisterReceiver(wifiScanReceiver); // 더이상 리시버 받지 않음! <- 이거 없으면 Adapter에 등록되기도 전에 wifiList가 계속 갱신되어 중복 데이터가 저장되어버림
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else { // scan failure handling
                scanFailure();
            }
        }// onReceive()..
    };

    BroadcastReceiver wifiConnectReceiver = new BroadcastReceiver() { // wifi 변경 감지
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); // WifiManager.NETWORK_STATE_CHANGED_ACTION
            /*Log.i(TAG, action);
            NetworkInfo networkInfo =
                    intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(networkInfo.isConnected()) {
                // Wifi is connected
                Log.d(TAG, "Wifi is connected: " + String.valueOf(networkInfo));
                isConnected = true;
            }*/

            if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo =
                        intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected()) {
                    // Wifi is connected
                    Log.d(TAG, "Wifi is connected: " + String.valueOf(networkInfo));
                    isConnected = true;
                }
            } else if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                NetworkInfo networkInfo =
                        intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI &&
                        ! networkInfo.isConnected()) {
                    // Wifi is disconnected
                    Log.d(TAG, "Wifi is disconnected: " + String.valueOf(networkInfo));
                }
            }
        }
    };


    //버튼을 눌렀을 때
    public void clickWifiScan(View view) {
        //getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
        boolean success = wifiManager.startScan();
        if (!success) {
            Log.e(TAG, "\"Wifi Scan에 실패하였습니다.");
        }
    }// clickWifiScan()..



    private void scanSuccess() {    // Wifi검색 성공
        wifiList = new ArrayList<>();
        List<ScanResult> results = wifiManager.getScanResults(); // 검색된 WiFi 목록들
        /*// 중복 방지
        List<ScanResult> raw_wifi_scan_list = wifiManager.getScanResults();
        Set<ScanResult> unique_wifi_scan_set = new HashSet(raw_wifi_scan_list);
        List<ScanResult> results = new ArrayList(unique_wifi_scan_set);*/

        List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks(); // 저장된 Wifi 목록들
        WifiInfo wifiInfo = wifiManager.getConnectionInfo(); // 현재 연결된 WiFi 정보
        Log.i(TAG, "검색된 결과 개수:"+results.size());
        for (ScanResult result : results) {
            if(result.SSID.length() < 1) { // 이름 없으면 skip
                Log.i(TAG, "SSID 비었음");
                continue;
            }

            String tmpSSID = "\""+result.SSID+"\""; // 비교용으로 따옴표 붙인 SSID
            WiFi wifi = new WiFi(result);

            if (tmpSSID.equals(wifiInfo.getSSID())) { // 지금 연결된 WiFI인 경우
                //Log.i(TAG, "현재 연결되어있음:"+result.SSID);
                wifi.setState(WiFi.WIFI_CONNECTED);
                wifiList.add(wifi); // 정보 담은 후 저장
                //scanResults.add(result.SSID); // 중복 확인용으로도 정보 담은 후 리스트에 추가
                continue;
            }

            boolean saved = false;
            for (WifiConfiguration config : configurations) {
                if (tmpSSID.equals(config.SSID)) { // 저장된 WiFi 목록에 있는 경우
                    //Log.i(TAG, "이미 저장되어있음:"+result.SSID);
                    wifi.setState(WiFi.WIFI_SAVED);
                    wifiList.add(wifi); // 정보 담은 후 저장
                    saved = true;
                    break;
                }
            }
            if (saved) // 이미 저장한 경우 skip
                continue;

            wifiList.add(wifi); // 정보 담은 후 저장
        }

        wifiRAdapter = new WifiRAdapter(this, wifiList);
        wifiRAdapter.setHasStableIds(true); // 안깜빡임
        recyclerView.setAdapter(wifiRAdapter);
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
            // 바로 스캔 시작
            boolean success = wifiManager.startScan();
            if (!success) {
                Log.e(TAG, "\"Wifi Scan에 실패하였습니다.");
            }
        } else {
            wifiSwitch.setChecked(false);
        }


    }

}