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
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
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

import static android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
import static com.example.mybomsettings.wifi.WifiListActivity.isConnected;

@SuppressLint("LongLogTag")
public class WifiRAdapter extends RecyclerView.Adapter<WifiRAdapter.ViewHolder>{

    public static ArrayList<WiFi> myWiFiList;
    public static int connectedWiFiPosition = -1; // 현재 연결된 WiFi의 인덱스
    public static int connectingWiFiPosition = -1; // 연결을 시도할 WiFi의 인덱스

    Dialog connectedDialog; // 연결된 WiFi 다이얼로그
    Dialog ConnectDialog; // WiFi 다이얼로그


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
                connectedDialog.setContentView(R.layout.activity_wifi_dialog);
                showConnectedDialog(v, pos);
            } else if (state == WiFi.WIFI_SAVED) { // 저장됨
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
            } else if (item.getState() == WiFi.WIFI_CONNECTING) {
                wifiConnectState.setText("연결중...");
                wifiConnectState.setVisibility(View.VISIBLE);
            } else if (item.getState() == WiFi.WIFI_AUTH_ERROR) {
                wifiConnectState.setText("인증 오류");
                wifiConnectState.setVisibility(View.VISIBLE);
            } else if (item.getState() == WiFi.WIFI_SAVED) {
                wifiConnectState.setText("저장됨");
                wifiConnectState.setVisibility(View.VISIBLE);
            } else if (item.getState() == WiFi.WIFI_NONE) {
                wifiConnectState.setVisibility(View.GONE);
            } else if (item.getState() == WiFi.WIFI_ERROR) {
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

    @SuppressLint("SetTextI18n")
    private void showConnectedDialog(View v, int pos) { // 연결된 WiFi 다이얼로그
        Context baseContext = v.getContext();
        String networkSSID = myWiFiList.get(pos).getSsid();
        ScanResult scanResult = myWiFiList.get(pos).getScanResult();

        connectedDialog.show();

        TextView wifi_tv = connectedDialog.findViewById(R.id.tv_dialog_wifi_ssid);
        TextView state_tv = connectedDialog.findViewById(R.id.tv_dialog_wifi_state);
        TextView level_tv = connectedDialog.findViewById(R.id.tv_dialog_wifi_level);
        TextView link_speed_tv = connectedDialog.findViewById(R.id.tv_dialog_wifi_link_speed);
        TextView frequency_tv = connectedDialog.findViewById(R.id.tv_dialog_wifi_frequency);
        TextView security_protocol_tv = connectedDialog.findViewById(R.id.tv_dialog_wifi_security_protocol);
        TextView forget_tv = connectedDialog.findViewById(R.id.tv_dialog_wifi_forget);
        TextView cancel_tv = connectedDialog.findViewById(R.id.tv_dialog_wifi_cancel);

        // WiFi 이름
        wifi_tv.setText(networkSSID);

        // 현재 연결된 WiFi 정보
        WifiManager wifiManager = (WifiManager)baseContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getSSID().equals("\"" +networkSSID+"\"")) {
            state_tv.setText("연결됨"); // 연결 상태
            link_speed_tv.setText(wifiInfo.getLinkSpeed()+" Mbps"); // 링크 속도
            frequency_tv.setText(wifiInfo.getFrequency()+" MHz"); // 빈도
            security_protocol_tv.setText(scanResult.capabilities); // 보안
            int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 4); // 신호 강도 (0~4단계)
            switch(level) { // 신호 강도 텍스트로 변경
                case 0:
                    level_tv.setText("매우 나쁨");
                    break;
                case 1:
                    level_tv.setText("나쁨");
                    break;
                case 2:
                    level_tv.setText("보통");
                    break;
                case 3:
                    level_tv.setText("좋음");
                    break;
                case 4:
                    level_tv.setText("매우 좋음");
                    break;
            }
        } else { // 오류 (연결됨 표시된 WiFi와 현재 연결된 WiFi 정보 불일치)
            AlertDialog.Builder failDialog = new AlertDialog.Builder(baseContext);
            failDialog.setTitle("현재 연결된 WiFi정보를 가져오는데 실패했습니다. WiFi 재검색 후 다시 시도해주세요");
            failDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ;
                }
            });
            connectedDialog.dismiss();
            failDialog.show();
            return ;
        }

        forget_tv.setOnClickListener(new View.OnClickListener() { // 저장 안 함 버튼
            @Override
            public void onClick(View v) {
                int netId = -1;
                for (WifiConfiguration tmp : wifiManager.getConfiguredNetworks()) {
                    if (tmp.SSID.equals("\"" +networkSSID+"\"")) {
                        // Log.i(TAG, "똑같은거 찾음:"+networkSSID);
                        netId = tmp.networkId;
                        break;
                    }
                }
                if (netId != -1 && wifiManager.removeNetwork(netId)) { // 저장된 WiFi 목록에서 삭제
                    myWiFiList.get(pos).setState(WiFi.WIFI_NONE);
                    notifyDataSetChanged();
                    Log.i(TAG, "저장 안 함 버튼 누르고 삭제 함");
                    connectedDialog.dismiss(); // 다이얼로그 닫기
                } else { // 삭제 실패
                    connectedDialog.dismiss(); // 다이얼로그 닫기
                    AlertDialog.Builder failDialog = new AlertDialog.Builder(baseContext);
                    failDialog.setTitle("WiFi를 저장 해제하는 동안 오류가 발생했습니다. 다시 시도해주세요");
                    failDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ;
                        }
                    });
                    failDialog.show();
                }
            }
        });

        cancel_tv.setOnClickListener(new View.OnClickListener() { // 닫기 버튼
            @Override
            public void onClick(View v) {
                connectedDialog.dismiss();
            }
        });


    }

    private void showConnectDialog(View v, int pos) { // WiFi 다이얼로그
        isConnected = false;
        Context baseContext = v.getContext();
        String networkSSID = myWiFiList.get(pos).getScanResult().SSID;
        EditText password = new EditText(baseContext);
        password.setInputType(TYPE_TEXT_VARIATION_VISIBLE_PASSWORD); // or TYPE_CLASS_TEXT

        AlertDialog.Builder dialog = new AlertDialog.Builder(baseContext);
        dialog.setTitle(networkSSID+"에 연결하기위한 비밀번호를 입력해주세요.");
        dialog.setView(password);
        dialog.setPositiveButton("연결", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myWiFiList.get(pos).setState(WiFi.WIFI_CONNECTING);
                notifyDataSetChanged();

                Log.i(TAG, networkSSID+"연결누름");
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

    /**
     * 저장되어있는 네트워크를 연결한다
     *  연결 성공
     * @param v 클릭한 WiFi의 View 객체
     * @param pos myWiFiList에서 해당 WiFi객체의 인덱스
     *
     */
    private void showSaveDialog(View v, int pos) { // 저장된 WiFi 다이얼로그 (클릭시 연결 시도)
        Context baseContext = v.getContext();
        String networkSSID = myWiFiList.get(pos).getSsid();
        WifiManager wifiManager = (WifiManager)v.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        AlertDialog.Builder saveDialog = new AlertDialog.Builder(baseContext);
        saveDialog.setTitle(networkSSID); // 다이얼로그 텍스트 : WiFi SSID명
        saveDialog.setPositiveButton("연결", new DialogInterface.OnClickListener() { // 저장된 WiFi 연결 시작
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int netId = -1;
                for (WifiConfiguration tmp : wifiManager.getConfiguredNetworks()) {
                    if (tmp.SSID.equals("\"" +networkSSID+"\"")) {
                        // Log.i(TAG, "똑같은거 찾음:"+networkSSID);
                        netId = tmp.networkId;
                        wifiManager.enableNetwork(netId, true);
                        break;
                    }
                }
                if (netId == -1) { // 연결 실패한 경우
                    Log.e(TAG, "addNetwork() returns -1.");
                    myWiFiList.get(pos).setState(WiFi.WIFI_ERROR); // 오류 발생
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
                }
                else { // 연결 될 때 까지 기다리기 (연결이 완료/실패되면 WifiListActivity의 wifiConnectReceiver에서 처리해줌)
                    Log.i(TAG, "연결 기다리기, supplicant state:"+wifiManager.getConnectionInfo().getSupplicantState());
                    connectingWiFiPosition = pos;
                    myWiFiList.get(pos).setState(WiFi.WIFI_CONNECTING);
                    notifyDataSetChanged();
                }
            }
        });
        saveDialog.setNegativeButton("저장 안 함", new DialogInterface.OnClickListener() { // 저장된 WiFi 삭제
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int netId = -1;
                for (WifiConfiguration tmp : wifiManager.getConfiguredNetworks()) {
                    if (tmp.SSID.equals("\"" +networkSSID+"\"")) {
                        // Log.i(TAG, "똑같은거 찾음:"+networkSSID);
                        netId = tmp.networkId;
                        break;
                    }
                }
                if (netId != -1 && wifiManager.removeNetwork(netId)) { // 저장된 WiFi 목록에서 삭제
                    myWiFiList.get(pos).setState(WiFi.WIFI_NONE);
                    notifyDataSetChanged();
                } else { // 삭제 실패
                    AlertDialog.Builder failDialog = new AlertDialog.Builder(baseContext);
                    failDialog.setTitle("WiFi를 저장 해제하는 동안 오류가 발생했습니다. 다시 시도해주세요");
                    failDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ;
                        }
                    });
                    failDialog.show();
                }
            }
        });
        saveDialog.setCancelable(true);

        saveDialog.show();
    }
}
