package com.example.mybomsettings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import static android.provider.Settings.System.SCREEN_BRIGHTNESS;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;

public class DisplayListActivity extends AppCompatActivity {
    SeekBar brightSeekBar;
    Switch brightModeSwitch;
    public static Context baseContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_list);
        baseContext = getBaseContext();
        brightSeekBar = findViewById(R.id.BrightnessSB);
        brightModeSwitch = findViewById(R.id.BrightnessSwitch);

        initialDisplaySetting();

        brightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                setBrightness(value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        brightModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(getApplicationContext())) {
                    if (isChecked == true) { // ?????? ?????? checked
                        Settings.System.putInt(getApplicationContext().getContentResolver(), SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                        brightSeekBar.setEnabled(false); // ?????? ?????? X
                        Log.i("MyTag:", "???????????? ???");
                    } else { // ?????? ?????? X
                        Settings.System.putInt(getApplicationContext().getContentResolver(), SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_MANUAL);
                        brightSeekBar.setEnabled(true); // ?????? ?????? ??????
                        Log.i("MyTag:", "???????????? ???");
                    }
                }  else {
                    Log.e("MyTag:", "canWrite()?????????~ ??? ?????? - ????????? ?????? ?????? ???????????????");
                }
            } else {
                Log.e("MyTag:", "?????? ??????");
            }

        });


    }

    private void initialDisplaySetting() { // default display ???????????? ?????????
        try {
            // ?????? ???????????? ???????????????
            int currentBrightness = Settings.System.getInt(getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, /* default value */ 50);
            brightSeekBar.setProgress((int) currentBrightness);
            Log.i("MyTag:", "?????? ??????:"+currentBrightness+", sdk version:"+Build.VERSION.SDK_INT);

            // ?????? ?????? ???????????????
            int brightnessMode = Settings.System.getInt(getApplicationContext().getContentResolver(), SCREEN_BRIGHTNESS_MODE);
            if (brightnessMode == SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                brightModeSwitch.setChecked(true);
                brightSeekBar.setEnabled(false); // ?????? ?????? X
            } else {
                brightModeSwitch.setChecked(false);
                brightSeekBar.setEnabled(true); // ?????? ?????? ??????
            }


        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setBrightness(int val) {
        if (val < 10) {
            val = 10;
        } else if (val > 255) {
            val = 255;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(getApplicationContext())) {
                Log.i("MyTag:", "????????? ??????:"+val);
                Settings.System.putInt(baseContext.getContentResolver(),
                        SCREEN_BRIGHTNESS, val);
            } else {
                Log.e("MyTag:", "canWrite()?????????~ ??? ?????? - ????????? ?????? ?????? ???????????????");
            }
        }
        /*WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = (float) val / 100;
        getWindow().setAttributes(params);*/

        /*Settings.System.putInt(baseContext.getContentResolver(),
                SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_AUTOMATIC);*/
    }




    @Override
    public ContentResolver getContentResolver() {
        return super.getContentResolver();
    }

    public boolean checkSystemPermission() {
        boolean permission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   //23?????? ??????
            permission = Settings.System.canWrite(this);
            Log.d("test", "Can Write Settings: " + permission);
            if(permission){
                Log.e("test", "??????");
            }else{
                Log.e("test", "?????????");
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 2127);
                permission = false;
            }
        } else {

        }

        return permission;
    }

}