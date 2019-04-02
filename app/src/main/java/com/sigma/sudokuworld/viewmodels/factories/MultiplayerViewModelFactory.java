package com.sigma.sudokuworld.viewmodels.factories;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import com.sigma.sudokuworld.viewmodels.MultiplayerViewModel;

public class MultiplayerViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final int[] initialCells;
    private final int[] solution;


    public MultiplayerViewModelFactory(Application application, int[] initialCells, int[] solution) {
        this.application = application;
        this.initialCells = initialCells;
        this.solution = solution;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MultiplayerViewModel(application, initialCells, solution);
    }
}