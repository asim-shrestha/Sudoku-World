package com.sigma.sudokuworld;

import com.sigma.sudokuworld.game.GameDifficulty;
import com.sigma.sudokuworld.game.gen.PuzzleGenerator;
import com.sigma.sudokuworld.game.gen.PuzzleGenerator.Puzzle;
import org.junit.Test;


import static org.junit.Assert.*;


public class PuzzleGeneratorUnitTest {

    @Test
    public void puzzleValidity() {
        checkValidPuzzle(getPuzzle(4));
        checkValidPuzzle(getPuzzle(6));
        checkValidPuzzle(getPuzzle(9));
        checkValidPuzzle(getPuzzle(12));
        checkValidPuzzle(getPuzzle(16));
    }

    @Test
    public void solutionValidity() {

        int size = 4;
        int[] solution = getPuzzle(size).getSoltuion();
        checkValidSolution(size, solution);

        size = 6;
        solution = getPuzzle(size).getSoltuion();
        checkValidSolution(size, solution);

        size = 9;
        solution = getPuzzle(size).getSoltuion();
        checkValidSolution(size, solution);

        size = 12;
        solution = getPuzzle(size).getSoltuion();
        checkValidSolution(size, solution);

        size = 16;
        solution = getPuzzle(size).getSoltuion();
        checkValidSolution(size, solution);

    }

    private void checkValidPuzzle(Puzzle puzzle) {
        int[] initial = puzzle.getCellValues();
        int[] solution = puzzle.getSoltuion();

        assertEquals(initial.length, solution.length);

        boolean isThereEmptyCells = false;
        for (int i = 0; i < initial.length; i++) {

            //Solution does not have blank cells
            assertTrue(solution[i] != 0);

            if ( initial[i] == 0) {

                //At least on empty cell
                isThereEmptyCells = true;
            } else {

                //Locked cells match solution
                assertEquals(initial[i], solution[i]);
            }
        }

        assertTrue(isThereEmptyCells);
    }

    private void checkValidSolution(int size, int[] solution) {

        //Check rows
        for (int i = 0; i < size; i++) {

            //Accounting for empty cell
            int[] occurrences = new int[size + 1];

            for (int j = 0; j < size; j++) {
                int cellNum  = i * size + j;
                int cellValue = solution[cellNum];

                occurrences[cellValue] += 1;
            }

            //Making sure there are no empty cells;
            assertEquals(0, occurrences[0]);

            for (int j = 1; j < size; j++) {
                assertEquals(1, occurrences[j]);
            }
        }

        //Check cols
        for (int i = 0; i < size; i++) {

            //Accounting for empty cell
            int[] occurrences = new int[size + 1];

            for (int j = 0; j < size; j++) {
                int cellNum  = j * size + i;
                int cellValue = solution[cellNum];

                occurrences[cellValue] += 1;
            }

            //Making sure there are no empty cells;
            assertEquals(0, occurrences[0]);

            for (int j = 1; j < size; j++) {
                assertEquals(1, occurrences[j]);
            }
        }
    }

    private Puzzle getPuzzle(int size) {
        return new PuzzleGenerator(size).generatePuzzle(GameDifficulty.EASY);
    }
}
