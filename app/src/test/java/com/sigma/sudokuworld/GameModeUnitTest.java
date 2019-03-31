package com.sigma.sudokuworld;

import com.sigma.sudokuworld.game.GameMode;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameModeUnitTest {

    @Test
    public void opposite() {
        assertEquals(GameMode.NATIVE, GameMode.opposite(GameMode.FOREIGN));
        assertEquals(GameMode.NATIVE, GameMode.opposite(GameMode.FOREIGN));

        assertNotEquals(GameMode.NATIVE, GameMode.opposite(GameMode.NATIVE));
        assertNotEquals(GameMode.FOREIGN, GameMode.opposite(GameMode.FOREIGN));

        assertEquals(GameMode.NUMBERS, GameMode.opposite(GameMode.NUMBERS));
        assertNotEquals(GameMode.FOREIGN, GameMode.opposite(GameMode.NUMBERS));
        assertNotEquals(GameMode.NATIVE, GameMode.opposite(GameMode.NUMBERS));
    }

    @Test
    public void fromString() {
        assertEquals(GameMode.NUMBERS, GameMode.fromString("numbers"));
        assertEquals(GameMode.NUMBERS, GameMode.fromString("NUMberS"));
        assertEquals(GameMode.NUMBERS, GameMode.fromString(" NUMBERS  "));

        assertEquals(GameMode.NATIVE, GameMode.fromString("native"));
        assertEquals(GameMode.NATIVE, GameMode.fromString("Native"));
        assertEquals(GameMode.NATIVE, GameMode.fromString(" native  "));

        assertEquals(GameMode.FOREIGN, GameMode.fromString("foreign"));
        assertEquals(GameMode.FOREIGN, GameMode.fromString("Foreign"));
        assertEquals(GameMode.FOREIGN, GameMode.fromString(" Foreign  "));
    }
}