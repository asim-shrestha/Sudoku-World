package com.sigma.sudokuworld;

import com.sigma.sudokuworld.game.GameDifficulty;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameDifficultyUnitTest {

    @Test
    public void fromString() {
        assertEquals(GameDifficulty.EASY, GameDifficulty.fromString("easy"));
        assertEquals(GameDifficulty.EASY, GameDifficulty.fromString("EaSY"));
        assertEquals(GameDifficulty.EASY, GameDifficulty.fromString(" eaSy "));

        assertEquals(GameDifficulty.MEDIUM, GameDifficulty.fromString("medium"));
        assertEquals(GameDifficulty.MEDIUM, GameDifficulty.fromString("Medium"));
        assertEquals(GameDifficulty.MEDIUM, GameDifficulty.fromString(" medium"));

        assertEquals(GameDifficulty.HARD, GameDifficulty.fromString("hard"));
        assertEquals(GameDifficulty.HARD, GameDifficulty.fromString("Hard  "));
        assertEquals(GameDifficulty.HARD, GameDifficulty.fromString(" HARD  "));
    }
}