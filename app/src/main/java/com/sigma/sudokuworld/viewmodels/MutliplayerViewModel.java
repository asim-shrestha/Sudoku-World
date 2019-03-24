package com.sigma.sudokuworld.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import com.sigma.sudokuworld.persistence.GameRepository;

public class MutliplayerViewModel extends AndroidViewModel {
    private GameRepository mGameRepository;

    public MutliplayerViewModel(@NonNull Application application) {
        super(application);

        mGameRepository = new GameRepository(application);
    }


}
