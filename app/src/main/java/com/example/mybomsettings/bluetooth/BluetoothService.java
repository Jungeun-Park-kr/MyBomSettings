package com.example.mybomsettings.bluetooth;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.mybomsettings.MainActivity;
import com.example.mybomsettings.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.mybomsettings.bluetooth.BluetoothListActivity.pairedDevices;
import static com.example.mybomsettings.bluetooth.BluetoothListActivity.updateBluetoothList;

public class BluetoothService extends Service {
    private static final String TAG = "MyTag)BluetoothService";

    // 항상 앱을 켜두기 위한 상단바
    public static final String NOTIF_ID_KeepMeAlive = "KeepMeAlive";
    private static final int NOTIF_ID_KMA = 1;

    // bluetooth
    private static BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager ;

    private BluetoothDevice connectedDevice;

    public BluetoothService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Initializes Bluetooth adapter.
        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();


        // 페어링된 디바이스 목록 가져오기
        List<BluetoothDevice> tmpList = new ArrayList<>(bluetoothAdapter.getBondedDevices());
        pairedDevices = new ArrayList<>();
        for (BluetoothDevice device : tmpList) {
            // 연결된 디바이스 상태를 페어링된 디바이스 목록에 저장 (저장된 기기+연결된 기기 모두 포함)
            Bluetooth bluetooth = new Bluetooth(device);
            pairedDevices.add(bluetooth);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                handleStart(intent, startId);
            }
        }, "BluetoothService").start();

        startForeground();

        Log.d(TAG, "onStartCommand - Bluetooth Service started! ");

        /*if (!isServiceRunning(BluetoothService.class.getName())) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    handleStart(intent, startId);
                }
            }, "BluetoothService").start();

            Log.d(TAG, "onStartCommand - Bluetooth Service started! ");
        } else {
            Log.d(TAG, "onStartCommand - Bluetooth Service already running! ");
        }*/

        return START_STICKY;
    }

    private void startForeground() {

        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        startForeground(NOTIF_ID_KMA, new NotificationCompat.Builder(this,
                NOTIF_ID_KeepMeAlive) // don't forget create a notification channel first
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build());
    }


    synchronized void handleStart(Intent intent, int startId) {
        // 브로드캐스트 리시버 등록
        //블루투스 브로드캐스트 리시버 등록
        IntentFilter searchFilter = new IntentFilter();
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //BluetoothRAdapter.ACTION_DISCOVERY_STARTED : 블루투스 검색 시작
        searchFilter.addAction(BluetoothDevice.ACTION_FOUND); //BluetoothDevice.ACTION_FOUND : 블루투스 디바이스 찾음
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //BluetoothRAdapter.ACTION_DISCOVERY_FINISHED : 블루투스 검색 종료
        searchFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        searchFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        searchFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        searchFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bluetoothSearchReceiver, searchFilter);
    }

    //블루투스 검색결과
    final BroadcastReceiver bluetoothSearchReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) { // 연결됨
                connectedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "연결된 애 있음. 갱신 시도..."+ connectedDevice.getName());
                for (Bluetooth bluetooth : pairedDevices) {
                    if (connectedDevice.getName().equals(bluetooth.getName().toString())) { // 일치하는 기기 찾기
                        pairedDevices.get(pairedDevices.indexOf(bluetooth)).setConnected(true);
                        updateBluetoothList(pairedDevices);
                        Log.i(TAG, "완전 갱신함 - pairedDevices의 길이:"+pairedDevices.size());
                        Log.i(TAG, "--연결됨--" + connectedDevice.getName());
                        break;
                    }
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) { // 연결해제 요청
                connectedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "연결해제 될 애 있음. 갱신 시도..." + connectedDevice.getName());
                for (Bluetooth bluetooth : pairedDevices) {
                    if (connectedDevice.getName().equals(bluetooth.getName().toString())) { // 일치하는 기기 찾기
                        pairedDevices.get(pairedDevices.indexOf(bluetooth)).setConnected(false);
                        updateBluetoothList(pairedDevices);
                        Log.i(TAG, "완전 갱신함 - pairedDevices의 길이:"+pairedDevices.size());
                        Log.i(TAG, "--연결 해제 요청--" + connectedDevice.getName());
                        break;
                    }
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) { // 연결 해제됨
                connectedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "연결해제 된 애 있음. 갱신 시도..." + connectedDevice.getName());
                for (Bluetooth bluetooth : pairedDevices) {
                    if (connectedDevice.getName().equals(bluetooth.getName().toString())) { // 일치하는 기기 찾기
                        pairedDevices.get(pairedDevices.indexOf(bluetooth)).setConnected(false);
                        updateBluetoothList(pairedDevices);
                        Log.i(TAG, "완전 갱신함 - pairedDevices의 길이:"+pairedDevices.size());
                        Log.i(TAG, "--연결 해제됨--" + connectedDevice.getName());
                        break;
                    }
                }
                Log.i(TAG,"ACTION_ACL_DISCONNECTED 필터링 끝");
            }
        }
    };

    public Boolean isServiceRunning(String class_name){
        // 시스템 내부의 액티비티 상태를 파악하는 ActivityManager객체를 생성한다.
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        //  manager.getRunningServices(가져올 서비스 목록 개수) - 현재 시스템에서 동작 중인 모든 서비스 목록을 얻을 수 있다.
        // 리턴값은 List<ActivityManager.RunningServiceInfo>이다. (ActivityManager.RunningServiceInfo의 객체를 담은 List)
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            // ActivityManager.RunningServiceInfo의 객체를 통해 현재 실행중인 서비스의 정보를 가져올 수 있다.
            if (class_name.equals(service.service.getClassName())) {
                return true;
            }
        }
        return  false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}