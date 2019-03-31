package com.sigma.sudokuworld.viewmodels;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.sigma.sudokuworld.game.GameDifficulty;
import com.sigma.sudokuworld.game.GameMode;
import com.sigma.sudokuworld.game.gen.PuzzleGenerator;
import com.sigma.sudokuworld.persistence.GameRepository;
import com.sigma.sudokuworld.persistence.WordSetRepository;
import com.sigma.sudokuworld.persistence.db.entities.Game;
import com.sigma.sudokuworld.persistence.db.entities.Set;
import com.sigma.sudokuworld.persistence.sharedpreferences.KeyConstants;
import com.sigma.sudokuworld.persistence.sharedpreferences.PersistenceService;

import java.util.List;

public class MenuViewModel extends BaseSettingsViewModel {
    private GameRepository mGameRepository;
    private WordSetRepository mWordSetRepository;
    private LiveData<List<Game>> mGames;

    public MenuViewModel(@NonNull Application application) {
        super(application);

        mGameRepository = new GameRepository(mApplication);
        mWordSetRepository = new WordSetRepository(mApplication);

        mGames = mGameRepository.getAllGames();
    }

    //Generates a new game based on stored settings
    public long generateNewGameWithStoredSettings() {
        PuzzleGenerator generator = new PuzzleGenerator(getSelectedBoardLength());
        PuzzleGenerator.Puzzle puzzle = generator.generatePuzzle(getSelectedGameDifficulty());

        Game game = new Game(
                0,
                getSelectedSetID(),
                getSelectedBoardLength(),
                getSelectedGameDifficulty(),
                getSelectedGameMode(),
                puzzle.getCellValues(),
                puzzle.getSoltuion(),
                Game.Companion.getFilledCells(puzzle.getCellValues()),
                0
        );

        //Returns the saveID
        return mGameRepository.newGame(game);
    }

    public LiveData<List<Game>> getAllGameSaves() {
        return mGames;
    }

    public void deleteGame(Game game) {
        mGameRepository.deleteGame(game);
    }

    /*
        Game Settings
     */
    public void setSelectedBoardLength(int boardLength) {
        PersistenceService.saveBoardLengthSetting(mApplication, boardLength);
    }
    public void setSelectedGameMode(GameMode gameMode) {
        PersistenceService.saveGameModeSetting(mApplication, gameMode);
    }

    public void setSelectedGameDifficulty(GameDifficulty difficulty) {
        PersistenceService.saveDifficultySetting(mApplication, difficulty);
    }

    public GameMode getSelectedGameMode() {
        return PersistenceService.loadGameModeSetting(mApplication);
    }

    public GameDifficulty getSelectedGameDifficulty() {
        return PersistenceService.loadDifficultySetting(mApplication);
    }

    public void setSelectedSet(Set set) {
        PersistenceService.saveSetSetting(mApplication, set.getSetID());
    }

    /**
     * Get the user selected set.
     * If the set doesn't exist it choose the first set
     * @return set
     */
    public Set getSelectedSet() {
        Set set = mWordSetRepository.getSet(getSelectedSetID());

        if (set == null) {
            set = mWordSetRepository.getFirstSet();
            setSelectedSet(set);
        }

        return set;
    }

    public long getSelectedSetSize() {
        return mWordSetRepository.getSetSize(PersistenceService.loadSetSetting(mApplication));
    }

    private long getSelectedSetID() {
        return PersistenceService.loadSetSetting(mApplication);
    }

    public int getSelectedBoardLength(){
        return PersistenceService.loadBoardLengthSetting(mApplication);
    }
}
