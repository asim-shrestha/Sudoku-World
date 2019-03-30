package com.sigma.sudokuworld;

import android.app.Application;
import com.sigma.sudokuworld.audio.MusicPlayer;

public class SudokuWorldApplication extends Application {
    private MusicPlayer mMusicPlayer;

    @Override
    public void onCreate() {
        super.onCreate();

        mMusicPlayer = new MusicPlayer(this);
    }

    public MusicPlayer getMusicPlayer() {
        return mMusicPlayer;
    }
}
