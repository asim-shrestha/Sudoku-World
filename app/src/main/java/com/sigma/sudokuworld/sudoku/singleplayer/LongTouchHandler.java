package com.sigma.sudokuworld.sudoku.singleplayer;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.Toast;

import com.sigma.sudokuworld.game.GameMode;
import com.sigma.sudokuworld.persistence.sharedpreferences.PersistenceService;
import com.sigma.sudokuworld.viewmodels.GameViewModel;
import com.sigma.sudokuworld.viewmodels.SinglePlayerViewModel;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class LongTouchHandler {
    private Context mContext;
    private GameViewModel mGameViewModel;
    private TextToSpeech mTTS;

    private Timer mTimer;
    private TimerTask mGridLongClickTask;

    private int mCellTouched;

    public LongTouchHandler(Context context, GameViewModel gameViewModel) {
        mContext = context;
        mGameViewModel = gameViewModel;
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
                        if (mGameViewModel.getGameMode() == GameMode.NATIVE) {
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
        if( Looper.myLooper() == null){ Looper.prepare(); }
    }

    public void handleGridLongClick(int cellTouched){
        mCellTouched = cellTouched;
        if(mTimer == null){
            //Create a new timer to be run
            mTimer = new Timer();
            resetGridLongClickTimer();
            mTimer.schedule(mGridLongClickTask, 650);
        }
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

    private void gridLongClick(){
        if (mGameViewModel.isLockedCell(mCellTouched)) {
            String text = mGameViewModel.getMappedString(
                    mGameViewModel.getCellValue(mCellTouched),
                    GameMode.opposite(mGameViewModel.getGameMode()));

            if (isComprehensionMode()) {
                initTTS();
                mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                initLooper();
                Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void cancelGridLongClick() {
        if (mTimer != null) {
            //End scheduled task and delete timer
            mTimer.cancel();
            mTimer = null;
        }
    }

    public boolean handleButtonLongClick( int buttonValue ){
        //Get the opposite language value
        String buttonText = mGameViewModel.getMappedString(
                buttonValue,
                GameMode.opposite(mGameViewModel.getGameMode()));

        if (isComprehensionMode()){
            initTTS();
            mTTS.speak(buttonText,TextToSpeech.QUEUE_FLUSH,null,null);
        }

        else {
            initLooper();
            Toast.makeText(mContext, buttonText, Toast.LENGTH_SHORT).show();
        }
        return true;
    }


    public void destroyLongTouchHandler() {
        if(mTTS != null){
            mTTS.stop();
            mTTS.shutdown();
        }
    }
}
