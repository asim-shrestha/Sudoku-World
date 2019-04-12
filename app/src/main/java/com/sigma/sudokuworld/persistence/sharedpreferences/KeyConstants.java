package com.sigma.sudokuworld.persistence.sharedpreferences;

public final class KeyConstants {

    //Game keys
    public static final String BOARD_LENGTH_KEY = "board";
    public static final String DIFFICULTY_KEY = "difficulty";
    public static final String MODE_KEY = "mode";
    public static final String CELL_VALUES_KEY = "values";
    public static final String SOLUTION_VALUES_KEY = "solution";
    public static final String LOCKED_CELLS_KEY = "locked";
    public static final String GAME_TIME_KEY = "time";

    //Db keys
    public static final String SAVE_ID_KEY = "saveID";
    public static final String SET_ID_KEY = "setID";
    public static final String PAIR_ID_KEY = "pairID";

    //Settings keys
    public static final String AUDIO_KEY = "audio";
    public static final String SFX_KEY = "sfx";
    public static final String MUSIC_KEY = "music";
    public static final String HINTS_KEY = "hints";
    public static final String RECTANGLE_KEY = "rect";

    public static final char CELL_LOCKED_FLAG = '~';
}
