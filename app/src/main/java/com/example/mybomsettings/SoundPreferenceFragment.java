package com.example.mybomsettings;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SeekBarPreference;


public class SoundPreferenceFragment extends PreferenceFragment {

    SharedPreferences prefs;

    ListPreference noticePreference;
    ListPreference alarmPreference;
    SeekBarPreference alarmSeekBar;
    SeekBarPreference mediaSeekBar;
    SeekBarPreference noticeSeekBar;

    private AudioManager mAudioManager;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_preference);
        noticePreference = (ListPreference)findPreference("notice_list");
        alarmPreference = (ListPreference)findPreference("alarm_list");
        mediaSeekBar = (SeekBarPreference)findPreference("mediaVolume");
        alarmSeekBar = (SeekBarPreference)findPreference("alarmVolume");
        noticeSeekBar = (SeekBarPreference)findPreference("noticeVolume");

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mAudioManager = (AudioManager) getActivity().getApplicationContext().getSystemService(getActivity().AUDIO_SERVICE);

        if (!prefs.getString("notice_list", "").equals("")) {
            noticePreference.setSummary(prefs.getString("notice_list", "없음"));
        }
        if (!prefs.getString("alarm_list", "").equals("")) {
            alarmPreference.setSummary(prefs.getString("alarm_list", "없음"));
        }
        if (!(prefs.getInt("mediaVolume", -1) == -1)) {
            mediaSeekBar.setValue(prefs.getInt("mediaVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)));
        }
        if (!(prefs.getInt("alarmVolume", -1) == -1)) {
            alarmSeekBar.setValue(prefs.getInt("alarmVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM)));
        }
        if (!(prefs.getInt("noticeVolume", -1) == -1)) {
            noticeSeekBar.setValue(prefs.getInt("noticeVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)));
        }

        prefs.registerOnSharedPreferenceChangeListener(prefListener);

        Log.i("MyTag:", "미디어:"+mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        Log.i("MyTag:", "알람:"+mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM));
        Log.i("MyTag:", "알림->시스템->통화:"+mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)); // CLOi가 알림(NOTIFICATION), 시스템(SYSTEM)이 아예 없음 에러남

        // android 폰 - max : 15, 15, 15,         CLOi - max : 10,10,7
       /* Log.i("MyTag:", "미디어 max:"+mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        Log.i("MyTag:", "알람 max:"+mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        Log.i("MyTag:", "알림->시스템->통화 max:"+mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)); // CLOi가 알림(NOTIFICATION), 시스템(SYSTEM)이 아예 없음 에러남*/
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
                        (int)(prefs.getInt("mediaVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC))),
                        AudioManager.FLAG_PLAY_SOUND);
                /*mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        (int)(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) *
                                (0.01*prefs.getInt("mediaVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)))),
                        AudioManager.FLAG_PLAY_SOUND);*/
            }
            if (key.equals("alarmVolume")) {
                alarmSeekBar.setValue(prefs.getInt("alarmVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM)));

                mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                        (int)(prefs.getInt("alarmVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM))),
                        AudioManager.FLAG_PLAY_SOUND);
            }
            if (key.equals("noticeVolume")) {
                noticeSeekBar.setValue(prefs.getInt("noticeVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)));

                mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        (int)(prefs.getInt("noticeVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL))),
                        AudioManager.FLAG_PLAY_SOUND);
            }
        }
    };
}
