package com.sigma.sudokuworld.sudoku;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.persistence.GameRepository;
import com.sigma.sudokuworld.persistence.db.entities.Game;

public class SudokuWin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku_win);
    }
}
