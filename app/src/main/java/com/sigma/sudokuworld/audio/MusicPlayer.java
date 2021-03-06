package com.sigma.sudokuworld.audio;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.persistence.sharedpreferences.PersistenceService;

public class MusicPlayer {
    private MediaPlayer mMediaPlayer;
    private boolean mShouldPlayMusic;

    public MusicPlayer(Context context) {
        mMediaPlayer = MediaPlayer.create(context, R.raw.backgroundmusic);
        mMediaPlayer.setLooping(true);

        //Must start the thread here
        mMediaPlayer.start();
        mMediaPlayer.pause();

        mShouldPlayMusic = PersistenceService.loadMusicEnabledSetting(context);
    }

    public void start() {
        if (mShouldPlayMusic) mMediaPlayer.start();
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public void setMusicEnabled(boolean isEnabled) {

        //If music is off and should turn on
        if (!mShouldPlayMusic && isEnabled) {
            mShouldPlayMusic = true;
            start();
        }

        //If music is on and should turn off
        else if (mShouldPlayMusic && !isEnabled) {
            mShouldPlayMusic = false;
            pause();
        }
    }
}
