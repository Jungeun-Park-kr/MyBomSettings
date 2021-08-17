package com.example.mybomsettings.wifi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybomsettings.R;

import java.util.ArrayList;
import java.util.List;

import static com.example.mybomsettings.wifi.WifiListActivity.isConnected;

@SuppressLint("LongLogTag")
public class WifiRAdapter extends RecyclerView.Adapter<WifiRAdapter.ViewHolder>{

    public static ArrayList<WiFi> myWiFiList;
    private int connectedWiFiPosition = -1; // 현재 연결된 WiFi의 인덱스

    Dialog connectedDialog; // 연결된 WiFi 다이얼로그
    Dialog ConnectDialog; // WiFi 다이얼로그
    Dialog saveDialog; // 저장된 WiFi 다이얼로그


    private static final String TAG = "MyTag:WiFiRAdapter)";

    public WifiRAdapter(Context c, List<WiFi> wifis) {
        myWiFiList = new ArrayList<WiFi>();
        Context myContexxt = (Context)c;
        if (wifis.size() > 0) {
            for (WiFi wifi : wifis) {
//                Log.i(TAG, "--"+wifi.getScanResult().SSID+"--");
                myWiFiList.add(wifi);
                if (wifi.getState() == WiFi.WIFI_CONNECTED) {
                    connectedWiFiPosition = myWiFiList.indexOf(wifi); // 현재 연결되어 있는 인덱스 저장
                    Log.i(TAG, "현재 연결된 wifi :"+wifi.getScanResult().SSID);
                }
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public LinearLayout item; // WiFi 정보 담긴 아이템 하나
        public ImageView wifiLevel; // WIFI 신호 강도 아이콘
        public TextView wifiName; // wifi 이름
        public TextView wifiConnectState; // wifi 연결 상태 (연결됨, 저장됨, 인증오류, 오류, GONE)

        @SuppressLint({"ClickableViewAccessibility"})
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            item = (LinearLayout) itemView.findViewById(R.id.wifi_cell);
            wifiLevel = (ImageView) itemView.findViewById(R.id.img_wifi_level);
            wifiName = (TextView) itemView.findViewById(R.id.tv_wifi_name);
            wifiConnectState = (TextView) itemView.findViewById(R.id.tv_wifi_connect_msg);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) { // 아이템 클릭할 시
            int pos = getAdapterPosition();
            int state = myWiFiList.get(pos).getState();
            if (state == WiFi.WIFI_CONNECTED) { // 현재 연결중
                connectedDialog = new Dialog(v.getContext());
                connectedDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                //connectedDialog.setContentView();
                showConnectedDialog(v, pos);
            } else if (state == WiFi.WIFI_SAVED) { // 저장됨
                saveDialog = new Dialog(v.getContext());
                saveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                //saveDialog.setContentView();
                showSaveDialog(v, pos);
            } else if (state == WiFi.WIFI_CONNECTING) { // 연결중
                ;
            } else if (state == WiFi.WIFI_NONE || state == WiFi.WIFI_AUTH_ERROR || state == WiFi.WIFI_ERROR) { // 저장 안됨, 인증오류, 오류
                ConnectDialog = new Dialog(v.getContext());
                ConnectDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                //ConnectDialog.setContentView();
                showConnectDialog(v, pos);
            } else {
                ConnectDialog = new Dialog(v.getContext());
                ConnectDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                //ConnectDialog.setContentView();
                showConnectDialog(v, pos);
            }
        }

        public void setItem(WiFi item) {
            wifiName.setText(item.getScanResult().SSID);
            // 강도 아이콘 설정
            int level = item.getLevel();
            switch(level) {
                case 1:
                    wifiLevel.setImageResource(R.drawable.wifi_1_512);
                    break;
                case 2:
                    wifiLevel.setImageResource(R.drawable.wifi_2_512);
                    break;
                case 3:
                case 4:
                    wifiLevel.setImageResource(R.drawable.wifi_3_512);
                    break;

                default:
                    wifiLevel.setImageResource(R.drawable.wifi_0_512);
            }
            // 연결 상태별 안내 문구
            if (item.getState() == WiFi.WIFI_CONNECTED) {
                wifiConnectState.setText("연결됨");
                wifiConnectState.setVisibility(View.VISIBLE);
                connectedWiFiPosition = getAdapterPosition(); // 현재 연결되어 있는 인덱스 저장
            } else if (item.getState() == WiFi.WIFI_AUTH_ERROR) {
                wifiConnectState.setText("인증 오류");
                wifiConnectState.setVisibility(View.VISIBLE);
            } else if (item.getState() == WiFi.WIFI_SAVED) {
                wifiConnectState.setText("저장됨");
                wifiConnectState.setVisibility(View.VISIBLE);
            } else if (item.getState() == WiFi.WIFI_NONE) {
                wifiConnectState.setVisibility(View.GONE);
            } else {
                wifiConnectState.setText("오류");
                wifiConnectState.setVisibility(View.GONE);
            }
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        Log.e(TAG, "onCreateViewHolder() 호출, myWiFiList 길이:"+myWiFiList.size());
        // This is what adds the code we've written in here to our target view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.wifi_recyclerview_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Here we use the information in the list we create to define the views
//        Log.e(TAG, "onBindViewHolder() 호출, myWiFiList 길이:"+myWiFiList.size());

        holder.setItem(myWiFiList.get(position));
    }

    @Override
    public int getItemCount() {
        return myWiFiList.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void showConnectedDialog(View v, int pos) { // 연결된 WiFi 다이얼로그
        //connectedDialog.show();
        // 다시 한번 더 연결 처리 in here

    }
    private void showConnectDialog(View v, int pos) { // WiFi 다이얼로그
        isConnected = false;
        Context baseContext = v.getContext();
        String networkSSID = myWiFiList.get(pos).getScanResult().SSID;
        EditText password = new EditText(v.getContext());

        AlertDialog.Builder dialog = new AlertDialog.Builder(baseContext);
        dialog.setTitle(networkSSID+"에 연결하기위한 비밀번호를 입력해주세요.");
        dialog.setView(password);
        dialog.setPositiveButton("연결", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myWiFiList.get(pos).setState(WiFi.WIFI_CONNECTING);
                notifyDataSetChanged();


                Log.i(TAG, networkSSID+"연결누름");
                // 비밀번호 확인
                /*if (password.getText().length() < 4) {
                    Log.i(TAG, "짧은 비번");
                    AlertDialog.Builder alert = new AlertDialog.Builder(baseContext);
                    alert.setTitle("비밀번호가 너무 짧습니다.");
                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ;
                        }
                    });
                    alert.show();
                } */

                //1번째
                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = String.format("\"%s\"", networkSSID);
                wifiConfig.preSharedKey = String.format("\"%s\"", password.getText().toString());

                WifiManager wifiManager = (WifiManager)baseContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                //remember id
                int netId = wifiManager.addNetwork(wifiConfig); // WiFi manager에 추가해주기
                if (netId == -1) {
                    Log.e(TAG, "addNetwork() returns -1.");
                }
                else {
                    List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                    for( WifiConfiguration i : list ) {
                        if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                            wifiManager.disconnect();
                            wifiManager.enableNetwork(i.networkId, true); // 실제 Android에 연결 시키기
                            boolean isSucceeded = wifiManager.reconnect();
                            Log.i(TAG, "연결성공?,"+"isSucceeded:"+isSucceeded+", status:"+wifiConfig.status+", supplicant state:"+wifiManager.getConnectionInfo().getSupplicantState());
                            myWiFiList.get(connectedWiFiPosition).setState(WiFi.WIFI_SAVED); // 기존 연결된 WiFi 저장됨으로 바꾸기
                            myWiFiList.get(pos).setState(WiFi.WIFI_CONNECTED);
                            connectedWiFiPosition = pos; // 현재 연결되어 있는 인덱스 저장
                            notifyDataSetChanged();
                            return;
                        }
                    }
                }
                // 연결 실패한 경우
                myWiFiList.get(pos).setState(WiFi.WIFI_AUTH_ERROR); // 인증 오류
                notifyDataSetChanged();
                AlertDialog.Builder failDialog = new AlertDialog.Builder(baseContext);
                failDialog.setTitle("연결을 실패했습니다. 다시 시도해주세요");
                failDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ;
                    }
                });
                failDialog.show();

                /*NetworkRequest networkCallback = new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        if (bindProcessToNetwork(network)) {
                            // socket connections will now use this network
                        } else {
                            // app doesn't have android.permission.INTERNET permission
                        }
                    }
                };

                NetworkRequest request = new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build();
                ConnectivityManager connectivityManager = (ConnectivityManager) v.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                connectivityManager.requestNetwork(request, networkCallback);*/
            }

        });
        dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private void showSaveDialog(View v, int pos) { // 저장된 WiFi 다이얼로그
        saveDialog.show();
    }

    /**
     * Emits a single of the [WifiConfiguration] created from the passed [scanResult] and [preSharedKey]
     */
    /*private WifiConfiguration createWifiConfiguration(Context context, ScanResult scanResult, String preSharedKey) {
        val auth = scanResult.auth;
        val keyManagement = scanResult.keyManagement;
        val pairwiseCipher = scanResult.pairwiseCipher;

        WifiConfiguration config = context.getApplicationContext().WifiConfiguration();
        config.SSID = "\"" +  scanResult.ssid + "\"";
        config.BSSID = scanResult.bssid;

        if (auth.contains("WPA") || auth.contains("WPA2")) {
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
        }

        if (auth.contains("EAP"))
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.LEAP)
        else if (auth.contains("WPA") || auth.contains("WPA2"))
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
        else if (auth.contains("WEP"))
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)

        if (keyManagement.contains("IEEE802.1X"))
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X)
        else if (auth.contains("WPA") && keyManagement.contains("EAP"))
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP)
        else if (auth.contains("WPA") && keyManagement.contains("PSK"))
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
        else if (auth.contains("WPA2") && keyManagement.contains("PSK"))
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)

        if (pairwiseCipher.contains("CCMP") || pairwiseCipher.contains("TKIP")) {
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
        }

        if (preSharedKey.isNotEmpty()) {
            if (auth.contains("WEP")) {
                if (preSharedKey.matches("\\p{XDigit}+".toRegex())) {
                    config.wepKeys[0] = preSharedKey
                } else {
                    config.wepKeys[0] = "\"" + preSharedKey + "\""
                }
                config.wepTxKeyIndex = 0
            } else {
                config.preSharedKey = "\"" + preSharedKey + "\""
            }
        }

        return config;
    }*/

}
