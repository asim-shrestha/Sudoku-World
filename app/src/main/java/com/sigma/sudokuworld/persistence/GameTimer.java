package com.sigma.sudokuworld.persistence;

import android.content.Context;
import android.os.SystemClock;
import android.widget.Chronometer;


public class GameTimer extends Chronometer {
    private long timeInterval;
    public GameTimer(Context context) {
        super(context);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    public void pause(){
        timeInterval = SystemClock.elapsedRealtime() - getBase();
        stop();
    }

    public void restart(){
        setBase(SystemClock.elapsedRealtime() - timeInterval);
        start();
    }

}

