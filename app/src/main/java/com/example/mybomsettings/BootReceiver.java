package com.example.mybomsettings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.mybomsettings.bluetooth.BluetoothService;

public class BootReceiver extends BroadcastReceiver {
    /**
     * 전원이 켜지는 것을 감지해서 블루투스 서비스를 실행하는 브로드캐스트 리시버입니다.
     *   - 블루투스 서비스 : 백그라운드에서 블루투스 연결을 감지
     */
    final static String TAG = "MyTAG: ";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.intent.action.BOOT_COMPLETED") || action.equals("android.intent.action.REBOOT")) {
            // Log.e(TAG, "전원 켜짐");
            Intent intentBoot = new Intent(context, BluetoothService.class);
            context.startService(intentBoot);
        }
    }
}