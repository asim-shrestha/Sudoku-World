package com.sigma.sudokuworld;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import android.widget.TextView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.games.*;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.sigma.sudokuworld.audio.SoundPlayer;
import com.sigma.sudokuworld.persistence.db.entities.Game;
import com.sigma.sudokuworld.persistence.sharedpreferences.KeyConstants;
import com.sigma.sudokuworld.sudoku.multiplayer.MultiplayerActivity;
import com.sigma.sudokuworld.sudoku.singleplayer.SinglePlayerActivity;
import com.sigma.sudokuworld.viewmodels.MenuViewModel;

import java.util.List;

public class MenuActivity extends BaseActivity {
    private static final String TAG = "MENU";
    private static final int RC_SIGN_IN = 1337;
    private static final int RC_INBOX = 1338;
    private static final int RC_LEADERBOARD = 1339;

    private MenuViewModel mMenuViewModel;
    private SoundPlayer mSoundPlayer;
    private FragmentManager mFragmentManager;

    private PlayersClient mPlayersClient;
    private GamesClient mGamesClient;
    private InvitationsClient mInvitationsClient;
    private LeaderboardsClient mLeaderboardsClient;

    private TextView mPlayerLabel;

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
        mPlayerLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        ImageView imageView = findViewById(R.id.menuAVD);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) imageView.getDrawable();
            animatedVectorDrawable.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

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
        //restart();
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
            signIn();
        } else {
            showFragment(new MultiplayerFragment());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: REQUEST CODE " + requestCode);
        Log.d(TAG, "onActivityResult: RESULT CODE " + resultCode);

        if (requestCode == RC_SIGN_IN) {
            interactiveSignInResult(data);
        }

        else if (requestCode == RC_INBOX && resultCode == Activity.RESULT_OK) {
            Invitation invitation = data.getParcelableExtra(Multiplayer.EXTRA_INVITATION);
            inboxResult(invitation);
        }
    }

    public void startGame(long saveID) {
        Intent intent = new Intent(getBaseContext(), SinglePlayerActivity.class);
        intent.putExtra(KeyConstants.SAVE_ID_KEY, saveID);

        startActivity(intent);
        closeFragment();
    }

    public void startMultiplayerGame(boolean isHost) {
        Intent intent = new Intent(getBaseContext(), MultiplayerActivity.class);
        intent.putExtra(MultiplayerActivity.IS_HOST_KEY, isHost);
        startActivity(intent);
    }

    public void showInviteInbox() {
        mInvitationsClient.getInvitationInboxIntent().addOnSuccessListener(new OnSuccessListener<Intent>() {
            @Override
            public void onSuccess(Intent intent) {
                startActivityForResult(intent, RC_INBOX);
            }
        });
    }

    public void showInviteDialog(final Invitation invitation) {
        new AlertDialog.Builder(this)
                .setTitle("Game Invite")
                .setMessage(invitation.getInviter().getDisplayName() + " has invited you to a game!")
                .setNeutralButton("Cancel", null)  //TODO decline invite
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inboxResult(invitation);
                    }
                })
                .show();
    }

    public void showLeaderboard() {
        mLeaderboardsClient.getAllLeaderboardsIntent().addOnSuccessListener(new OnSuccessListener<Intent>() {
            @Override
            public void onSuccess(Intent intent) {
                startActivityForResult(intent, RC_LEADERBOARD);
            }
        });
    }

    private void inboxResult(Invitation invitation) {

        if (invitation != null) {
            Intent intent = new Intent(getBaseContext(), MultiplayerActivity.class);
            intent.putExtra(MultiplayerActivity.INVITATION_KEY, invitation.getInvitationId());
            startActivity(intent);
        }
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

    private void signIn() {
        GoogleSignInOptions signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN;
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {
            //Already signed in
        } else {
            final GoogleSignInClient signInClient = GoogleSignIn.getClient(this, signInOptions);
            signInClient.silentSignIn().addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>() {
                @Override
                public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                    if (task.isSuccessful()) {
                        onConnected(task.getResult());
                    } else {
                        startActivityForResult(signInClient.getSignInIntent(), RC_SIGN_IN);
                    }
                }
            });
        }
    }

    private void interactiveSignInResult(Intent data) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        Log.d(TAG, "interactiveSignInResult: SIGN IN RESULT CODE" + GoogleSignInStatusCodes.getStatusCodeString(result.getStatus().getStatusCode()));


        if (result.isSuccess()) {

            //Logged in. Setting up player data
            onConnected(result.getSignInAccount());

        } else {
            String message = "An error has occurred";
            new AlertDialog.Builder(this).setMessage(message)
                    .setNeutralButton("ok", null).show();
        }
    }

    private void signOut() {
        if (isSignedIn()) {
            GoogleSignInOptions signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN;
            GoogleSignInClient signInClient = GoogleSignIn.getClient(this, signInOptions);

            signInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    onDisconnected();
                }
            });
        }
    }

    /**
     * Sets up the players data
     * @param googleSignInAccount player
     */
    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        mPlayersClient = Games.getPlayersClient(this, googleSignInAccount);
        mGamesClient = Games.getGamesClient(this, googleSignInAccount);
        mInvitationsClient = Games.getInvitationsClient(this, googleSignInAccount);
        mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);

        mGamesClient.setViewForPopups(findViewById(android.R.id.content));

        mPlayersClient.getCurrentPlayer().addOnCompleteListener(new OnCompleteListener<Player>() {
            @Override
            public void onComplete(@NonNull Task<Player> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        mPlayerLabel.setText(task.getResult().getDisplayName());
                    }
                }
            }
        });

        //Checks if the user has accepted an invitation from the notification bar
        mGamesClient.getActivationHint().addOnSuccessListener(new OnSuccessListener<Bundle>() {
            @Override
            public void onSuccess(Bundle bundle) {
                if (bundle != null) {
                    Invitation invitation = bundle.getParcelable(Multiplayer.EXTRA_INVITATION);
                    inboxResult(invitation);
                }
            }
        });

        mInvitationsClient.registerInvitationCallback(mInvitationCallback);
    }

    private InvitationCallback mInvitationCallback = new InvitationCallback() {
        @Override
        public void onInvitationReceived(@NonNull final Invitation invitation) {
            showInviteDialog(invitation);
        }

        @Override
        public void onInvitationRemoved(@NonNull String s) {
            //STUB
        }
    };

    private void onDisconnected() {
        mPlayersClient = null;
        mGamesClient = null;
        mLeaderboardsClient = null;

        mInvitationsClient.unregisterInvitationCallback(mInvitationCallback);
        mInvitationsClient = null;

        mPlayerLabel.setText(R.string.signedOut);
    }
}
