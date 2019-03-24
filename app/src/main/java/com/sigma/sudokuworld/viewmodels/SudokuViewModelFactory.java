package com.sigma.sudokuworld.viewmodels;

        import android.app.Application;
        import android.arch.lifecycle.ViewModel;
        import android.arch.lifecycle.ViewModelProvider;
        import android.support.annotation.NonNull;

public class SudokuViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final long saveId;

    public SudokuViewModelFactory(Application application, long saveId) {
        this.saveId = saveId;
        this.application = application;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new SudokuViewModel(application, saveId);
    }
}
