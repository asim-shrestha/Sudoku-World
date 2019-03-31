package com.sigma.sudokuworld.viewmodels;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import com.sigma.sudokuworld.game.GameDifficulty;
import com.sigma.sudokuworld.game.GameMode;
import com.sigma.sudokuworld.persistence.db.entities.Game;
import com.sigma.sudokuworld.persistence.sharedpreferences.PersistenceService;
import com.sigma.sudokuworld.sudoku.SudokuGridView;

public class MultiplayerViewModel extends GameViewModel {

    private MutableLiveData<Integer> lastCellChanged;

    public MultiplayerViewModel(@NonNull Application application, int[] initialCells, int[] solution) {
        super(
                application,
                new Game(
                        0,
                        PersistenceService.loadSetSetting(application),
                        initialCells.length,
                        GameDifficulty.EASY,
                        GameMode.NUMBERS,
                        initialCells,
                        solution,
                        Game.Companion.getFilledCells(initialCells),
                        0));

        lastCellChanged = new MutableLiveData<>();
        lastCellChanged.setValue(null);
    }

    public LiveData<Integer> getLastCellChanged() {
        return lastCellChanged;
    }

    @Override
    protected void updateCellLabel(int cellNumber, int value) {
        super.updateCellLabel(cellNumber, value);

        lastCellChanged.setValue(cellNumber);
    }

    /**
     * Update the labels to show if the competitor has filled a cell or not
     * @param cellNumber cell number
     * @param filledByCompetitor add or remove fill flag
     */
    public void setCompetitorFilledCell(int cellNumber, boolean filledByCompetitor) {
        if (filledByCompetitor && !isFilledByCompetitor(cellNumber)) {
            //Add the cell fill flag
            String label = labels.get(cellNumber);
            label = SudokuGridView.COMPETITOR_FILLED_FLAG + label;
            labels.set(cellNumber, label);
            updateLiveLabels();
        } else if (!filledByCompetitor && isFilledByCompetitor(cellNumber)) {
            //Remove the cell fill flag
            String label = labels.get(cellNumber);
            label = label.substring(1);
            labels.set(cellNumber, label);
            updateLiveLabels();
        }
    }

    /**
     * Check if the cell is filled by the competitor
     * @param cellNumber cell number
     * @return is the cell filled by the competitor
     */
    private boolean isFilledByCompetitor(int cellNumber) {
        String label = labels.get(cellNumber);

        if (label.isEmpty()) return false;

        return label.charAt(0) == SudokuGridView.COMPETITOR_FILLED_FLAG;
    }
}
