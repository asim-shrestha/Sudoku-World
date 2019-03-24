package com.sigma.sudokuworld.sudoku;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;

import com.sigma.sudokuworld.game.GameMode;
import com.sigma.sudokuworld.viewmodels.SinglePlayerViewModel;

import java.util.Locale;

public class AudioSudokuActivity extends SinglePlayerActivity {

    private TextToSpeech mTTS;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTTS();

        super.mSudokuGridView.setOnLongClickListener(longClickListener);

        for (Button button : mInputButtons) {
            button.setOnLongClickListener(buttonLongClickListener);
        }
    }

    @Override
    protected void onDestroy() {
        if(mTTS != null){
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }

    View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (mSinglePlayerViewModel.isLockedCell(cellTouched)) {
                String text = mSinglePlayerViewModel.getMappedString(
                        mSinglePlayerViewModel.getCellValue(cellTouched),
                        GameMode.opposite(mSinglePlayerViewModel.getGameMode())
                );
                mTTS.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
            }
            return true;
        }
    };

    Button.OnLongClickListener buttonLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            Button button = (Button) v;
            mTTS.speak(button.getText().toString(),TextToSpeech.QUEUE_FLUSH,null,null);
            return true;
        }
    };

    private void initTTS() {

        //Initializing TTS
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){

                    //Set Lang
                    if (mSinglePlayerViewModel.getGameMode() == GameMode.NATIVE) {
                        mTTS.setLanguage(new Locale("fr"));
                    } else {
                        mTTS.setLanguage(new Locale("en"));
                    }
                }
            }
        });
    }
}
