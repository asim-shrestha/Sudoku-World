package com.sigma.sudokuworld.sudoku;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.sigma.sudokuworld.BaseActivity;
import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.persistence.sharedpreferences.KeyConstants;

public class SudokuWin extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku_win);
        Intent intent = getIntent();

        //Get difficulty text
        String gameDifficulty = "Difficulty: ";
        gameDifficulty = gameDifficulty.concat(intent.getStringExtra(KeyConstants.DIFFICULTY_KEY));

        //Get time text
        Long gameTimer = (intent.getLongExtra(KeyConstants.GAME_TIME_KEY, 1));
        //Get proper time formatting
        gameTimer = gameTimer/1000;
        String gameTime = Long.toString(gameTimer/60);
        gameTime = gameTime.concat(" minutes and ");
        gameTime = gameTime.concat(Long.toString(gameTimer % 60));
        gameTime = gameTime.concat(" seconds");

        //Grab text views
        TextView timeText = findViewById(R.id.timeText);
        TextView difficultyText = findViewById(R.id.difficultyText);

        difficultyText.setText(gameDifficulty);
        timeText.setText(gameTime);

    }
}
