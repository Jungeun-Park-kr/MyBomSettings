package com.example.mybomsettings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.example.mybomsettings.BluetoothList.unpairDevice;


public class BluetoothRAdapter extends RecyclerView.Adapter<BluetoothRAdapter.ViewHolder>{

    public static ArrayList<Bluetooth> myBluetoothList;

    Dialog bluetoothDialog; // 블루투스 기기 클릭시 띄울 다이얼로그
    private static final String TAG = "MyTag:BluetoothRAdapter)";

    @SuppressLint("LongLogTag")
    public BluetoothRAdapter(Context c, List<BluetoothDevice> bluetoothDevices) {
        myBluetoothList = new ArrayList<Bluetooth>();
        Context myContexxt = (Context)c;

        // 모든 페어링된 디바이스 정보 불러오기
        if (bluetoothDevices.size() > 0) {

            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : bluetoothDevices) {
                String deviceName = device.getName(); // 디바이스 이름
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.i(TAG, "페어링된 기기:"+deviceName);

                myBluetoothList.add(new Bluetooth(device));
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public LinearLayout item; // 블루투스 기기 1개의 정보 담긴 layout (bluetooth_recyclerview_item.xml)
        public ImageButton setting; // 블루투스 기기 설정 버튼
        public TextView textName; // 기기 이름
        public TextView textConnectState; // 기기 연결 상태 (연결중.../연결됨/GONE)

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            item = (LinearLayout) itemView.findViewById(R.id.bluetooth_cell);
            textName = (TextView)itemView.findViewById(R.id.tv_bluetooth_device);
            setting = (ImageButton)itemView.findViewById(R.id.img_btn_bluetooth_setting);
            textConnectState = (TextView)itemView.findViewById(R.id.tv_bluetooth_connect_msg);
            itemView.setOnClickListener(this);
            item.setOnClickListener(this);
            setting.setOnClickListener(this);

        }

        @SuppressLint("LongLogTag")
        @Override
        public void onClick(View v) { // 해당 기기 클릭시
            int pos = getAdapterPosition();
            if (v == setting) { // 기기 설정버튼
                // 커스텀 다이얼로그 띄우기
                Log.i(TAG, "설정 클릭:"+(String) myBluetoothList.get(pos).getName());
                bluetoothDialog = new Dialog(v.getContext());
                bluetoothDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                bluetoothDialog.setContentView(R.layout.activity_bluetooth_dialog);
                showBluetoothDialog(v, pos);
            } else { // 기기 이름 버튼 - 블루투스 연결하기
                // TODO : 블루투스 연결하기
//                BluetoothList.connectSelectedDevice((String) myBluetoothList.get(pos).getName());
                Log.i(TAG, "기기 클릭:"+(String) myBluetoothList.get(pos).getName());
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // This is what adds the code we've written in here to our target view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.bluetooth_recyclerview_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    public void addBluetoothDevice(Bluetooth device) { myBluetoothList.add(device); } // 테스트2용

    @SuppressLint("LongLogTag")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Here we use the information in the list we create to define the views
        Log.e(TAG, "onBindViewHolder() 호출, myBluetoothList 길이:"+myBluetoothList.size());
        if (!myBluetoothList.isEmpty()) {
            // 이름 보이기
            TextView name = holder.textName;
            name.setText(myBluetoothList.get(position).getName());
        }
    }

    @Override
    public int getItemCount() {
        return myBluetoothList.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @SuppressLint("LongLogTag")
    private void showBluetoothDialog(View v, int pos) { // 다이얼로그 디자인 및 띄우기
        Log.i(TAG, "다이얼로그 띄우기");
        bluetoothDialog.show();

        BluetoothDevice device = myBluetoothList.get(pos).getDevice(); // 클릭한 디바이스 정보
        TextView name = bluetoothDialog.findViewById(R.id.tv_dialog_bluetooth_name);
        TextView forget = bluetoothDialog.findViewById(R.id.tv_dialog_bluetooth_forget);
        TextView confirm = bluetoothDialog.findViewById(R.id.tv_dialog_bluetooth_confirm);

        name.setText(myBluetoothList.get(pos).getName());

        forget.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) { // 페어링 해제 (저장안함 버튼 클릭)
                unpairDevice(myBluetoothList.get(pos).getDevice());
                bluetoothDialog.dismiss(); // 다이얼로그 닫기
//                myBluetoothList.remove(pos); // 삭제한 디바이스는 목록에서 삭제 (이거 동작안됨)
                Log.i(TAG, "삭제 완료, bluetoothList길이 :"+myBluetoothList.size());
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothDialog.dismiss(); // 다이얼로그 닫기
            }
        });



    }


}
