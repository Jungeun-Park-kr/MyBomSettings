package com.example.mybomsettings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("LongLogTag")
public class WifiRAdapter extends RecyclerView.Adapter<WifiRAdapter.ViewHolder>{
    public static ArrayList<WiFi> myWiFiList;

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
                    Log.i(TAG, "현재 연결된 wifi :"+wifi.getScanResult().SSID);
                }
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public LinearLayout item; // WiFi 정보 담긴 아이템 하나
        public ImageView wifiLevel; // WIFI 신호 강도 아이콘
        public TextView wifiName; // wifi 이름
        public TextView wifiConnectState; // wifi 연결 상태 (연결됨, 저장됨, GONE)

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
            } else if (state == WiFi.WIFI_NONE) { // 저장 안됨
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
                    wifiLevel.setImageResource(R.drawable.wifi_1_24);
                    break;
                case 2:
                    wifiLevel.setImageResource(R.drawable.wifi_2_24);
                    break;
                case 3:
                    wifiLevel.setImageResource(R.drawable.wifi_3_24);
                    break;
                default:
                    wifiLevel.setImageResource(R.drawable.wifi_0_24);
            }
            // 연결 상태별 안내 문구
            if (item.getState() == WiFi.WIFI_CONNECTED) {
                wifiConnectState.setText("연결됨");
                wifiConnectState.setVisibility(View.VISIBLE);
            } else if (item.getState() == WiFi.WIFI_SAVED) {
                wifiConnectState.setText("저장됨");
                wifiConnectState.setVisibility(View.VISIBLE);
            } else if (item.getState() == WiFi.WIFI_NONE) {
                wifiConnectState.setVisibility(View.GONE);
            } else {
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

        TextView name = holder.wifiName;
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
        connectedDialog.show();

    }
    private void showConnectDialog(View v, int pos) { // WiFi 다이얼로그
        connectedDialog.show();
    }
    private void showSaveDialog(View v, int pos) { // 저장된 WiFi 다이얼로그
        saveDialog.show();
    }

}
