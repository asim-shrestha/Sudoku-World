package com.sigma.sudokuworld.viewmodels;

import android.app.Application;
import android.support.annotation.NonNull;

import com.sigma.sudokuworld.persistence.GameRepository;


public class SinglePlayerViewModel extends GameViewModel {

    //Constructor loads a saved game
    public SinglePlayerViewModel(@NonNull Application application, long saveID) {
        super(application, new GameRepository(application).getGameSaveByID(saveID));
    }

    @Override
    protected void onCleared() {
        new GameRepository(getApplication()).saveGame(super.getGame());

        super.onCleared();
    }
}

