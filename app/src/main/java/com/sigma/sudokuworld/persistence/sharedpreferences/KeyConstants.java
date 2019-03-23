package com.sigma.sudokuworld.persistence.sharedpreferences;

public final class KeyConstants {

    /**
     * About Bundles:
     *
     * A "settings" Bundle should have:
     * Difficulty
     * GameMode
     * isAudioMode
     * Native words
     * Foreign words
     *
     * A "save" Bundle should have:
     * Difficulty
     * GameMode
     * Cell values
     * Solution
     * Locked cells
     * Native words
     * Foreign words
     */

    //Public index keys for saving data in bundle
    public static final String BOARD_LENGTH_KEY = "board";
    public static final String DIFFICULTY_KEY = "difficulty";
    public static final String MODE_KEY = "mode";
    public static final String AUDIO_KEY = "audio";
    public static final String SOUND_KEY = "sound";
    public static final String HINTS_KEY = "hints";
    public static final String RECTANGLE_KEY = "rect";
    public static final String CELL_VALUES_KEY = "values";
    public static final String SOLUTION_VALUES_KEY = "solution";
    public static final String LOCKED_CELLS_KEY = "locked";
    public static final String NATIVE_WORDS_KEY = "native";
    public static final String FOREIGN_WORDS_KEY = "foreign";

    //Public index key for continue game
    public static final String CONTINUE_KEY = "continue";

    public static final String SAVE_ID_KEY = "saveID";
    public static final String SET_ID_KEY = "setID";
    public static final String PAIR_ID_KEY = "pairID";

    public static final char CELL_LOCKED_FLAG = '~';
}
