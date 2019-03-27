package com.sigma.sudokuworld.viewmodels;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import com.sigma.sudokuworld.game.GameMode;
import com.sigma.sudokuworld.persistence.WordSetRepository;
import com.sigma.sudokuworld.persistence.db.entities.Game;
import com.sigma.sudokuworld.persistence.db.views.WordPair;
import com.sigma.sudokuworld.persistence.sharedpreferences.KeyConstants;
import com.sigma.sudokuworld.sudoku.SudokuGridView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class GameViewModel extends BaseSettingsViewModel {
    private int mBoardSize;
    private int mBoardLength;
    private Game mGame;

    private MutableLiveData<List<String>> cellLabelsLiveData;
    private MutableLiveData<List<String>> buttonLabelsLiveData;
    private MutableLiveData<Boolean> gameWonLiveData;

    List<String> labels;
    private List<String> buttonLabels;

    private SparseArray<String> nativeWordsMap;
    private SparseArray<String> foreignWordsMap;

    private String TAG = "SinglePlayerViewModel";

    //Constructor loads a saved game
    GameViewModel(@NonNull Application application, Game game) {
        super(application);

        mGame = game;
        mBoardSize = mGame.getCellValues().length;
        mBoardLength = (int) Math.sqrt(mBoardSize);
        init();
    }

    Game getGame() {
        return mGame;
    }

    public LiveData<List<String>> getCellLabels() {
        return cellLabelsLiveData;
    }

    public LiveData<List<String>> getButtonLabels() {
        return buttonLabelsLiveData;
    }

    public LiveData<Boolean> isGameWon() {
        return gameWonLiveData;
    }

    public String getMappedString(int value, GameMode mode) {
        return valueToMappedLabel(value, mode);
    }

    public int getBoardLength(){
        return mBoardLength;
    }

    public int getCellValue(int cellNumber) {
        return mGame.getCellValue(cellNumber);
    }

    public void setCellValue(int cellNumber, int value) {
        if (cellNumber > mGame.getCellValues().length || cellNumber < 0) {
            Log.wtf(TAG, "Invalid Cell number");
            return;
        }

        // Locked cell
        if (mGame.getLockedCells()[cellNumber]) return;

        mGame.setCellValue(cellNumber, value);
        updateCellLabel(cellNumber, value);
        checkForGameWin();
    }

    public GameMode getGameMode() {
        return mGame.getGameMode();
    }

    public boolean isLockedCell(int cellNumber) {
        return mGame.isLocked(cellNumber);
    }

    public boolean isCorrectValue(int cellNumber, int value) {
        return mGame.getSolutionValue(cellNumber) ==  value;
    }

    public boolean isCellCorrect(int cellNumber) {
        return mGame.getSolutionValue(cellNumber) ==  mGame.getCellValue(cellNumber);
    }

    public ArrayList<Integer> getIncorrectCells() {
        ArrayList<Integer> incorrectCells = new ArrayList<>();
        for (int i = 0; i < mBoardSize; i++) {
            if (mGame.getCellValue(i) != mGame.getSolutionValue(i)) incorrectCells.add(i);
        }

        return incorrectCells;
    }

    protected void updateCellLabel(int cellNumber, int value) {
        GameMode gameMode = mGame.getGameMode();

        String flags = "";
        String label = labels.get(cellNumber);

        if (!label.isEmpty() && label.charAt(0) == SudokuGridView.COMPETITOR_FILLED_FLAG) {
            flags += SudokuGridView.COMPETITOR_FILLED_FLAG;
        }

        labels.set(cellNumber, flags + valueToMappedLabel(value, gameMode));
        updateLiveLabels();
    }

    void updateLiveLabels() {
        cellLabelsLiveData.setValue(labels); //TODO: Don't run on main thread
    }

    /**
     * If the game is won updates the live data
     */
    private void checkForGameWin() {
        if (Arrays.equals(mGame.getCellValues(), mGame.getSolutionValues())) {
            gameWonLiveData.setValue(true);
        }
    }

    private void init() {
        initializeWordMaps();
        initCellLabelsLiveData();
        initButtonLabelsLiveData();

        gameWonLiveData = new MutableLiveData<>();
        gameWonLiveData.setValue(false);
    }

    private void initializeWordMaps() {
        WordSetRepository wordSetRepository = new WordSetRepository(getApplication());
        List<WordPair> wordPairs = wordSetRepository.getAllWordPairsInSet(mGame.getSetID());

        nativeWordsMap = new SparseArray<>();
        nativeWordsMap.append(0, "");

        foreignWordsMap = new SparseArray<>();
        foreignWordsMap.append(0, "");

        for(int i = 0; i < wordPairs.size(); i++) {
            nativeWordsMap.append(i + 1, wordPairs.get(i).getNativeWord().getWord());
            foreignWordsMap.append(i + 1, wordPairs.get(i).getForeignWord().getWord());
        }
    }

    private void initCellLabelsLiveData() {
        cellLabelsLiveData = new MutableLiveData<>();
        labels = new ArrayList<>();

        GameMode gameMode = mGame.getGameMode();
        for (int i = 0; i < mBoardSize; i++) {
            String label = "";
            if (mGame.isLocked(i)) {
                label += KeyConstants.CELL_LOCKED_FLAG;
                label += valueToMappedLabel(mGame.getCellValue(i), GameMode.opposite(gameMode));
            } else {
                label += valueToMappedLabel(mGame.getCellValue(i), gameMode);
            }

            labels.add(i, label);
        }

        cellLabelsLiveData.setValue(labels); //TODO: Don't run on main thread
    }

    private void initButtonLabelsLiveData() {
        buttonLabelsLiveData = new MutableLiveData<>();
        buttonLabels = new ArrayList<>();

        GameMode gameMode = mGame.getGameMode();
        gameMode = GameMode.opposite(gameMode);

        for (int i = 0; i < mBoardLength; i++) {
            String label = "";
            label += valueToMappedLabel(i + 1, GameMode.opposite(gameMode));

            buttonLabels.add(i, label);
        }

        buttonLabelsLiveData.setValue(buttonLabels); //TODO: Don't run on main thread
    }

    private String valueToMappedLabel(int value, GameMode gameMode) {
        String label = "";

        if (value != 0) {
            if (gameMode == GameMode.NUMBERS || (nativeWordsMap.size() <= value)) //TODO: Remove OR CASE once set sizes >= 12
                label = Integer.toString(value);
            else if (gameMode == GameMode.NATIVE) label = nativeWordsMap.valueAt(value);
            else if (gameMode == GameMode.FOREIGN) label = foreignWordsMap.valueAt(value);
        }

        return label;
    }
}
