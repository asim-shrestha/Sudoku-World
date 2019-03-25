package com.sigma.sudokuworld.game;

public enum BoardSize {
    BOARD_SIZE0,
    BOARD_SIZE1,
    BOARD_SIZE2,
    BOARD_SIZE3,
    BOARD_SIZE4;

    public static int getBoardLength(BoardSize boardSize) {
        switch (boardSize){
            case BOARD_SIZE0: return 4;
            case BOARD_SIZE1: return 6;
            case BOARD_SIZE2: return 9;
            case BOARD_SIZE3: return 12;
            case BOARD_SIZE4: return 16;
            default: return 9;
        }
    }

    public static BoardSize getBoardSize(int boardLength) {
        switch (boardLength){
            case 4: return BOARD_SIZE0;
            case 6: return BOARD_SIZE1;
            case 9: return BOARD_SIZE2;
            case 12: return BOARD_SIZE3;
            case 16: return BOARD_SIZE4;
            default: return BOARD_SIZE2;
        }
    }
}
