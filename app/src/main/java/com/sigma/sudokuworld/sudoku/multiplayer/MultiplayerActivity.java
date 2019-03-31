package com.sigma.sudokuworld.sudoku.multiplayer;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.sudoku.SudokuActivity;
import com.sigma.sudokuworld.viewmodels.ConnectionViewModel;
import com.sigma.sudokuworld.viewmodels.MultiplayerViewModel;
import com.sigma.sudokuworld.viewmodels.MultiplayerViewModelFactory;

public class MultiplayerActivity extends SudokuActivity {
    private static final String TAG = "Multiplayer";

    private static final int RC_WAITING_ROOM = 276;

    private ConnectionViewModel mConnectionViewModel;

    private MultiplayerViewModel mMultiplayerViewModel;
    private LoadingScreenFragment mLoadingScreenFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO make rotation compatible, Currently boots you out of game on rotate. Put in view model?
        mConnectionViewModel = ViewModelProviders.of(this).get(ConnectionViewModel.class);
        mConnectionViewModel.getGameStateLiveData().observe(this, mGameStateObserver);

        mLoadingScreenFragment = new LoadingScreenFragment();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_WAITING_ROOM) {
            mConnectionViewModel.setWaitingRoomResult(resultCode);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mConnectionViewModel.leaveGameRoom();
    }

    Observer<ConnectionViewModel.GameState> mGameStateObserver = new Observer<ConnectionViewModel.GameState>() {
        @Override
        public void onChanged(@Nullable ConnectionViewModel.GameState state) {
            if (state == null) {
                Log.d(TAG, "onChanged: GAME STATE = NULL");
                return;
            }

            switch(state) {
                case NEW:
                    displayLoadingScreen();
                    break;
                case LOBBY:
                    displayWaitingRoom();
                    break;
                case PLAYING:
                    displayGame();
                    break;
                case OVER:
                    displayGameOverScreen();
                    break;

                //Disconnect cases
                case LEAVE:
                    finish();
                    break;
                case PEER_LEFT:
                    displayDisconnectDialog("Opponent Left");
                    break;
                case ERROR:
                    displayErrorDialog("An Error has occurred");
                    break;
                case SINGED_OUT:
                    displayErrorDialog("You were signed out");
                    break;
            }
        }
    };

    void displayErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setNeutralButton("Ok", null)
                .show();
    }

    void displayDisconnectDialog(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    void displayWaitingRoom() {
        Intent intent = mConnectionViewModel.getWaitingRoomIntent();
        startActivityForResult(intent, RC_WAITING_ROOM);
    }

    void displayLoadingScreen() {
        mFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, mLoadingScreenFragment)
                .addToBackStack(null)
                .commit();
    }

    void displayGame() {
        int[] initialCells = mConnectionViewModel.getInitialCells();
        int[] solutionCells = mConnectionViewModel.getSolutionCells();

        MultiplayerViewModelFactory factory =
                new MultiplayerViewModelFactory(getApplication(), initialCells, solutionCells);

        mMultiplayerViewModel = ViewModelProviders.of(this, factory).get(MultiplayerViewModel.class);
        super.setGameViewModel(mMultiplayerViewModel);
        super.mGameTimer.start();

        // Watch for my most recent move and send to others
        mMultiplayerViewModel.getLastCellChanged().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer != null) {
                    if (mMultiplayerViewModel.getCellValue(integer) != 0) {
                        mConnectionViewModel.broadcastMove(integer, true);
                    } else {
                        mConnectionViewModel.broadcastMove(integer, false);
                    }
                }
            }
        });

        // Watch to see if i've won the game
        mMultiplayerViewModel.isGameWon().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isGameWon) {
                if (isGameWon != null && isGameWon) {
                    mConnectionViewModel.claimWin();
                }
            }
        });

        mConnectionViewModel.getCompetitorFilledCell().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer == null) return;

                mMultiplayerViewModel.setCompetitorFilledCell(integer,  true);
            }
        });

        mConnectionViewModel.getCompetitorEmptiedCell().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer == null) return;

                mMultiplayerViewModel.setCompetitorFilledCell(integer, false);
            }
        });

        mFragmentManager.popBackStack();
    }

    private void displayGameOverScreen() {
        GameOverFragment gameOverFragment = GameOverFragment.newInstance("FIX ME", true);

        mFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, gameOverFragment)
                .addToBackStack(null)
                .commit();
    }
}
