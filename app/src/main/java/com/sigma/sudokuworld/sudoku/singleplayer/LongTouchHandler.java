package com.sigma.sudokuworld.sudoku.singleplayer;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.Toast;

import com.sigma.sudokuworld.game.GameMode;
import com.sigma.sudokuworld.viewmodels.SinglePlayerViewModel;

import java.util.Locale;

public class LongTouchHandler {
    private TextToSpeech mTTS;
    private boolean mIsComprehensionMode;
    private Context mContext;

    public LongTouchHandler(Context context, final GameMode gameMode) {
        mContext = context;

        //Initializing TTS
        mTTS = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){

                    //Set Lang
                    if (gameMode == GameMode.NATIVE) {
                        mTTS.setLanguage(new Locale("fr"));
                    } else {
                        mTTS.setLanguage(new Locale("en"));
                    }
                }
            }
        });

        //Check if comprehension mode is on
        mIsComprehensionMode = true;
    }

    public void destroyLongTouchHandler() {
        if(mTTS != null){
            mTTS.stop();
            mTTS.shutdown();
        }
    }

    public boolean handleGridLongClick(SinglePlayerViewModel mSinglePlayerViewModel, int cellTouched){
        if (mSinglePlayerViewModel.isLockedCell(cellTouched)) {
            String text = mSinglePlayerViewModel.getMappedString(
                    mSinglePlayerViewModel.getCellValue(cellTouched),
                    GameMode.opposite(mSinglePlayerViewModel.getGameMode())
            );
            mTTS.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
        }
        return true;
    }

    public boolean handleButtonLongClick(Button button){
        if (mIsComprehensionMode){
            mTTS.speak(button.getText().toString(),TextToSpeech.QUEUE_FLUSH,null,null);
        }

        else {
            Toast.makeText(mContext, button.getText(), Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
