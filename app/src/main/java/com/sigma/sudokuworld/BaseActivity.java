package com.sigma.sudokuworld;

import android.support.v7.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();

        ((SudokuWorldApplication) getApplication()).getMusicPlayer().start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        ((SudokuWorldApplication) getApplication()).getMusicPlayer().pause();
    }
}
