package com.sigma.sudokuworld.sudoku.singleplayer;

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
    private TimerTask mGridLongClickTimer;

    private int mCellTouched;

    public LongTouchHandler(Context context, GameViewModel gameViewModel) {
        mContext = context;
        mGameViewModel = gameViewModel;

        //Initializing TTS
        mTTS = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
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

    private boolean isComprehensionMode() {
        return PersistenceService.loadAudioModeSetting(mContext);
    }

    private void resetGridLongClickTimer(){
        mGridLongClickTimer = new TimerTask() {
            @Override
            public void run() {
                mTimer.purge();
                if (mGameViewModel.isLockedCell(mCellTouched)) {
                    String text = mGameViewModel.getMappedString(
                            mGameViewModel.getCellValue(mCellTouched),
                            GameMode.opposite(mGameViewModel.getGameMode())
                    );
                    if(isComprehensionMode())
                    {
                        mTTS.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
                    }
                    else
                    {
                        prepareLooper();
                        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }
    private void prepareLooper(){
        //Create a looper if there is none
        if(Looper.myLooper() == null){ Looper.prepare(); }
    }
    public void handleGridLongClick(int cellTouched){
        mCellTouched = cellTouched;
        if(mTimer == null){
            //Create a new timer to be run
            mTimer = new Timer();
            resetGridLongClickTimer();
            mTimer.schedule(mGridLongClickTimer, 650);
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
            mTTS.speak(buttonText,TextToSpeech.QUEUE_FLUSH,null,null);
        }

        else {
            prepareLooper();
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
