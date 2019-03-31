package com.sigma.sudokuworld.sudoku.singleplayer;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import com.sigma.sudokuworld.game.GameMode;
import com.sigma.sudokuworld.persistence.sharedpreferences.PersistenceService;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class LongClickHandler {
    private Context mContext;
    private GameMode mGameMode;
    private TextToSpeech mTTS;

    private Timer mTimer;
    private TimerTask mGridLongClickTask;

    private String mCellString;

    public LongClickHandler(Context context, GameMode gameMode) {
        mContext = context;
        mGameMode = gameMode;
        initTTS();
        initLooper();
    }

    private boolean isComprehensionMode() {
        return PersistenceService.loadAudioModeSetting(mContext);
    }

    private void initTTS(){
        if (mTTS == null) {
            mTTS = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        //Set Lang
                        if (mGameMode == GameMode.NATIVE) {
                            mTTS.setLanguage(new Locale("fr"));
                        } else {
                            mTTS.setLanguage(new Locale("en"));
                        }
                    }
                }
            });
        }
    }

    private void initLooper(){
        //Create a looper if there is none
        //Looper must be in thread to make toasts
        if( mContext.getMainLooper()== null){ Looper.prepare(); }
    }

    private void resetGridLongClickTimer(){
        mGridLongClickTask = new TimerTask() {
            @Override
            public void run() {
                //Executes when timer finishes
                mTimer.purge();
                Activity mainActivity = (Activity) mContext;

                //Run on ui thread to properly display toasts
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gridLongClick();

                    }
                });

            }
        };
    }

    public void handleGridLongClick(String cellString){
        mCellString = cellString;
        if(mTimer == null){
            //Create a new timer to be run
            mTimer = new Timer();
            resetGridLongClickTimer();
            mTimer.schedule(mGridLongClickTask, 650);
        }
    }

    private void gridLongClick(){
        //Runs after the timer has successfully finished
        displayText(mCellString);
    }

    public void cancelGridLongClick() {
        if (mTimer != null) {
            //End scheduled task and delete timer
            mTimer.cancel();
            mTimer = null;
        }
    }

    public boolean handleButtonLongClick( String buttonText ){
        //Get the opposite language value and display it
        displayText(buttonText);
        return true;
    }

    private void displayText(String text) {
        if (isComprehensionMode()){
            initTTS();
            mTTS.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
        }

        else {
            initLooper();
            Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
        }
    }

    public void destroyLongTouchHandler() {
        if(mTTS != null){
            mTTS.stop();
            mTTS.shutdown();
        }
    }

}
