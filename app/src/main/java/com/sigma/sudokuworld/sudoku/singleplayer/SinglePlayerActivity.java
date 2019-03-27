package com.sigma.sudokuworld.sudoku.singleplayer;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import com.sigma.sudokuworld.persistence.sharedpreferences.KeyConstants;
import com.sigma.sudokuworld.sudoku.SudokuActivity;
import com.sigma.sudokuworld.viewmodels.SinglePlayerViewModel;
import com.sigma.sudokuworld.viewmodels.SingleplayerViewModelFactory;

public abstract class SinglePlayerActivity extends SudokuActivity {
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
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the current state of the Sudoku board
        outState.putLong(KeyConstants.SAVE_ID_KEY, mSaveID);
    }
}