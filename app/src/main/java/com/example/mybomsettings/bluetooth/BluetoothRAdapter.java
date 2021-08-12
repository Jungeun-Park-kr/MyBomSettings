package com.example.mybomsettings.bluetooth;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.content.DialogInterface;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybomsettings.R;

import java.util.ArrayList;
import java.util.List;

import static com.example.mybomsettings.bluetooth.BluetoothListActivity.closeGatt;
import static com.example.mybomsettings.bluetooth.BluetoothListActivity.connectPairedDevice;
import static com.example.mybomsettings.bluetooth.BluetoothListActivity.connectSelectedBLEDevice;
import static com.example.mybomsettings.bluetooth.BluetoothListActivity.disConnectBLEDevice;
import static com.example.mybomsettings.bluetooth.BluetoothListActivity.disconnectPairedDevice;
import static com.example.mybomsettings.bluetooth.BluetoothListActivity.unpairDevice;

@SuppressLint("LongLogTag")
public class BluetoothRAdapter extends RecyclerView.Adapter<BluetoothRAdapter.ViewHolder>{

    public ArrayList<Bluetooth> myBluetoothList;

    Dialog bluetoothDialog; // 블루투스 기기 클릭시 띄울 다이얼로그
    private static final String TAG = "MyTag:BluetoothRAdapter)";

    public BluetoothRAdapter(Context c, List<Bluetooth> bluetoothDevices) {
        myBluetoothList = new ArrayList<Bluetooth>();
        Context myContexxt = (Context)c;

        // 모든 페어링된 디바이스 정보 불러오기
        if (bluetoothDevices.size() > 0) {

            // There are paired devices. Get the name and address of each paired device.
            for (Bluetooth device : bluetoothDevices) {
                Log.i(TAG, "페어링된 기기:"+device.getName());
                myBluetoothList.add(device);
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public LinearLayout item; // 블루투스 기기 1개의 정보 담긴 layout (bluetooth_recyclerview_item.xml)
        public ImageButton setting; // 블루투스 기기 설정 버튼
        public TextView textName; // 기기 이름
        public TextView textConnectState; // 기기 연결 상태 (연결중.../연결됨/GONE)

        @SuppressLint({"ClickableViewAccessibility", "LongLogTag"})
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
                // TODO : 블루투스 연결하기 - 안됨..
//                BluetoothListActivity.connectSelectedDevice((String) myBluetoothList.get(pos).getName());
                Log.i(TAG, "기기 클릭:"+(String) myBluetoothList.get(pos).getName());

                BluetoothDevice device = myBluetoothList.get(pos).getDevice();
                if (myBluetoothList.get(pos).getConnected()) { // 이미 연결 되어있는 경우
                    // 연결 해제 다이얼로그 띄우기
                    AlertDialog.Builder adb = new AlertDialog.Builder(v.getContext());
                    adb.setTitle("연결을 해제하시겠습니까?");
                    adb.setCancelable(true);
                    adb.setMessage(myBluetoothList.get(pos).getName()+"와(과) 연결이 끊어집니다.");
                    adb.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 연결 해제 시작
                            if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
                                Log.i(TAG, "DEVICE_TYPE_LE 연결 해제");
                                disConnectBLEDevice(device); // DEVICE_TYPE_LE
                                closeGatt();
                            } else {
                                Log.i(TAG, "일반 블루투스 연결 해제");
                                disconnectPairedDevice(device); // 일반 BLUETOOTH
                                disConnectBLEDevice(device); // DEVICE_TYPE_LE
                                closeGatt();
                            }
                            dialog.dismiss();
                        }
                    });
                    adb.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = adb.create();
                    alertDialog.show();
                } else { // 연결 안되어있는 경우
                    // 연결 시도하기
                    connectPairedDevice(device); // 일반 BLUETOOTH
                    connectSelectedBLEDevice(device); // DEVICE_TYPE_LE
                }

                /*if (BluetoothListActivity.getState() == STATE_NONE) { // 페어링은 되어있으나 연결이 안되어 있는 경우
                    connectPairedDevice(myBluetoothList.get(pos).getDevice());
                    textConnectState.setVisibility(View.VISIBLE);
                    textConnectState.setText("연결됨");
                } else if (BluetoothListActivity.getState() == STATE_CONNECTING) { // 연결 중
                    textConnectState.setVisibility(View.VISIBLE);
                    textConnectState.setText("연결중...");
                } else if (BluetoothListActivity.getState() == STATE_DISCONNECTING) { // 연결 완료된 경우
                    //textConnectState.setVisibility(View.GONE);
                    disconnectPairedDevice(myBluetoothList.get(pos).getDevice());
                }
                else { // 연결 시도하기
                    textConnectState.setVisibility(View.VISIBLE);
                    textConnectState.setText("연결중...");
                    connectPairedDevice(myBluetoothList.get(pos).getDevice());
                    textConnectState.setVisibility(View.VISIBLE);
                    textConnectState.setText("연결됨");
                }*/
            }

        }

        public void setItem(Bluetooth bluetooth) {
            if (!myBluetoothList.isEmpty()) {  // 이름 보이기
                textName.setText(bluetooth.getName());
            }

            if (bluetooth.getConnected()) { // 연결 상태 보이기
                // Log.i(TAG, bluetooth.getName()+"<- 현재 연결됨");
                textConnectState.setText("연결됨");
                textConnectState.setVisibility(View.VISIBLE);
            } else {
                textConnectState.setVisibility(View.GONE);
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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Here we use the information in the list we create to define the views
        holder.setItem(myBluetoothList.get(position));
    }

    @Override
    public int getItemCount() {
        return myBluetoothList.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

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
                String tmp = (String) myBluetoothList.get(pos).getName();
                bluetoothDialog.dismiss(); // 다이얼로그 닫기
                myBluetoothList.remove(pos); // 삭제한 디바이스는 목록에서 삭제 (이거 동작안됨)
                Log.i(TAG, tmp+"삭제 완료, bluetoothList길이 :"+myBluetoothList.size());
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
