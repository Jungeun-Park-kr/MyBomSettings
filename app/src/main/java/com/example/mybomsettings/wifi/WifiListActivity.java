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

    // WiFi
    IntentFilter intentFilter = new IntentFilter();
    IntentFilter wifiIntentFilter = new IntentFilter();
    WifiManager wifiManager;
    ConnectivityManager connManager;
    NetworkInfo mWifi;

    public static boolean isConnected = false; // Wi-Fi ?????? ??????


    // UI
    SwitchMaterial wifiSwitch; // ???????????? ?????? ?????? ?????????
    RecyclerView recyclerView;
    Button searchWifiBtn;
    Button addWifiBtn;
    Dialog connectDialog; // WiFi ?????? ???????????? ???????????? ???????????????
    LinearLayout contents; // WiFi on/off??? ?????? ?????? ????????? ??????
    TextView scanningTV, errorTV; // WiFi ????????? ?????????, WiFi ??????????????? ?????? ?????????
    private static LottieAnimationView lottieAnimationView; //(????????????) ????????? ??????
    
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

        // Wifi Scan ?????? ?????????
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
        // WiFi Connect ?????? ?????????
        wifiIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        wifiIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        wifiIntentFilter.addAction(SUPPLICANT_STATE_CHANGED_ACTION);
        wifiIntentFilter.addAction(SUPPLICANT_CONNECTION_CHANGE_ACTION);
        wifiIntentFilter.addAction(WifiManager.EXTRA_SUPPLICANT_ERROR);
        getApplicationContext().registerReceiver(wifiConnectReceiver, wifiIntentFilter);

        wifiSwitch.setOnCheckedChangeListener(new wifiSwitchListener()); // ???????????? ON/OFF ????????? ?????????
        searchWifiBtn.setOnClickListener(l-> clickWifiScan(searchWifiBtn)); // Wi-Fi ?????? ?????? ?????? ?????????
        addWifiBtn.setOnClickListener(l-> clickWifiConnect(addWifiBtn)); // Wi-Fi ?????? ?????? ?????? ?????? ?????????
    }


    class wifiSwitchListener implements CompoundButton.OnCheckedChangeListener {     // WiFi ON/OFF ????????? ?????????
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked && !checkWifiOnAndConnected()) { // ????????? ON
                wifiManager.setWifiEnabled(true);
                Log.e(TAG, "?????????ON ??????");
                clickWifiScan(buttonView); // ?????? ?????? ??????
            } else { // ????????? OFF
                wifiManager.setWifiEnabled(false);
                errorTV.setVisibility(View.VISIBLE);
                contents.setVisibility(View.GONE);
                lottieAnimationView.setVisibility(View.INVISIBLE); //????????????*/
            }
        }
    }

    private boolean checkWifiOnAndConnected() { // Wi-Fi ON/OFF ??????
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
        public void onReceive(Context c, Intent intent) {   // wifiManager.startScan(); ???  ???????????? ????????? ( ??????????????? ????????? ????????? startScan()??? ??????. )
            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false); //?????? ?????? ?????? ??? ??????
            if (success) {
                scanSuccess();
                try {
                    Thread.sleep(3000);
                    //getApplicationContext().unregisterReceiver(wifiScanReceiver); // ????????? ????????? ?????? ??????! <- ?????? ????????? Adapter??? ??????????????? ?????? wifiList??? ?????? ???????????? ?????? ???????????? ??????????????????
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else { // scan failure handling
                scanFailure();
            }
        }// onReceive()..
    };

    BroadcastReceiver wifiConnectReceiver = new BroadcastReceiver() { // wifi ?????? ??????
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); // WifiManager.NETWORK_STATE_CHANGED_ACTION
            if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo =
                        intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected()) {
                    // Wifi is connected
                    Log.d(TAG, "Wifi is connected: " + String.valueOf(networkInfo));
                    isConnected = true;
                    if (wifiList != null) {
                        String ssid = networkInfo.getExtraInfo();
                        for (WiFi wifi : wifiList) {
                            if (ssid.equals("\""+wifi.getSsid()+"\"")) { // ?????? ????????? ?????? ????????? ??????
                                //Log.i(TAG, "connectedWiFiPosition :"+connectedWiFiPosition+", newConnectWifiPosition:"+wifiList.indexOf(wifi));
                                if (connectedWiFiPosition != -1)
                                    wifiList.get(connectedWiFiPosition).setState(WiFi.WIFI_SAVED); // ????????? ????????? -> ?????????
                                wifi.setState(WiFi.WIFI_CONNECTED); // ????????? ????????? WIFI ????????? ??????
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
                        ! networkInfo.isConnected())) {
                    // Wifi is disconnected
                    Log.d(TAG, "Wifi is disconnected: " + String.valueOf(networkInfo));
                    if (wifiList != null) {
                        String ssid = networkInfo.getExtraInfo();
                        for (WiFi wifi : wifiList) {
//                            Log.d(TAG, "????????? :"+wifi.getSsid()+" - "+ssid);
                            if (ssid.equals("\""+wifi.getSsid()+"\"")) { // ?????? ????????? ?????? ????????? ??????
                                wifi.setState(WiFi.WIFI_SAVED);
                                break;
                            }
                        }
                        wifiRAdapter.notifyDataSetChanged();
                    }
                }
            } else if (action.equals(SUPPLICANT_STATE_CHANGED_ACTION)) {
               // Log.d(TAG, "Wifi state changed");
            } else if (action.equals(SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
               // Log.d(TAG, "Wifi connection changed");
            }
            int supl_error=intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
            if(supl_error == WifiManager.ERROR_AUTHENTICATING) {
                Log.e(TAG, "ERROR_AUTHENTICATING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                if (connectingWiFiPosition != -1) { // ?????? ???????????? WiFi??? ??????????????? ?????? ??????
                    Log.i(TAG, "?????? ???????????? ????????? ??????");
                    wifiList.get(connectingWiFiPosition).setState(WiFi.WIFI_AUTH_ERROR);
                    wifiRAdapter.notifyDataSetChanged();
                }
            }
        }
    };


    //????????? ????????? ???
    public void clickWifiScan(View view) {
        //getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
        Log.e(TAG, "WiFi ?????? ??????");
        boolean success = wifiManager.startScan();
        contents.setVisibility(View.VISIBLE);
        errorTV.setVisibility(View.GONE);
        // ???????????????
        lottieAnimationView.setVisibility(View.VISIBLE);
        scanningTV.setVisibility(View.VISIBLE);
        setUpAnimation(lottieAnimationView);
        if (!success) {
            scanningTV.setVisibility(View.INVISIBLE);
            lottieAnimationView.setVisibility(View.INVISIBLE); //????????????
            Log.e(TAG, "\"Wifi Scan??? ?????????????????????.");
        }
    }// clickWifiScan()..


    // ?????? ?????? ?????? ??????
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
        
        // ?????? ???????????? ??????
        final int[] wifi_auth_tmp = {-1};
        final int WIFI_AUTH_NONE = 0; //  WPA is not used
        final int WIFI_AUTH_WEP = 1; // EAP authentication
        final int WIFI_AUTH_WPA_PSK_OR_WPA2_PSK = 2; // WPA pre-shared key
        final int WIFI_AUTH_IEEE8021X = 3; // IEEE 802.1X
        final int WIFI_AUTH_WPA2_PSK = 4; // WPA2 pre-shared key (@hide ????????????)

        auth_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // ????????? ?????????
           @Override
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { // ????????? ?????? ?????????
               // An item was selected. You can retrieve the selected item using
               // parent.getItemAtPosition(pos)
               Log.i(TAG, "?????? : "+position+"???, "+parent.getItemAtPosition(position));
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
           public void onNothingSelected(AdapterView<?> parent) { // ???????????? ?????? ???????????? (????????? : ??????)
               wifi_auth_tmp[0] = WIFI_AUTH_NONE;
           }
       });

//        Log.i(TAG, "??????:"+wifi_name+", ??????:"+wifi_password+", ??????:"+wifi_security);

        connect_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String wifi_name = wifi_name_et.getText().toString();
                String wifi_password = wifi_password_et.getText().toString();
                final int wifi_auth = wifi_auth_tmp[0];

                Log.i(TAG, "??????:"+wifi_name+", ??????:"+wifi_password+", ??????:"+wifi_auth+", wifi_auth_tmp[0]:"+wifi_auth_tmp[0]);
                ///  0. ???????????? ?????? ??????
                //   1. ???????????? ????????? ?????? ??????
                //   2-1. ?????? ????????? WiFi ????????? ??????
                //   2-2. ?????? ????????? ?????? ????????? ????????? ??????????????? ??????

                /* 0. ???????????? ?????? ?????? */
                if (wifi_name.length() < 1) {
                    AlertDialog.Builder failDialog = new AlertDialog.Builder(v.getContext());
                    failDialog.setTitle("????????? ????????? ??????????????????.");
                    failDialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ;
                        }
                    });
                    failDialog.show();
                    return ;
                }

                /* 1. ?????? ?????? */
                WifiConfiguration wifiConfig = new WifiConfiguration(); // Create a WifiConfig
                wifiConfig.SSID = String.format("\"%s\"", wifi_name); //AP Name
                if (wifi_password.length() > 1) { // ?????? ?????? ?????? ??????
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
                        wifiConfig.allowedKeyManagement.set(WIFI_AUTH_WPA2_PSK); // KeyMgmt.WPA_PSK??? @hide ????????????
                        break;
                    case WIFI_AUTH_IEEE8021X :
                        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
                        break;
                    default :
                        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                }
                /* Create WifiManager */
                WifiManager wifiManager = (WifiManager)baseContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                int netId = wifiManager.addNetwork(wifiConfig); // WiFi manager??? ???????????????
                if (netId == -1) { // ??????
                    Log.e(TAG, "addNetwork() returns -1.");
                    AlertDialog.Builder failDialog = new AlertDialog.Builder(v.getContext());
                    failDialog.setTitle("???????????? ????????? ??????????????????. ?????? ??????????????????");
                    failDialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
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
                    wifiManager.enableNetwork(netId, true); // ?????? Android??? ?????? ?????????
                    boolean isSucceeded = wifiManager.reconnect();
                    if (isSucceeded && wifiConfig.status == 0) { // ?????? ????????? ??????????????? ??????
                        Log.i(TAG, "?????????????,"+"isSucceeded:"+isSucceeded+", status:"+wifiConfig.status+", supplicant state:"+wifiManager.getConnectionInfo().getSupplicantState());
                        AlertDialog.Builder failDialog = new AlertDialog.Builder(v.getContext());
                        failDialog.setTitle("???????????? ????????? ??????????????????. [?????????]????????? ?????? ??????????????????.");
                        failDialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
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

        cancel_tv.setOnClickListener(new View.OnClickListener() { // ?????? ?????? ??????
            @Override
            public void onClick(View v) {
                connectDialog.dismiss();
            }
        });

    }

    private void scanSuccess() {    // Wifi?????? ??????
        wifiList = new ArrayList<>(); // ?????? WiFi ??????
        List<ScanResult> results = wifiManager.getScanResults(); // ????????? WiFi ?????????
        List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks(); // ????????? Wifi ?????????
        WifiInfo wifiInfo = wifiManager.getConnectionInfo(); // ?????? ????????? WiFi ??????
        // Log.i(TAG, "????????? ?????? ??????:"+results.size());
        for (ScanResult result : results) {
            if(result.SSID.length() < 1) { // ?????? ????????? skip (??? ???????????? ??????????????? ?????? ?????? WiFi??? ?????? ???????????????)
                // Log.i(TAG, "SSID ?????????");
                continue;
            }

            String tmpSSID = "\""+result.SSID+"\""; // ??????????????? ????????? ?????? SSID
            WiFi wifi = new WiFi(result);

            if (tmpSSID.equals(wifiInfo.getSSID())) { // ?????? ????????? WiFI??? ??????
                //Log.i(TAG, "?????? ??????????????????:"+result.SSID);
                wifi.setState(WiFi.WIFI_CONNECTED);
                wifiList.add(0, wifi); // ?????? ?????? ??? ?????? (?????? ????????? ??????)
                continue;
            }

            boolean saved = false;
            for (WifiConfiguration config : configurations) {
                if (tmpSSID.equals(config.SSID)) { // ????????? WiFi ????????? ?????? ??????
                    // Log.i(TAG, "?????? ??????????????????:"+result.SSID+", status:"+config.status);
                    wifi.setState(WiFi.WIFI_SAVED);
                    if (wifiList.size() < 1) {
                        wifiList.add(0, wifi); // ?????? ?????? ??? ?????? (????????? WiFi?????? ?????? ???????????? ??????)
                    } else {
                        wifiList.add(1, wifi); // ?????? ?????? ??? ?????? (????????? ??????)
                    }
                    saved = true;
                    break;
                }
            }
            if (saved) // ?????? ????????? ?????? skip
                continue;



            wifiList.add(wifi); // ?????? ?????? ??? ??????
        }

        wifiRAdapter = new WifiRAdapter(this, wifiList);
        wifiRAdapter.setHasStableIds(true); // ????????????
        recyclerView.setAdapter(wifiRAdapter);
        lottieAnimationView.setVisibility(View.INVISIBLE); //????????????
        scanningTV.setVisibility(View.INVISIBLE);
    }

    private void scanFailure() {    // Wifi?????? ??????
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
        scanningTV.setVisibility(View.INVISIBLE);
        lottieAnimationView.setVisibility(View.INVISIBLE); //????????????
//  ... potentially use older scan results ...
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
        
        if (checkWifiOnAndConnected()) { // Wi-Fi ????????? ?????? ????????? ?????????
            errorTV.setVisibility(View.GONE);
            contents.setVisibility(View.VISIBLE);
            wifiSwitch.setChecked(true);
            // ?????? ?????? ??????
            boolean success = wifiManager.startScan();
            // ???????????????
            scanningTV.setVisibility(View.VISIBLE);
            lottieAnimationView.setVisibility(View.VISIBLE);
            setUpAnimation(lottieAnimationView);
            if (!success) {
                scanningTV.setVisibility(View.INVISIBLE);
                lottieAnimationView.setVisibility(View.INVISIBLE); //????????????
                Log.e(TAG, "\"Wifi Scan??? ?????????????????????.");
            }
        } else {
            wifiSwitch.setChecked(false);
            errorTV.setVisibility(View.VISIBLE);
            contents.setVisibility(View.GONE);
            lottieAnimationView.setVisibility(View.INVISIBLE);
        }

    }

    private void setUpAnimation(LottieAnimationView animview) { //?????? ??????????????? ??????
        //????????? ???????????????
        animview.setAnimation("lottie_loading.json");
        //???????????? ?????? : ??????
        animview.setRepeatCount(LottieDrawable.INFINITE); //????????? ?????? ??????
        //??????
        animview.playAnimation();
    }

}