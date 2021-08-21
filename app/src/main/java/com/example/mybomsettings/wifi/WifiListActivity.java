package com.example.mybomsettings.wifi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.example.mybomsettings.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.net.wifi.WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION;
import static android.net.wifi.WifiManager.SUPPLICANT_STATE_CHANGED_ACTION;
import static com.example.mybomsettings.wifi.WifiRAdapter.connectedWiFiPosition;
import static com.example.mybomsettings.wifi.WifiRAdapter.connectingWiFiPosition;

@SuppressLint("LongLogTag")
public class WifiListActivity extends AppCompatActivity {

    private static Context baseContext;
    private static final String TAG = "WifiListActivity MyTag";

    // WiFi 관련 객체들
    IntentFilter intentFilter = new IntentFilter();
    IntentFilter wifiIntentFilter = new IntentFilter();
    WifiManager wifiManager;
    ConnectivityManager connManager;
    NetworkInfo mWifi;

    public static boolean isConnected = false; // Wi-Fi 연결 유무


    // UI
    SwitchMaterial wifiSwitch; // 블루투스 사용 유무 스위치
    Button searchWifiBtn; // WiFi 검색 버튼
    Button addWifiBtn; // WiFi 네트워크 직접 추가 버튼
    Dialog connectDialog; // WiFi 직접 입력해서 연결하는 다이얼로그
    LinearLayout contents; // WiFi on/off에 따라 본문 가릴때 사용
    TextView scanningTV, errorTV; // WiFi 검색중 텍스트, WiFi 꺼져있을때 안내 텍스트
    private static LottieAnimationView lottieAnimationView; //(로딩모양) 검색중 로띠

    RecyclerView recyclerView; // WiFi 목록을 보여주는 Recycler View
    
    // Adapter
    WifiRAdapter wifiRAdapter; // WiFi 목록을 위한 어댑터
    List<WiFi> wifiList;  // WiFi를 담을 리스트


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_list);

        baseContext = getApplicationContext();

        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifi = connManager.getNetworkInfo(TYPE_WIFI);
        initializeAll();

        // Wifi Scan 관련 리시버 등록
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
        // WiFi Connect 관련 리시버 등록
        wifiIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        wifiIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        wifiIntentFilter.addAction(SUPPLICANT_STATE_CHANGED_ACTION);
        wifiIntentFilter.addAction(SUPPLICANT_CONNECTION_CHANGE_ACTION);
        wifiIntentFilter.addAction(WifiManager.EXTRA_SUPPLICANT_ERROR);
        getApplicationContext().registerReceiver(wifiConnectReceiver, wifiIntentFilter);

        wifiSwitch.setOnCheckedChangeListener(new wifiSwitchListener()); // 블루투스 ON/OFF 스위치 리스너
        searchWifiBtn.setOnClickListener(l-> clickWifiScan(searchWifiBtn)); // Wi-Fi 검색 버튼 클릭 리스너
        addWifiBtn.setOnClickListener(l-> clickWifiConnect(addWifiBtn)); // Wi-Fi 직접 추가 버튼 클릭 리스너
    }


    class wifiSwitchListener implements CompoundButton.OnCheckedChangeListener {     // WiFi ON/OFF 스위치 리스너
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked && !checkWifiOnAndConnected()) { // 스위치 ON
                wifiManager.setWifiEnabled(true);
                clickWifiScan(buttonView); // 바로 스캔 시작
                // TODO : 스위치 켰을때 무한 검색하는 문제가 있음.. 진입할 때 자동으로 ON하려는 기능이 원인으로 보임
                //         진입하자마자 검색은 성공 but 이후에 스위치를 키면 오류 발생 (재검색 버튼은 정상동작)
            } else { // 스위치 OFF
                wifiManager.setWifiEnabled(false);
                errorTV.setVisibility(View.VISIBLE);
                contents.setVisibility(View.GONE);
                lottieAnimationView.setVisibility(View.INVISIBLE); //로띠종료*/
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
            if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo =
                        intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected()) { // Wifi is connected
                    // Log.d(TAG, "Wifi is connected: " + String.valueOf(networkInfo));
                    isConnected = true;
                    if (wifiList != null) {
                        String ssid = networkInfo.getExtraInfo();
                        for (WiFi wifi : wifiList) {
                            if (ssid.equals("\""+wifi.getSsid()+"\"")) { // 새로 연결된 경우 텍스트 변경
                                if (connectedWiFiPosition != -1)
                                    wifiList.get(connectedWiFiPosition).setState(WiFi.WIFI_SAVED); // 기존에 연결됨 -> 저장됨
                                wifi.setState(WiFi.WIFI_CONNECTED); // 새롭게 연결된 WIFI 텍스트 변경
                                connectedWiFiPosition = wifiList.indexOf(wifi);
                                break;
                            }
                        }
                        wifiRAdapter.notifyDataSetChanged();
                    }
                }
            } else if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                NetworkInfo networkInfo =
                        intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if(networkInfo != null && (networkInfo.getType() == ConnectivityManager.TYPE_WIFI &&
                        ! networkInfo.isConnected())) { // Wifi is disconnected
                    // Log.d(TAG, "Wifi is disconnected: " + String.valueOf(networkInfo));
                    if (wifiList != null) {
                        String ssid = networkInfo.getExtraInfo();
                        for (WiFi wifi : wifiList) {
                            if (ssid.equals("\""+wifi.getSsid()+"\"")) { // 연결 해제된 경우 텍스트 변경
                                wifi.setState(WiFi.WIFI_SAVED);
                                break;
                            }
                        }
                        wifiRAdapter.notifyDataSetChanged();
                    }
                }
            }
            int supl_error=intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
            if(supl_error == WifiManager.ERROR_AUTHENTICATING) {
                // Log.e(TAG, "ERROR_AUTHENTICATING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                if (connectingWiFiPosition != -1) { // 현재 연결중인 WiFi의 비밀번호가 틀린 경우
                    wifiList.get(connectingWiFiPosition).setState(WiFi.WIFI_AUTH_ERROR);
                    wifiRAdapter.notifyDataSetChanged();
                }
            }
        }
    };


    //버튼을 눌렀을 때
    public void clickWifiScan(View view) {
        boolean success = wifiManager.startScan();
        contents.setVisibility(View.VISIBLE);
        errorTV.setVisibility(View.GONE);
        // 애니메이션
        lottieAnimationView.setVisibility(View.VISIBLE);
        scanningTV.setVisibility(View.VISIBLE);
        setUpAnimation(lottieAnimationView);
        if (!success) {
            scanningTV.setVisibility(View.INVISIBLE);
            lottieAnimationView.setVisibility(View.INVISIBLE); //로띠종료
            // Log.e(TAG, "\"Wifi Scan에 실패하였습니다.");
        }
    }// clickWifiScan()..


    // 네트워크 추가 버튼 클릭
    public void clickWifiConnect(View view) {
        connectDialog = new Dialog(view.getContext());
        connectDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        connectDialog.setContentView(R.layout.activity_wifi_connect_dialog);

        connectDialog.show();
        EditText wifi_name_et = connectDialog.findViewById(R.id.et_dialog_wifi_connect_name);
        Spinner auth_spinner = connectDialog.findViewById(R.id.spinner_dialog_wifi_connect);
        EditText wifi_password_et = connectDialog.findViewById(R.id.et_dialog_wifi_connect_password);
        TextView connect_tv = connectDialog.findViewById(R.id.tv_dialog_wifi_connect_save);
        TextView cancel_tv = connectDialog.findViewById(R.id.tv_dialog_wifi_connect_cancel);
        
        // 보안 프로토콜 타입
        final int[] wifi_auth_tmp = {-1};
        final int WIFI_AUTH_NONE = 0; //  WPA is not used
        final int WIFI_AUTH_WEP = 1; // EAP authentication
        final int WIFI_AUTH_WPA_PSK_OR_WPA2_PSK = 2; // WPA pre-shared key
        final int WIFI_AUTH_IEEE8021X = 3; // IEEE 802.1X
        final int WIFI_AUTH_WPA2_PSK = 4; // WPA2 pre-shared key (@hide 되어있음)

        auth_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // 스피너 리스너
           @Override
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { // 아이템 선택 리스너
               // An item was selected. You can retrieve the selected item using
               // parent.getItemAtPosition(pos)
               // Log.i(TAG, "선택 : "+position+"번, "+parent.getItemAtPosition(position));
               switch(position) {
                   case 1:
                       wifi_auth_tmp[0] = WIFI_AUTH_WEP;
                       break;
                   case 2:
                       wifi_auth_tmp[0] = WIFI_AUTH_WPA_PSK_OR_WPA2_PSK;
                       break;
                   case 3:
                       wifi_auth_tmp[0] = WIFI_AUTH_IEEE8021X;
                       break;
                   default :
                       wifi_auth_tmp[0] = WIFI_AUTH_NONE;
                       break;
               }
           }
           @Override
           public void onNothingSelected(AdapterView<?> parent) { // 아무것도 선택 안된경우 (디폴트 : 없음)
               wifi_auth_tmp[0] = WIFI_AUTH_NONE;
           }
       });

        connect_tv.setOnClickListener(new View.OnClickListener() { // 저장 버튼 클릭
            @Override
            public void onClick(View v) {
                String wifi_name = wifi_name_et.getText().toString();
                String wifi_password = wifi_password_et.getText().toString();
                final int wifi_auth = wifi_auth_tmp[0];

                Log.i(TAG, "이름:"+wifi_name+", 비번:"+wifi_password+", 보안:"+wifi_auth+", wifi_auth_tmp[0]:"+wifi_auth_tmp[0]);
                ///  0. 입력받은 정보 확인
                //   1. 입력받은 정보로 연결 시작
                //   2-1. 연결 성공시 WiFi 목록에 추가
                //   2-2. 연결 실패시 실패 메시지 띄우고 다이얼로그 닫기

                /* 0. 입력받은 정보 확인 */
                if (wifi_name.length() < 1) {
                    AlertDialog.Builder failDialog = new AlertDialog.Builder(v.getContext());
                    failDialog.setTitle("올바른 정보를 입력해주세요.");
                    failDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ;
                        }
                    });
                    failDialog.show();
                    return ;
                }

                /* 1. 연결 시도 */
                WifiConfiguration wifiConfig = new WifiConfiguration(); // Create a WifiConfig
                wifiConfig.SSID = String.format("\"%s\"", wifi_name); //AP Name
                if (wifi_password.length() > 1) { // 암호 있는 경우 설정
                    wifiConfig.preSharedKey = String.format("\"%s\"", wifi_password); //
                }
                
                /*Key Mgmnt*/
                wifiConfig.allowedKeyManagement.clear();
                switch (wifi_auth) {
                    case WIFI_AUTH_WEP:
                        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                        break;
                    case WIFI_AUTH_WPA_PSK_OR_WPA2_PSK :
                        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                        wifiConfig.allowedKeyManagement.set(WIFI_AUTH_WPA2_PSK); // KeyMgmt.WPA_PSK는 @hide 되어있음
                        break;
                    case WIFI_AUTH_IEEE8021X :
                        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
                        break;
                    default :
                        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                }
                /* Create WifiManager */
                WifiManager wifiManager = (WifiManager)baseContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                int netId = wifiManager.addNetwork(wifiConfig); // WiFi manager에 추가해주기
                if (netId == -1) { // 실패
                    Log.e(TAG, "addNetwork() returns -1.");
                    AlertDialog.Builder failDialog = new AlertDialog.Builder(v.getContext());
                    failDialog.setTitle("네트워크 추가를 실패했습니다. 다시 시도해주세요");
                    failDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ;
                        }
                    });
                    failDialog.show();
                    return ;
                }
                else {
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(netId, true); // 실제 Android에 연결 시키기
                    boolean isSucceeded = wifiManager.reconnect();
                    if (isSucceeded && wifiConfig.status == 0) { // 연결 성공시 다이얼로그 닫기
                        Log.i(TAG, "연결성공?,"+"isSucceeded:"+isSucceeded+", status:"+wifiConfig.status+", supplicant state:"+wifiManager.getConnectionInfo().getSupplicantState());
                        AlertDialog.Builder failDialog = new AlertDialog.Builder(v.getContext());
                        failDialog.setTitle("네트워크 추가를 성공했습니다. [재검색]버튼을 눌러 확인해주세요.");
                        failDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ;
                            }
                        });
                        failDialog.show();
                        connectDialog.dismiss();
                    }
                }
            }
        });

        cancel_tv.setOnClickListener(new View.OnClickListener() { // 취소 버튼 클릭
            @Override
            public void onClick(View v) {
                connectDialog.dismiss();
            }
        });

    }

    private void scanSuccess() {    // Wifi검색 성공
        wifiList = new ArrayList<>(); // 전체 WiFi 목록
        List<ScanResult> results = wifiManager.getScanResults(); // 검색된 WiFi 목록들
        List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks(); // 저장된 Wifi 목록들
        WifiInfo wifiInfo = wifiManager.getConnectionInfo(); // 현재 연결된 WiFi 정보
        // Log.i(TAG, "검색된 결과 개수:"+results.size());
        for (ScanResult result : results) {
            if(result.SSID.length() < 1) { // 이름 없으면 skip (왜 뜨는지는 모르겠지만 이름 없는 WiFi가 같이 검색되버림)
                // Log.i(TAG, "SSID 비었음");
                continue;
            }

            String tmpSSID = "\""+result.SSID+"\""; // 비교용으로 따옴표 붙인 SSID
            WiFi wifi = new WiFi(result);

            if (tmpSSID.equals(wifiInfo.getSSID())) { // 지금 연결된 WiFI인 경우
                //Log.i(TAG, "현재 연결되어있음:"+result.SSID);
                wifi.setState(WiFi.WIFI_CONNECTED);
                wifiList.add(0, wifi); // 정보 담은 후 저장 (제일 상위에 넣기)
                continue;
            }

            boolean saved = false;
            for (WifiConfiguration config : configurations) {
                if (tmpSSID.equals(config.SSID)) { // 저장된 WiFi 목록에 있는 경우
                    // Log.i(TAG, "이미 저장되어있음:"+result.SSID+", status:"+config.status);
                    wifi.setState(WiFi.WIFI_SAVED);
                    if (wifiList.size() < 1) {
                        wifiList.add(0, wifi); // 정보 담은 후 저장 (연결된 WiFi없는 경우 최상위에 넣기)
                    } else {
                        wifiList.add(1, wifi); // 정보 담은 후 저장 (상위에 넣기)
                    }
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
        lottieAnimationView.setVisibility(View.INVISIBLE); //로띠종료
        scanningTV.setVisibility(View.INVISIBLE);
    }

    private void scanFailure() {    // Wifi검색 실패
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
        scanningTV.setVisibility(View.INVISIBLE);
        lottieAnimationView.setVisibility(View.INVISIBLE); //로띠종료
//  ... potentially use older scan results ...
        AlertDialog.Builder failDialog = new AlertDialog.Builder(this);
        failDialog.setTitle("WiFi 검색을 실패했습니다. 다시 시도해주세요");
        failDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ;
            }
        });
        failDialog.show();
        return ;
    }

    private void initializeAll() {
        contents = findViewById(R.id.wifi_contents);
        wifiSwitch = findViewById(R.id.switch_wifi);
        recyclerView = findViewById(R.id.recyclerview_wifi);
        searchWifiBtn = findViewById(R.id.btn_wifi_search);
        addWifiBtn = findViewById(R.id.btn_wifi_add);
        lottieAnimationView = findViewById(R.id.lottie_wifi_loading);
        scanningTV = findViewById(R.id.tv_wifi_scanning);
        errorTV = findViewById(R.id.wifi_error_msg);
        
        if (checkWifiOnAndConnected()) { // Wi-Fi 유무에 따라 스위치 초기화
            errorTV.setVisibility(View.GONE);
            contents.setVisibility(View.VISIBLE);
            wifiSwitch.setChecked(true);
            // 바로 스캔 시작
            boolean success = wifiManager.startScan();
            // 애니메이션
            scanningTV.setVisibility(View.VISIBLE);
            lottieAnimationView.setVisibility(View.VISIBLE);
            setUpAnimation(lottieAnimationView);
            if (!success) {
                scanningTV.setVisibility(View.INVISIBLE);
                lottieAnimationView.setVisibility(View.INVISIBLE); //로띠종료
                Log.e(TAG, "\"Wifi Scan에 실패하였습니다.");
            }
        } else {
            wifiSwitch.setChecked(false);
            errorTV.setVisibility(View.VISIBLE);
            contents.setVisibility(View.GONE);
            lottieAnimationView.setVisibility(View.INVISIBLE);
        }

    }

    private void setUpAnimation(LottieAnimationView animview) { //로띠 애니메이션 설정
        //재생할 애니메이션
        animview.setAnimation("lottie_loading.json");
        //반복횟수 지정 : 무한
        animview.setRepeatCount(LottieDrawable.INFINITE); //아니면 횟수 지정
        //시작
        animview.playAnimation();
    }

}