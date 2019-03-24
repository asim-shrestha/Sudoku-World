package com.sigma.sudokuworld;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import android.widget.TextView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.sigma.sudokuworld.audio.SoundPlayer;
import com.sigma.sudokuworld.persistence.db.entities.Game;
import com.sigma.sudokuworld.persistence.sharedpreferences.KeyConstants;
import com.sigma.sudokuworld.sudoku.AudioSudokuActivity;
import com.sigma.sudokuworld.sudoku.MultiplayerActivity;
import com.sigma.sudokuworld.sudoku.VocabSudokuActivity;
import com.sigma.sudokuworld.viewmodels.MenuViewModel;

import java.util.List;

public class MenuActivity extends AppCompatActivity {
    private MenuViewModel mMenuViewModel;
    private SoundPlayer mSoundPlayer;
    private FragmentManager mFragmentManager;

    private GoogleSignInClient mSignInClient;
    private PlayersClient mPlayersClient;

    private TextView mPlayerLabel;

    private int RC_SIGN_IN = 1337;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mMenuViewModel = ViewModelProviders.of(this).get(MenuViewModel.class);
        mFragmentManager = getSupportFragmentManager();

        //Play a sound every time the fragment is opened
        mFragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                mSoundPlayer.playPlaceCellSound();
            }
        });

        mMenuViewModel.getAllGameSaves().observe(this, new Observer<List<Game>>() {
            @Override
            public void onChanged(@Nullable List<Game> games) {
                if (games.isEmpty()) findViewById(R.id.continueButton).setVisibility(View.GONE);
                else findViewById(R.id.continueButton).setVisibility(View.VISIBLE);
            }
        });


        mSoundPlayer = new SoundPlayer(this);
        mPlayerLabel = findViewById(R.id.userLabel);
        ImageView imageView = findViewById(R.id.menuAVD);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) imageView.getDrawable();
            animatedVectorDrawable.start();
        }

        mSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            onConnected(account);
        }
    }

    /**
     * Called when play button is pressed. (Action defined in xml onClick)
     */
    public void onPlayPressed(View v) {
        showFragment(new NewGameFragment());
    }

    /**
     * Called when Continue button is pressed. (Action defined in xml onClick)
     */
    public void onContinuePressed(View v) {
        showFragment(new SelectGameFragment());
    }

    /**
     * Called when Settings button is pressed. (Action defined in xml onClick)
     */
    public void onSettingsPressed(View v) {
        showFragment(new SettingsFragment());
    }

    public void onMultiPlayerPressed(View v) {
        if (!isSignedIn()) {
            //Not signed in
            startActivityForResult(mSignInClient.getSignInIntent(), RC_SIGN_IN);
        } else {
            showFragment(new MultiplayerFragment());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Returning from the sign in intent
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {

                //Logged in. Setting up player data
                onConnected(result.getSignInAccount());

            } else {
                String message = "An error has occurred";
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton("ok", null).show();
            }
        }
    }

    public void startGame(long saveID) {
        Intent intent;
        if (mMenuViewModel.isAudioModeEnabled()) {
            intent = new Intent(getBaseContext(), AudioSudokuActivity.class);
        } else {
            intent = new Intent(getBaseContext(), VocabSudokuActivity.class);
        }

        intent.putExtra(KeyConstants.SAVE_ID_KEY, saveID);

        startActivity(intent);
        closeFragment();
    }

    public void startMultiplayerGame() {

        Intent intent = new Intent(getBaseContext(), MultiplayerActivity.class);
        intent.putExtra(KeyConstants.SAVE_ID_KEY, mMenuViewModel.generateNewGameWithStoredSettings());
        startActivity(intent);
    }

    /* Show and close fragments */

    public void closeFragment(){
        mFragmentManager.popBackStack();
    }

    public void showFragment(Fragment fragment) {
        mFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    /* Google auth */

    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    /**
     * Sets up the players data
     * @param googleSignInAccount player
     */
    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        mPlayersClient = Games.getPlayersClient(this, googleSignInAccount);

        mPlayersClient.getCurrentPlayer().addOnCompleteListener(new OnCompleteListener<Player>() {
            @Override
            public void onComplete(@NonNull Task<Player> task) {
                if (task.isSuccessful()) {
                    mPlayerLabel.setText(task.getResult().getDisplayName());
                }
            }
        });
    }
}

