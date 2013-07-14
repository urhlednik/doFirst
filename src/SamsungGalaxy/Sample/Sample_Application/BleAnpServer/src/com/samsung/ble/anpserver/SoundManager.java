/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samsung.ble.anpserver;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Environment;
import android.util.Log;

public class SoundManager {

    static private SoundManager _instance;
    private static SoundPool mSoundPool;
    private static AudioManager mAudioManager;
    private static Context mContext;
    private static int soundId;
    private int sound_voulume;
    private static final String SOUND_PATH = "/media/audio/ringtones/02_Fog_on_the_water.ogg";

    static synchronized public SoundManager getInstance() {
        if (_instance == null)
            _instance = new SoundManager();
        return _instance;
    }

    public void initSounds(Context theContext) {
        mContext = theContext;
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mSoundPool.setOnLoadCompleteListener(new LoadListener());

    }

    private class LoadListener implements SoundPool.OnLoadCompleteListener {

        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            float volFloat = ((float) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
                    / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            if (sound_voulume == 0) {
                volFloat = 0;
            } else if (sound_voulume == 131072) {
                volFloat = 1.0f;
            }
            Log.e("LoadListener", "Value of Volume " + volFloat);
        }
    }

    public void addSound(int alert_level) {
        String filePath = Environment.getRootDirectory() + SOUND_PATH;
        sound_voulume = alert_level;
        Log.e("SoundManager", "Load Sound :" + filePath);
        soundId = mSoundPool.load(filePath, 1);
    }

    public void releaseSound() {
        if (mSoundPool != null)
            mSoundPool.release();
    }

    public void stopSound() {
        mSoundPool.stop(soundId);
    }

    public static void cleanup() {
        mSoundPool.release();
        mSoundPool = null;
        mAudioManager.unloadSoundEffects();
        _instance = null;

    }

}