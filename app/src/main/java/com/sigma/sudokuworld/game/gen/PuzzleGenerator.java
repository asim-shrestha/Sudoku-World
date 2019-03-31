package com.sigma.sudokuworld.game.gen;
import android.os.Bundle;

import com.sigma.sudokuworld.game.GameDifficulty;
import com.sigma.sudokuworld.persistence.db.entities.Game;
import com.sigma.sudokuworld.persistence.sharedpreferences.KeyConstants;

import java.util.Arrays;
import java.util.Random;
import java.lang.Math;


//This object takes in the subsection size of a Sudoku and can solve boards, generate valid sudoku boards
//and check whether or not a given solution is the correct solution
public class PuzzleGenerator {
    private SudokuCell[][] mSudokuCells;
    private int mSubsectionHeight;
    private int mSubsectionWidth;
    private int mBoardLength;
    private int mBoardSize;
    private int[] mSolutionValues;

    //Constructor
    public PuzzleGenerator(int boardLength) {
        mBoardLength = boardLength;
        mBoardSize = (mBoardLength) * (mBoardLength);
        //Figure out what two numbers make up each subsection
        mSubsectionHeight = (int) Math.floor(Math.sqrt(boardLength));
        mSubsectionWidth = (int) Math.ceil(Math.sqrt(boardLength));
        mSolutionValues = new int[mBoardSize];
    }

    public Puzzle generatePuzzle(GameDifficulty difficulty) {
        generateSudokuCells();
        generateBoard(difficulty);

        return new Puzzle(getCellValues(), mSolutionValues);
    }


    private void generateSudokuCells() {
        //Creates space for the two dimensional array of Sudoku cells
        mSudokuCells = new SudokuCell[mBoardLength][mBoardLength];

        //Loops through and puts a cell in each index of the 2D Sudoku cell array
        for (int row = 0; row < mBoardLength; row++) {
            for (int column = 0; column < mBoardLength; column++) {
                mSudokuCells[row][column] = new SudokuCell(mBoardLength);
            }
        }
    }

    
    private void generateBoard(GameDifficulty difficulty) {
        clearBoard();
        solveBoard();
        mSolutionValues = getCellValues();

        //Check a different number of nodes in generatePlayableBoard
        //based on the selected difficulty setting
        //We use a propriotary formula to generate how many nodes to potentially delete
        int nodesToCheck = 0;
        switch (difficulty){
            case EASY:
                nodesToCheck = (mBoardSize + mBoardLength)/6;
                break;
            case MEDIUM:
                nodesToCheck = (mBoardSize + mBoardLength)*2 /6;
                break;
            case HARD:
                nodesToCheck = (mBoardSize + mBoardLength)*4 / 5;
        }

        //Cap nodesToCheck at 100 so that the computations don't get too big
        nodesToCheck = Math.min(nodesToCheck, 65);
        generatePlayableBoard(nodesToCheck);
    }


    private void clearBoard() {
        //Reset solution array
        for (int i = 0; i < mBoardSize; i++){
            mSolutionValues[i] = 0;
        }

        //Reset every cell in cell list
        for (int row = 0; row < mBoardLength; row++) {
            for (int column = 0; column < mBoardLength; column++) {
                mSudokuCells[row][column].clearCurrValue();
            }
        }
    }

    //Takes the row and column of a cell and removes all values from its candidate list that it
    //cannot take
    private void scanCandidates(int row, int column) {
        SudokuCell cell = mSudokuCells[row][column];

        //Remove pre-existing row and column
        for ( int i = 0; i < mBoardLength; i++) {
            cell.removeCandidate(mSudokuCells[row][i].getCurrValue());
            cell.removeCandidate(mSudokuCells[i][column].getCurrValue());
        }


        //Remove pre-existing sub-section values
        //The below calculations will be coordinates for the top left index of each sub-section
        int subSectionRow = mSubsectionHeight*(row / mSubsectionHeight);
        int subSectionColumn = mSubsectionWidth*(column / mSubsectionWidth);
        for ( int i = 0; i < mSubsectionHeight; i++) {
            for (int j =0; j < mSubsectionWidth; j++) {
                cell.removeCandidate(mSudokuCells[subSectionRow + i][subSectionColumn + j].getCurrValue());
            }
        }
    }


    private boolean solveBoard() {
        //For each cell, check what values it can take and make it take a random value
        //If it has no possible values, go to the previous cell and change its value and move on
        //Keep backtracking if all cells do not have any possible values
        //Algorithm ends when we go past the last cell
        boolean isBacktracking = false;
        int index = 0;
        while (index < mBoardSize) {
            int row = index / mBoardLength;
            int column = index % mBoardLength;

            //If we've backtracked to before the first node, return false
            if (index < 0) {
                return false;}

            //Cell must NOT be locked to change its value
            //If this cell has not had a value placed yet, reset its candidate list
            //This is for the situation where a node is revisited after being backtracked to
            //And therefore needs a fresh candidate list to find potential values on
            if (mSudokuCells[row][column].getCurrValue() == 0) {
                mSudokuCells[row][column].resetCandidateList();
            }

            scanCandidates(row, column);

            //Check if the cell is locked
            //If it is move to the next cell which depends on if we're backtracking or not
            if (mSudokuCells[row][column].isLocked()){
                //I had to put a statement in here in order for this to be checked
                //Currently doesn't do anything and skips the next two checks if the cell is locked
                int j =0;
                }

            //If candidate is picked, we're no longer backtracking and we move to next cell
            else if (mSudokuCells[row][column].pickCandidate()) {
                isBacktracking = false;
            }

            else {
                //Candidate is not picked, set cell value to zero and start backtracking
                mSudokuCells[row][column].clearCurrValue();
                isBacktracking = true;
            }


            //Change index based on whether or not we are backtracking
            if (isBacktracking) { index--; }
            else { index ++; }
        }

        //After we have found a solution, we lock every cell in place
        for (int row = 0; row < mBoardLength; row++) {
            for (int column = 0; column < mBoardLength; column++) {
                mSudokuCells[row][column].changeLockValue(true);
            }
        }

        return true;
    }


    private void generatePlayableBoard(int nodesToCheck) {
        //Keeps track of how loops have been iterated and what cells are emptied.
        //Works by emptying a node and then checking if the solution is unique
        //If a solution can still be generated with that node not being able to take its previous
        //value then we cannot delete that node
        int cycleLength = Math.min(mBoardSize, nodesToCheck);
        SudokuCell[] deletedNodeList = new SudokuCell[cycleLength];
        SudokuCell[] cellList = new SudokuCell[mBoardSize];
        int cellIndex = 0;
        int deletedNodeIndex = 0;

        //Copy cell list
        for (int row = 0; row < mBoardLength; row++) {
            for(int column = 0; column < mBoardLength; column++) {
                cellList[cellIndex] = mSudokuCells[row][column];
                cellIndex++;
            }
        }


        //Randomly shuffle cell list
        for (int i = 0; i < mBoardSize; i++) {
            int randomIndex = randomInt(mBoardSize);
            SudokuCell tempCell = cellList[randomIndex];
            cellList[randomIndex] = cellList[i];
            cellList[i] = tempCell;
        }


        //Loop through and check each cell in cell list
        for (cellIndex = 0; cellIndex < cycleLength; cellIndex++) {
            //Getting the cell to check
            SudokuCell cell = cellList[cellIndex];

            //Make sure the cell cannot re-get its current value
            int restrictedValue = cell.getCurrValue();
            cell.setRestrictedValue(restrictedValue);

            deletedNodeList[deletedNodeIndex] = cell;
            //Resetting all cells that have been emptied
            for (int i = 0; i <= deletedNodeIndex; i++) {
                deletedNodeList[i].clearCurrValue();
            }

            //If there are now no solutions possible we may delete the cell
            if(!solveBoard()) {
                deletedNodeIndex++;
            }
            //We cannot delete the cell so we put its value back
            else {
                cell.changeCurrValue(restrictedValue);
            }

            //Reset the cell's restricted value
            cell.setRestrictedValue(-1);
        }


        //Solving the board places values in our deleted nodes
        //This is so that the value of the nodes will get reset
        for (int i = 0; i < deletedNodeIndex; i++) {
            deletedNodeList[i].clearCurrValue();
            deletedNodeList[i].resetCandidateList();
        }
    }

    boolean isCellVisited(int size, SudokuCell[] visitedCellList, SudokuCell cell) {
        for (int i = 0; i < size; i++) {
            if (visitedCellList[i] == cell) {return true;}
        }
        return false;
    }


    //Returns a one dimensional array containing cell values at their respective index
    private int[] getCellValues() {
            int[] cellValues = new int[mBoardSize];
            int cellValueIndex = 0;
            //Loops through and grabs the value of each element in the 2D Sudoku cell array
            for (int row = 0; row < mBoardLength; row++) {
                for(int column = 0; column < mBoardLength; column++) {
                    //mSudokuCells[row][column].pickCandidate();
                    cellValues[cellValueIndex] = mSudokuCells[row][column].getCurrValue();
                    cellValueIndex++;
                }
            }
            return cellValues;
    }

    private static int randomInt(int max){
        Random random = new Random();
        return random.nextInt(max);
    }

    public class Puzzle {
        private int[] cellValues;
        private int[] soltuion;

        Puzzle(int[] cellValues, int[] solution) {
            this.cellValues = cellValues;
            this.soltuion = solution;
        }

        public int[] getCellValues() {
            return cellValues;
        }

        public int[] getSoltuion() {
            return soltuion;
        }
    }
}
