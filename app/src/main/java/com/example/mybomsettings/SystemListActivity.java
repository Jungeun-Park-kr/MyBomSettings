package com.example.mybomsettings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragment;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.SeekBarPreference;

import android.widget.Switch;
import android.widget.Toast;

import static android.media.AudioManager.FLAG_PLAY_SOUND;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class SystemListActivity extends AppCompatActivity{
    /**
     * 시스템 설정 액티비티 입니다.
     * 디스플레이 및 소리 관련 설정을 변경합니다.
     */


    /**
     * <<디스플레이>>
     *     - 자동 밝기 ON/OFF 스위치
     *     - 밝기 조절 SeekBar
     */
    // 디스플레이 UI
    SeekBar brightnessSeekBar; // 밝기 조절
    Switch brightnessModeSwitch; // 자동 밝기 ON/OFF

    /**
     * <<사운드>>
     * 안드로이드에는 총 5가지의 소리가 있음
     * 따라서 5가지를 조절할 수 있는 기능을 모두 넣었음
     */
    // 사운드 UI
    SeekBar alarmSeekBar; // 알람
    SeekBar mediaSeekBar; // 미디어
    SeekBar callSeekBar; // 전화
    SeekBar notificationSeekBar; // 알림
    SeekBar systemSeekBar; // 시스템

    private AudioManager mAudioManager;
    public static Context baseContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_list);

        baseContext = getBaseContext();


        initialDisplaySetting(); // 현재 디스플레이 설정값 가져와서 초기화
        initialSoundSetting(); // 현재 소리 설정값 가져와서 초기화

        // SeekBar 리스너 등록
        brightnessSeekBar.setOnSeekBarChangeListener(new seekBarChangeListener()); // 밝기 조절
        alarmSeekBar.setOnSeekBarChangeListener(new seekBarChangeListener()); // 알람 볼륨
        mediaSeekBar.setOnSeekBarChangeListener(new seekBarChangeListener()); // 미디어 볼륨
        callSeekBar.setOnSeekBarChangeListener(new seekBarChangeListener()); // 전화
        notificationSeekBar.setOnSeekBarChangeListener(new seekBarChangeListener()); // 알림
        systemSeekBar.setOnSeekBarChangeListener(new seekBarChangeListener()); // 시스템

        // 자동 밝기 ON/OFF 스위치
        brightnessModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.System.canWrite(getApplicationContext())) {
                        if (brightnessModeSwitch.isChecked()) { // 자동 밝기 checked
                            Settings.System.putInt(getContentResolver(), SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                            brightnessSeekBar.setEnabled(false); // 밝기 조절 X
                            // Log.i("MyTag:", "자동밝기 켬");
                        } else { // 자동 밝기 X
                            Settings.System.putInt(getApplicationContext().getContentResolver(), SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_MANUAL);
                            brightnessSeekBar.setEnabled(true); // 밝기 조절 가능
                            // Log.i("MyTag:", "자동밝기 끔");
                        }
                    }  else { // 권한이 없는 경우 설정을 바꿀 수 없음
                        // Log.e("MyTag:", "canWrite()불가임~ 앱 권한 - 시스템 설정 권한 확인해보기");
                    }
                } else { // 설정을 바꿀 수 없는 버전
                    // Log.e("MyTag:", "버전 아님");
                }
            }
        });

    }



    private void initialSoundSetting() {
        mediaSeekBar = findViewById(R.id.mediaVolumeSB);
        alarmSeekBar = findViewById(R.id.alarmVolumeSB);
        callSeekBar = findViewById(R.id.callVolumeSB);
        notificationSeekBar = findViewById(R.id.notificationVolumeSB);
        systemSeekBar = findViewById(R.id.systemVolumeSB);
        mAudioManager = (AudioManager)getApplicationContext().getSystemService(getApplicationContext().AUDIO_SERVICE);

        mediaSeekBar.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        notificationSeekBar.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
        systemSeekBar.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
        alarmSeekBar.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM));
        callSeekBar.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));

        // 확인용
        /*Log.i("MyTag:", "media:"+(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)));
        Log.i("MyTag:", "alarm:"+(mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM)));
        Log.i("MyTag:", "notice:"+(mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)));*/
    }


    private void initialDisplaySetting() { // default display 설정으로 초기화
        brightnessSeekBar = findViewById(R.id.brightnessSB);
        brightnessModeSwitch = findViewById(R.id.brightnessModeSwitch);
        try {
            // 밝기 얻어와서 세팅해주기
            int currentBrightness = Settings.System.getInt(getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, /* default value */ 50);
            brightnessSeekBar.setProgress((int) currentBrightness);
            //Log.i("MyTag:", "현재 밝기:"+currentBrightness+", sdk version:"+Build.VERSION.SDK_INT);

            // 밝기 모드 세팅해주기
            int brightnessMode = Settings.System.getInt(getApplicationContext().getContentResolver(), SCREEN_BRIGHTNESS_MODE);
            if (brightnessMode == SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                brightnessModeSwitch.setChecked(true);
                brightnessSeekBar.setEnabled(false); // 밝기 조절 X
            } else {
                brightnessModeSwitch.setChecked(false);
                brightnessSeekBar.setEnabled(true); // 밝기 조절 가능
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     *  안드로이드는 밝기가 최대 255까지 지원함
     *  따라서 seekBar의 maxValue도 255
     *  10보다 작을 경우 10으로 유지
     * @param val 밝기 값
     */
    private void setBrightness(int val) {
        if (val < 10) {
            val = 10;
        } else if (val > 255) {
            val = 255;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(getApplicationContext())) {
                //Log.i("MyTag:", "선택한 밝기:"+val);
                Settings.System.putInt(baseContext.getContentResolver(),
                        SCREEN_BRIGHTNESS, val);
            } else {  // 권한이 없는 경우 설정을 바꿀 수 없음
                // Log.e("MyTag:", "canWrite()불가임~ 앱 권한 - 시스템 설정 권한 확인해보기");
            }
        }
    }

    @Override
    public ContentResolver getContentResolver() {
        return super.getContentResolver();
    }

    public boolean checkSystemPermission() {
        boolean permission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   //23버전 이상
            permission = Settings.System.canWrite(this);
            //Log.d("test", "Can Write Settings: " + permission);
            if(!permission){  // 권한이 없는 경우 설정을 바꿀 수 없음
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 2127);
                permission = false;
            }
        } else {

        }
        return permission;
    }

    private class seekBarChangeListener  implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
//            Log.i("MyTag:", "선택한 애:"+seekBar.getId()+", 값:"+progress);
            if (seekBar == brightnessSeekBar) {
                setBrightness(progress);
            } else if (seekBar == mediaSeekBar) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, FLAG_PLAY_SOUND);
            } else if (seekBar == notificationSeekBar) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, progress, FLAG_PLAY_SOUND);
            } else if (seekBar == systemSeekBar) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, progress, FLAG_PLAY_SOUND);
            } else if (seekBar == alarmSeekBar) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, progress, FLAG_PLAY_SOUND);
            } else if (seekBar == callSeekBar) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, progress, FLAG_PLAY_SOUND);
            }
        }
        public void onStartTrackingTouch(SeekBar seekBar) {}
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }
}