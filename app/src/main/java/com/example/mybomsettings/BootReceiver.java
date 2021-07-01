package com.example.mybomsettings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    final static String TAG = "MyTAG: ";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.intent.action.BOOT_COMPLETED") || action.equals("android.intent.action.REBOOT")) {
            /*Log.e(TAG, "전원 켜짐");
            Intent intentBoot = new Intent(context, BluetoothService.class);
            context.startService(intentBoot);*/
        }
    }
}