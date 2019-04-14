package com.sigma.sudokuworld.sudoku.singleplayer;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.sigma.sudokuworld.game.GameDifficulty;
import com.sigma.sudokuworld.persistence.sharedpreferences.KeyConstants;
import com.sigma.sudokuworld.sudoku.SudokuActivity;
import com.sigma.sudokuworld.sudoku.SudokuWin;
import com.sigma.sudokuworld.viewmodels.SinglePlayerViewModel;
import com.sigma.sudokuworld.viewmodels.factories.SingleplayerViewModelFactory;

public class SinglePlayerActivity extends SudokuActivity {
    protected long mSaveID = 0;
    protected SinglePlayerViewModel mSinglePlayerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mSaveID = savedInstanceState.getLong(KeyConstants.SAVE_ID_KEY);
        } else {
            Intent intent = getIntent();
            mSaveID = intent.getLongExtra(KeyConstants.SAVE_ID_KEY, 0);
        }

        SingleplayerViewModelFactory factory = new SingleplayerViewModelFactory(getApplication(), mSaveID);
        mSinglePlayerViewModel = ViewModelProviders.of(this, factory).get(SinglePlayerViewModel.class);

        super.setGameViewModel(mSinglePlayerViewModel);
        super.mLongClickHandler.setTTSLanguage(
                mSinglePlayerViewModel.getNativeLanguage().getCode(),
                mSinglePlayerViewModel.getForeignLanguage().getCode()
        );

        mSinglePlayerViewModel.isGameWon().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isGameWon) {
                if (isGameWon != null && isGameWon)
                    showGameWin();
                }
            });

        mGameTimer.setElapsedTime(mSinglePlayerViewModel.getElapsedTime());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the current state of the Sudoku board
        outState.putLong(KeyConstants.SAVE_ID_KEY, mSaveID);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mGameTimer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mGameTimer.pause();
        mSinglePlayerViewModel.setElapsedTime(mGameTimer.getElapsedTime());
    }

    private void showGameWin() {

        //The Sudoku board is correct
        mSoundPlayer.playCorrectSound();

        //Start SudokuWin activity
        Intent intent = new Intent(this, SudokuWin.class);

        //Place game time into intent
        intent.putExtra(KeyConstants.GAME_TIME_KEY, mGameTimer.getElapsedTime());

        //Place game difficulty into intent
        String gameDifficulty = GameDifficulty.toString(mSinglePlayerViewModel.getGameDifficulty());
        intent.putExtra(KeyConstants.DIFFICULTY_KEY, gameDifficulty);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);

        //End activity and delete game instance
        mSinglePlayerViewModel.deleteGame();
        this.finish();
    }
}
