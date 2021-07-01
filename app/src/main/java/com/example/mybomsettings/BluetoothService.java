package com.example.mybomsettings;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

import static com.example.mybomsettings.BluetoothList.pairedDevices;
import static com.example.mybomsettings.BluetoothList.updateBluetoothList;

public class BluetoothService extends Service {
    private static final String TAG = "MyTag:Bluetooth Service";

    BluetoothDevice connected;
    public BluetoothService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Thread thread = new Thread() {
            @Override
            public void run() {
                handleStart(intent, startId);
            }
        };
        thread.start();
        Log.d(TAG, "onStartCommand - Bluetoothservice started! ");

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    synchronized void handleStart(Intent intent, int startId)  {
        // 블루투스 연결상태 브로드캐스트 리시버 등록
        IntentFilter connectFilter = new IntentFilter();
        connectFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        connectFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        connectFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bluetoothConnectReceiver, connectFilter);
    }

    final BroadcastReceiver bluetoothConnectReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) { //연결됨
                connected = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "연결된 애 있음. 갱신 시도..."+connected.getName());
                //setDeviceState(action);
                for (Bluetooth bluetooth : pairedDevices) {
                    if (connected.getName().equals(bluetooth.getName().toString())) { // 일치하는 기기 찾기
                        pairedDevices.get(pairedDevices.indexOf(bluetooth)).setConnected(true);
                        updateBluetoothList(pairedDevices);
                        Log.i(TAG, "--연결됨--" + connected.getName());
                        break;
                    }
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) { //연결해제 요청
                connected = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "연결해제 될 애 있음. 갱신 시도..." + connected.getName());
                //setDeviceState(action);
                for (Bluetooth bluetooth : pairedDevices) {
                    if (connected.getName().equals(bluetooth.getName().toString())) { // 일치하는 기기 찾기
                        pairedDevices.get(pairedDevices.indexOf(bluetooth)).setConnected(false);
                        updateBluetoothList(pairedDevices);
                        Log.i(TAG, "--연결 해제 요청--" + connected.getName());
                        break;
                    }
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) { // 연결 해제됨
                connected = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "연결해제 된 애 있음. 갱신 시도..." + connected.getName());
                //setDeviceState(action);
                for (Bluetooth bluetooth : pairedDevices) {
                    if (connected.getName().equals(bluetooth.getName().toString())) { // 일치하는 기기 찾기
                        pairedDevices.get(pairedDevices.indexOf(bluetooth)).setConnected(false);
                        updateBluetoothList(pairedDevices);
                        Log.i(TAG, "--연결 해제됨--" + connected.getName());
                        break;
                    }
                }
                Log.i(TAG,"ACTION_ACL_DISCONNECTED 필터링 끝");
            }
        }
    };

}