package com.sigma.sudokuworld.sudoku;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.WindowManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.game.GameDifficulty;
import com.sigma.sudokuworld.game.gen.PuzzleGenerator;
import com.sigma.sudokuworld.persistence.sharedpreferences.KeyConstants;
import com.sigma.sudokuworld.viewmodels.MultiplayerViewModel;
import com.sigma.sudokuworld.viewmodels.MultiplayerViewModelFactory;
import com.sigma.sudokuworld.viewmodels.RealtimeProtocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiplayerActivity extends SudokuActivity {
    private static final String TAG = "Mutiplayer";
    private static final int RC_WAITING_ROOM = 276;

    private static final int MIN_PLAYER_COUNT = 2;

    //Clients
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient;
    private PlayersClient mPlayersClient;

    //My account
    private GoogleSignInAccount mGoogleSignInAccount;
    private String mMyPlayerID;

    //Room
    private String mRoomID;
    private RoomConfig mRoomConfig;
    private ArrayList<Participant> mParticipants;

    //Game
    private MultiplayerViewModel mMultiplayerViewModel;
    private FragmentManager mFragmentManager;

    private String mHostParticipantID;
    private String mMyParticipantID;
    private boolean isGameStarted;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO make rotation compatible, Currently boots you out of game on rotate. Put in view model?

        GoogleSignInAccount mAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (mAccount == null) {
            finish();
        }
        onConnected(mAccount);

        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction().replace(R.id.fragment_container, new LoadingSrceenFragment()).commit();

        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(this, mAccount);
        isGameStarted = false;

        newAutoMatchRoom();
    }

    @Override
    protected void onStop() {
        super.onStop();

        leaveRoom();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_WAITING_ROOM) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: STARTING GAME");
                setupGame();
            } else {
                leaveRoom();
            }
        }
    }

    /*
     *
     * - Sign in / out -
     *
     * */

    private void onConnected(GoogleSignInAccount account) {
        Log.d(TAG, "onConnected: CONNECTED TO GOOGLE");

        mGoogleSignInAccount = account;
        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(this, account);

        mPlayersClient = Games.getPlayersClient(this, account);
        mPlayersClient.getCurrentPlayerId().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                mMyPlayerID = task.getResult();
            }
        });
    }


    /*
     *
     * - Game Logic -
     *
     * */

    /**
     * Called when all players have connected
     */
    private void setupGame() {
        mHostParticipantID = chooseHost();

        if (isParticipantMe(mHostParticipantID)) {
            performHostSetup();
        }
    }

    private void startGame(int[] initialCells, int[] solution) {
        isGameStarted = true;

        MultiplayerViewModelFactory factory =
                new MultiplayerViewModelFactory(getApplication(), initialCells, solution);


        mMultiplayerViewModel = ViewModelProviders.of(this, factory).get(MultiplayerViewModel.class);
        super.setGameViewModel(mMultiplayerViewModel);

        //Observe when cells are changed and broadcast to opponent
        mMultiplayerViewModel.getLastCellChanged().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer != null) {
                    if (mMultiplayerViewModel.getCellValue(integer) != 0) {
                        broadcastMove(integer, true);
                    } else {
                        broadcastMove(integer, false);
                    }
                }
            }
        });


        mFragmentManager.beginTransaction().remove(mFragmentManager.findFragmentById(R.id.fragment_container)).commit();
    }

    /**
     * Logic for if we should cancel the game
     * @param room game room
     * @return should we cancel the game
     */
    private boolean shouldCancelGame(Room room) {
        if (isGameStarted) {
            int playerCount = 0;

            for (Participant p : mParticipants) {
                if (p.isConnectedToRoom()) playerCount++;
            }

            return (playerCount < MIN_PLAYER_COUNT);
        }

        return false;
    }

    /**
     * Chooses a host for the game.
     * All clients will have the same host
     * @return host participantID
     */
    private String chooseHost() {

        //Sorts the id's and choose the first one
        String[] ids = new String[mParticipants.size()];
        for (int i = 0; i < mParticipants.size(); i++) {
            ids[i] = mParticipants.get(i).getParticipantId();
        }
        Arrays.sort(ids);

        return ids[0];
    }

    /**
     * Host creates then uploads the puzzle to FireBase and broadcasts the puzzle location to peers
     */
    private void performHostSetup() {
        Log.d(TAG, "performHostSetup: I AM HOST");

        Bundle puzzle = new PuzzleGenerator(9).generatePuzzle(GameDifficulty.EASY);

        int[] initialCells = puzzle.getIntArray(KeyConstants.CELL_VALUES_KEY);
        int[] solution = puzzle.getIntArray(KeyConstants.SOLUTION_VALUES_KEY);

        broadcastPuzzle(initialCells, solution);
    }

    /*
     *
     * - Room Session -
     *
     * */

    private void newAutoMatchRoom() {
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(1, 1, 0);

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mRealTimeMultiplayerClient.create(mRoomConfig);
    }

    RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {
        @Override
        public void onRoomCreated(int statusCode, @Nullable Room room) {
            Log.d(TAG, "onRoomCreated: CREATED ROOM");

            if (statusCode != GamesCallbackStatusCodes.OK)
                showGameError("OnRoomCreate: ERROR CODE  " + GamesCallbackStatusCodes.getStatusCodeString(statusCode));

            mRealTimeMultiplayerClient.getWaitingRoomIntent(room, 2)
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            startActivityForResult(intent, RC_WAITING_ROOM);
                        }
                    });
        }

        @Override
        public void onJoinedRoom(int statusCode, @Nullable Room room) {
            Log.d(TAG, "onJoinedRoom: JOINED ROOM");

            if (statusCode != GamesCallbackStatusCodes.OK)
                showGameError("OnRoomJoin: ERROR CODE  " + GamesCallbackStatusCodes.getStatusCodeString(statusCode));

            //TODO: figure out what goes here
        }

        @Override
        public void onLeftRoom(int statusCode, @NonNull String s) {
            Log.d(TAG, "onLeftRoom: LEFT ROOM");

            //Called through leaveRoom
        }

        //Called when all players are connected
        @Override
        public void onRoomConnected(int statusCode, @Nullable Room room) {
            Log.d(TAG, "onRoomConnected: CONNECTED TO ROOM");

            if (statusCode != GamesCallbackStatusCodes.OK)
                showGameError("OnRoomConnected: ERROR CODE  " + GamesCallbackStatusCodes.getStatusCodeString(statusCode));

            updateRoom(room);
            mRoomID = room.getRoomId();
            mMyParticipantID = room.getParticipantId(mMyPlayerID);
        }
    };

    RoomStatusUpdateCallback mRoomStatusUpdateCallback = new RoomStatusUpdateCallback() {
        @Override
        public void onRoomConnecting(@Nullable Room room) {
            updateRoom(room);
        }

        @Override
        public void onRoomAutoMatching(@Nullable Room room) {
            updateRoom(room);
        }

        @Override
        public void onPeerInvitedToRoom(@Nullable Room room, @NonNull List<String> list) {
            updateRoom(room);
        }

        @Override
        public void onPeerDeclined(@Nullable Room room, @NonNull List<String> list) {
            updateRoom(room);
        }

        @Override
        public void onPeerJoined(@Nullable Room room, @NonNull List<String> list) {
            updateRoom(room);
        }

        @Override
        public void onPeerLeft(@Nullable Room room, @NonNull List<String> list) {
            updateRoom(room);
        }

        @Override
        public void onConnectedToRoom(@Nullable Room room) {
            updateRoom(room);
        }

        @Override
        public void onDisconnectedFromRoom(@Nullable Room room) {
            updateRoom(room);
        }

        @Override
        public void onPeersConnected(@Nullable Room room, @NonNull List<String> list) {
            updateRoom(room);
        }

        @Override
        public void onPeersDisconnected(@Nullable Room room, @NonNull List<String> list) {
            updateRoom(room);
        }

        @Override
        public void onP2PConnected(@NonNull String s) {
            //Stub
        }

        @Override
        public void onP2PDisconnected(@NonNull String s) {
            //Stub
        }
    };

    public void updateRoom(Room room) {
        if (room != null) mParticipants = room.getParticipants();

        if (shouldCancelGame(room)) {
            new AlertDialog.Builder(this)
                    .setMessage("Player Disconnected")
                    .setNeutralButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            leaveRoom();
                        }
                    })
                    .show();
        }
    }

    /**
     * Leave the room gracefully and return to the menu
     */
    private void leaveRoom() {
        Log.d(TAG, "leaveRoom: LEAVING ROOM");

        if (mRoomID != null) {
            mRealTimeMultiplayerClient.leave(mRoomConfig, mRoomID);
        }

        finish();
    }

    /*
    *
    * - Communication -
    *
    * */

    OnRealTimeMessageReceivedListener mMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            byte[] bytes = realTimeMessage.getMessageData();

            if (bytes[0] == RealtimeProtocol.FILL_SQUARE) {
                if (mMultiplayerViewModel == null) {
                    Log.d(TAG, "onRealTimeMessageReceived: MOVE RECEIVED BEFORE READY FOR GAME");
                    return;
                }


                mMultiplayerViewModel.setCompetitorFilledCell(bytes[1], true);
            }

            else if (bytes[0] == RealtimeProtocol.UNFILL_SQUARE) {
                if (mMultiplayerViewModel == null) {
                    Log.d(TAG, "onRealTimeMessageReceived: MOVE RECEIVED BEFORE READY FOR GAME");
                    return;
                }

                mMultiplayerViewModel.setCompetitorFilledCell(bytes[1], false);
            }

            else if (bytes[0] == RealtimeProtocol.PUZZLE) {
                Log.d(TAG, "onRealTimeMessageReceived: PUZZLE RECEIVED");

                int size = bytes[1];

                int[] initialCells = new int[size];
                int[] solution = new int[size];

                int bytePosition = 2;
                for (int i = 0; i < size; i++, bytePosition++) {
                    initialCells[i] = bytes[bytePosition];
                }

                for (int i = 0; i < size; i++, bytePosition++) {
                    solution[i] = bytes[bytePosition];
                }

                startGame(initialCells, solution);
            }
        }
    };

    public void broadcastPuzzle(final int[] initial, final int[] solution) {

        int bytePosition = 0;
        byte[] bytes = new byte[2 + initial.length + solution.length];


        bytes[bytePosition] = RealtimeProtocol.PUZZLE;
        bytePosition++;

        //Puzzle length
        bytes[bytePosition] = (byte) initial.length;
        bytePosition++;

        //TODO loop unrolling?
        for (int i = 0; i < initial.length; i++, bytePosition++) {
            bytes[bytePosition] = (byte) initial[i];
        }

        for (int i = 0; i < solution.length; i++, bytePosition++) {
            bytes[bytePosition] = (byte) solution[i];
        }

        for (Participant p : mParticipants) {
            if (isParticipantMe(p.getParticipantId())) continue;

            mRealTimeMultiplayerClient.sendReliableMessage(
                    bytes,
                    mRoomID,
                    p.getParticipantId(),
                    null)
                    .addOnSuccessListener(new OnSuccessListener<Integer>() {
                @Override
                public void onSuccess(Integer integer) {
                    Log.d(TAG, "onSuccess: PUZZLE SUCCESSFULLY DELIVERED");
                    startGame(initial, solution);
                }
            });
        }
    }

    public void broadcastMove(int cellNumber, boolean isFilled) {
        byte[] bytes = new byte[2];

        bytes[0] = (isFilled ? RealtimeProtocol.FILL_SQUARE : RealtimeProtocol.UNFILL_SQUARE);
        bytes[1] = (byte) cellNumber;

        mRealTimeMultiplayerClient.sendUnreliableMessageToOthers(bytes, mRoomID); //TODO: reliable msg?
    }

    /*
     *
     * - Misc -
     *
     * */

    private boolean isParticipantMe(String participantID) {
        return mMyParticipantID.equals(participantID);
    }

    private void showGameError(String msg) {
        Log.d(TAG, msg);
        new AlertDialog.Builder(this).setMessage(msg).setNeutralButton("ok", null).create();
    }
}
