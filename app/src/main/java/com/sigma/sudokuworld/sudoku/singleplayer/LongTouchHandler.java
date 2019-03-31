package com.sigma.sudokuworld.sudoku.singleplayer;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.Toast;

import com.sigma.sudokuworld.game.GameMode;
import com.sigma.sudokuworld.persistence.sharedpreferences.PersistenceService;
import com.sigma.sudokuworld.viewmodels.GameViewModel;
import com.sigma.sudokuworld.viewmodels.SinglePlayerViewModel;

import java.util.Locale;
import java.util.Timer;

public class LongTouchHandler {
    private Context mContext;
    private GameViewModel mGameViewModel;
    private TextToSpeech mTTS;
    private Timer mTimer;

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

        mTimer = new Timer();
    }

    private boolean isComprehensionMode() {
        return PersistenceService.loadAudioModeSetting(mContext);
    }

    public void destroyLongTouchHandler() {
        if(mTTS != null){
            mTTS.stop();
            mTTS.shutdown();
        }
    }

    public boolean handleGridLongClick(int cellTouched){
        if (mGameViewModel.isLockedCell(cellTouched)) {
            String text = mGameViewModel.getMappedString(
                    mGameViewModel.getCellValue(cellTouched),
                    GameMode.opposite(mGameViewModel.getGameMode())
            );
            if(isComprehensionMode())
            {
                mTTS.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
            }
            else
            {
                Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                return true;
            }

        }
        return true;
    }

    public boolean handleButtonLongClick(Button button){
        if (isComprehensionMode()){
            mTTS.speak(button.getText().toString(),TextToSpeech.QUEUE_FLUSH,null,null);
        }

        else {
            String text = mGameViewModel.getMappedString(
                    button.getText(),
                    GameMode.opposite(mGameViewModel.getGameMode()));
            Toast.makeText(mContext, button.getText(), Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
