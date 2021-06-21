package com.example.mybomsettings;

import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

import static android.media.AudioManager.ADJUST_UNMUTE;
import static android.media.AudioManager.FLAG_PLAY_SOUND;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
import static com.example.mybomsettings.DisplayList.baseContext;


public class SystemPreferenceFragment extends PreferenceFragment {

    SharedPreferences prefs;

    SeekBarPreference brightnessSeekBar;
    SwitchPreference brightnessModeSwitch;

    ListPreference noticePreference;
    ListPreference alarmPreference;
    SeekBarPreference alarmSeekBar;
    SeekBarPreference mediaSeekBar;
    SeekBarPreference noticeSeekBar;

    private AudioManager mAudioManager;
    MediaPlayer mediaPlayer;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_preference);
        noticePreference = (ListPreference)findPreference("notice_list");
        alarmPreference = (ListPreference)findPreference("alarm_list");
        mediaSeekBar = (SeekBarPreference)findPreference("mediaVolume");
        alarmSeekBar = (SeekBarPreference)findPreference("alarmVolume");
        noticeSeekBar = (SeekBarPreference)findPreference("noticeVolume");
        brightnessSeekBar = (SeekBarPreference)findPreference("brightness");
        brightnessModeSwitch = (SwitchPreference)findPreference("brightnessMode");


        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mAudioManager = (AudioManager) getActivity().getApplicationContext().getSystemService(getActivity().getApplicationContext().AUDIO_SERVICE);

        Log.i("MyTag:", "media:"+(prefs.getInt("mediaVolume", -1)));
        Log.i("MyTag:", "alarm:"+(prefs.getInt("alarmVolume", -1)));
        Log.i("MyTag:", "notice:"+(prefs.getInt("noticeVolume", -1)));
        // 기본값 세팅
        if (!prefs.getString("notice_list", "").equals("")) {
            noticePreference.setSummary(prefs.getString("notice_list", "없음"));
        }
        if (!prefs.getString("alarm_list", "").equals("")) {
            alarmPreference.setSummary(prefs.getString("alarm_list", "없음"));
        }
        if (!(prefs.getInt("mediaVolume", -1) == -1)) {
            mediaSeekBar.setValue(prefs.getInt("mediaVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)));
        } else { // prefs 존재하지 않을 경우 현재 값으로 가져옴
            mediaSeekBar.setValue(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
        if (!(prefs.getInt("alarmVolume", -1) == -1)) {
            alarmSeekBar.setValue(prefs.getInt("alarmVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM)));
        } else {
            alarmSeekBar.setValue(mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM));
        }
        if (!(prefs.getInt("noticeVolume", -1) == -1)) {
            noticeSeekBar.setValue(prefs.getInt("noticeVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)));
        }else {
            noticeSeekBar.setValue(mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
        }
        if (!(prefs.getInt("brightness", 0) == -1)) {
            int  currentBrightness = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, /* default value */ 50);
            brightnessSeekBar.setValue(prefs.getInt("brightness", currentBrightness));
        }
        if (!(prefs.getBoolean("brightnessMode", true)) || (prefs.getBoolean("brightnessMode", true))) {
            int brightnessMode = SCREEN_BRIGHTNESS_MODE_AUTOMATIC; // SCREEN_BRIGHTNESS_MODE_AUTOMATIC = 1 default : AUTO
            try {
                brightnessMode = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(), SCREEN_BRIGHTNESS_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            if (brightnessMode == SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                brightnessModeSwitch.setChecked(prefs.getBoolean("brightnessMode", true));
                brightnessSeekBar.setEnabled(false); // 밝기 조절 못하게 비활성화
            } else {
                brightnessModeSwitch.setChecked(prefs.getBoolean("brightnessMode", false));
                brightnessSeekBar.setEnabled(true); // 밝기 조절 가능하게 활성화
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(prefListener);

        Log.i("MyTag:", "미디어:"+mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        Log.i("MyTag:", "알람:"+mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM));
        Log.i("MyTag:", "알림->시스템->통화:"+mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)); // CLOi가 알림(NOTIFICATION), 시스템(SYSTEM)이 아예 없음 에러남

        Log.i("MyTag:", "현재 밝기:"+Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 50));
        // android 폰 - max : 15, 15, 15,         CLOi - max : 10,10,7
       /* Log.i("MyTag:", "미디어 max:"+mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        Log.i("MyTag:", "알람 max:"+mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        Log.i("MyTag:", "알림->시스템->통화 max:"+mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)); // CLOi가 알림(NOTIFICATION), 시스템(SYSTEM)이 아예 없음 에러남*/


        Log.i("MyTag:", "-------------변경전-------------\nisVolumeFixed (): "+mAudioManager.isVolumeFixed());
        Log.i("MyTag:", "noti-max : "+mAudioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
        Log.i("MyTag:", "system-max : "+mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
        Log.i("MyTag:", "STREAM_SYSTEM-is mute : "+mAudioManager.isStreamMute(AudioManager.STREAM_SYSTEM));
        Log.i("MyTag:", "STREAM_NOTIFICATION-is mute : "+mAudioManager.isStreamMute(AudioManager.STREAM_NOTIFICATION));
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, 	ADJUST_UNMUTE, 	FLAG_PLAY_SOUND);
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, 	ADJUST_UNMUTE, 	FLAG_PLAY_SOUND);
        Log.i("MyTag:", "-------------변경후-------------\nSTREAM_SYSTEM-is mute : "+mAudioManager.isStreamMute(AudioManager.STREAM_SYSTEM));
        Log.i("MyTag:", "STREAM_NOTIFICATION-is mute : "+mAudioManager.isStreamMute(AudioManager.STREAM_NOTIFICATION));
        Log.i("MyTag:", "system : "+mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
        Log.i("MyTag:", "STREAM_NOTIFICATION : "+mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));

        mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 7, FLAG_PLAY_SOUND);
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 7, FLAG_PLAY_SOUND);
        Log.i("MyTag:", "-------------7로 변경후-------------\nsystem : "+mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
        Log.i("MyTag:", "STREAM_NOTIFICATION : "+mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
        Log.i("MyTag:", "STREAM_RING : "+mAudioManager.getStreamVolume(AudioManager.STREAM_RING));
        Log.i("MyTag:", "STREAM_DTMF : "+mAudioManager.getStreamVolume(AudioManager.STREAM_DTMF));
        Log.i("MyTag:", "STREAM_VOICE_CALL : "+mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
        Log.i("MyTag:", "STREAM_ALARM : "+mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM));

/*
        mediaPlayer.stop();
        mediaPlayer.reset();
*/

    }

    private SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key.equals("notice_list")) {
                noticePreference.setSummary(prefs.getString("notice_list", "없음"));
            }
            if(key.equals("alarm_list")) {
                alarmPreference.setSummary(prefs.getString("alarm_list", "없음"));
            }
            if (key.equals("mediaVolume")) {
                mediaSeekBar.setValue(prefs.getInt("mediaVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)));

                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        prefs.getInt("mediaVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)),
                        FLAG_PLAY_SOUND);
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                /*mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        (int)(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) *
                                (0.01*prefs.getInt("mediaVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)))),
                        AudioManager.FLAG_PLAY_SOUND);*/
            }
            if (key.equals("alarmVolume")) {
                alarmSeekBar.setValue(prefs.getInt("alarmVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM)));

                mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                        (int)(prefs.getInt("alarmVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM))),
                        FLAG_PLAY_SOUND); // 소리 재생 플래그 실행 안됨
                // 아래 기본 소리 안멈추고 계속남 -> 잠깐만 소리 나게 변경해야 함
                /*Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                Ringtone rt = RingtoneManager.getRingtone(getActivity().getApplicationContext(), alarm);
                rt.play();
                Log.i("MyTag:", "uri:"+alarm);*/

                // MediaPlayer 객체 할당
                 mediaPlayer = MediaPlayer.create(getActivity().getApplicationContext(), R.raw.dingdong);
                mediaPlayer.start();
            }
            if (key.equals("noticeVolume")) {
                noticeSeekBar.setValue(prefs.getInt("noticeVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)));

                mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        (int)(prefs.getInt("noticeVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL))),
                        FLAG_PLAY_SOUND);

                /*Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone rt = RingtoneManager.getRingtone(getActivity().getApplicationContext(), notification);
                rt.play();
                Log.i("MyTag:", "uri:"+notification);*/
            }
            if (key.equals("brightness")) {
                int  currentBrightness = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, /* default value */ 50);
                brightnessSeekBar.setValue(prefs.getInt("brightness", currentBrightness));

                if (currentBrightness < 10) {
                    currentBrightness = 10;
                } else if (currentBrightness > 255) {
                    currentBrightness = 255;
                }

                // 실제로 바꿔주기 동작
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.System.canWrite(getActivity().getApplicationContext())) {
                        Log.i("MyTag:", "선택한 밝기:"+prefs.getInt("brightness", currentBrightness));
                        Settings.System.putInt(getContext().getContentResolver(),
                                SCREEN_BRIGHTNESS, prefs.getInt("brightness", currentBrightness));
                    } else {
                        Log.e("MyTag:", "canWrite()불가임~ 앱 권한 - 시스템 설정 권한 확인해보기");
                    }
                }
            }
            if (key.equals("brightnessMode")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.System.canWrite(getActivity().getApplicationContext())) {
                        if (brightnessModeSwitch.isChecked() == true) { // 자동 밝기 checked
                            Settings.System.putInt(getContext().getContentResolver(), SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                            brightnessSeekBar.setEnabled(false); // 밝기 조절 X
                            Log.i("MyTag:", "자동밝기 켬");
                        } else { // 자동 밝기 X
                            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(), SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_MANUAL);
                            brightnessSeekBar.setEnabled(true); // 밝기 조절 가능
                            Log.i("MyTag:", "자동밝기 끔");
                        }
                    }  else {
                        Log.e("MyTag:", "canWrite()불가임~ 앱 권한 - 시스템 설정 권한 확인해보기");
                    }
                } else {
                    Log.e("MyTag:", "버전 아님");
                }
            }
        }
    };
}
