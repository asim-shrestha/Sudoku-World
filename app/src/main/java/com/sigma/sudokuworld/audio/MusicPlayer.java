package com.sigma.sudokuworld.audio;

import android.content.Context;
import android.media.MediaPlayer;
import com.sigma.sudokuworld.R;

public class MusicPlayer {
    private MediaPlayer mMediaPlayer;

    public MusicPlayer(Context context) {
        mMediaPlayer = MediaPlayer.create(context, R.raw.backgroundmusic);
        mMediaPlayer.setLooping(true);
    }

    public void start() {
        mMediaPlayer.start();
    }

    public void pause() {
        mMediaPlayer.pause();
    }
}
