package com.sigma.sudokuworld.sudoku;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.Chronometer;

public class GameTimer extends Chronometer {
    private long mPauseTime = 0;

    public GameTimer(Context context) {
        super(context);
    }

    public GameTimer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void start() {
        super.setBase(SystemClock.elapsedRealtime() - mPauseTime);
        super.start();
    }

    public void pause(){
        super.stop();
        mPauseTime = SystemClock.elapsedRealtime() - super.getBase();
    }

    public void setElapsedTime(long elapsedTime) {
        mPauseTime = elapsedTime;
    }

    public long getElapsedTime() {
        return SystemClock.elapsedRealtime() - super.getBase();
    }
}

