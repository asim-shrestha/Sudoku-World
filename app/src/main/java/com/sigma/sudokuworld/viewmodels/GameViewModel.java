package com.sigma.sudokuworld.viewmodels;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import com.sigma.sudokuworld.game.GameDifficulty;
import com.sigma.sudokuworld.game.GameMode;
import com.sigma.sudokuworld.persistence.GameRepository;
import com.sigma.sudokuworld.persistence.LanguageRepository;
import com.sigma.sudokuworld.persistence.WordPairRepository;
import com.sigma.sudokuworld.persistence.WordRepository;
import com.sigma.sudokuworld.persistence.WordSetRepository;
import com.sigma.sudokuworld.persistence.db.entities.Game;
import com.sigma.sudokuworld.persistence.db.entities.Language;
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
    private List<WordPair> mWordPairs;

    private MutableLiveData<List<String>> cellLabelsLiveData;
    private MutableLiveData<List<String>> buttonLabelsLiveData;
    private MutableLiveData<Boolean> gameWonLiveData;

    List<String> labels;
    private List<String> buttonLabels;

    private SparseArray<String> nativeWordsMap;
    private SparseArray<String> foreignWordsMap;

    private Language nativeLanguage;
    private Language foreignLanguage;

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

    public Language getNativeLanguage() {
        return nativeLanguage;
    }

    public Language getForeignLanguage() {
        return foreignLanguage;
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


        if (!isCorrectValue(cellNumber, value)) {
            if (mWordPairs !=  null && value > 0 && mWordPairs.size() >= value && (getGameMode() != GameMode.NUMBERS)) {
                new WordPairRepository(mApplication).incrementMisuseCount(mWordPairs.get(value - 1).getPairID());
            }
        }

        mGame.setCellValue(cellNumber, value);
        updateCellLabel(cellNumber, value);
        checkForGameWin();
    }

    public long getElapsedTime() {
        return mGame.getTimeInterval();
    }

    public void setElapsedTime(long elapsedTime) {
        mGame.setTimeInterval(elapsedTime);
    }

    public GameDifficulty getGameDifficulty(){
        return mGame.getDifficulty();
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

        labels.set(cellNumber, flags + valueToMappedLabel(value, GameMode.opposite(gameMode)));
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
        LanguageRepository languageRepository = new LanguageRepository(getApplication());

        mWordPairs = wordSetRepository.getAllWordPairsInSet(mGame.getSetID());

        if (nativeLanguage == null && !mWordPairs.isEmpty()) {
            nativeLanguage = languageRepository.getLanguageByName(mWordPairs.get(0).getNativeLanguageName());
        }

        if (foreignLanguage == null && !mWordPairs.isEmpty()) {
            foreignLanguage = languageRepository.getLanguageByName(mWordPairs.get(0).getForeignLanguageName());
        }

        nativeWordsMap = new SparseArray<>();
        nativeWordsMap.append(0, "");

        foreignWordsMap = new SparseArray<>();
        foreignWordsMap.append(0, "");

        for(int i = 0; i < mWordPairs.size(); i++) {
            nativeWordsMap.append(i + 1, mWordPairs.get(i).getNativeWord().getWord());
            foreignWordsMap.append(i + 1, mWordPairs.get(i).getForeignWord().getWord());
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
                label += valueToMappedLabel(mGame.getCellValue(i), gameMode);
            } else {
                label += valueToMappedLabel(mGame.getCellValue(i), GameMode.opposite(gameMode));
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
            label += valueToMappedLabel(i + 1, gameMode);

            buttonLabels.add(i, label);
        }

        buttonLabelsLiveData.setValue(buttonLabels); //TODO: Don't run on main thread
    }

    private String valueToMappedLabel(int value, GameMode gameMode) {
        String label = "";

        if (value != 0) {
            if (gameMode == GameMode.NUMBERS || (nativeWordsMap.size() <= value)) label = Integer.toString(value);
            else if (gameMode == GameMode.NATIVE) label = nativeWordsMap.valueAt(value);
            else if (gameMode == GameMode.FOREIGN) label = foreignWordsMap.valueAt(value);
        }

        return label;
    }

    public void deleteGame(){
        new GameRepository(getApplication()).deleteGame(mGame);
    }
}
