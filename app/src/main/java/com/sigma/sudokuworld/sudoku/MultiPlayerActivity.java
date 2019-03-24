package com.sigma.sudokuworld.sudoku;

import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.WindowManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.*;
import com.google.android.gms.tasks.OnSuccessListener;
import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.SettingsFragment;
import com.sigma.sudokuworld.viewmodels.RealtimeProtocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiPlayerActivity extends SudokuActivity {
    private static final String TAG = "Mutiplayer";
    private static final int RC_WAITING_ROOM = 276;

    private static final int MIN_PLAYER_COUNT = 2;

    private GoogleSignInAccount mAccount;
    private RealTimeMultiplayerClient mMultiplayerClient;
    private RoomConfig mRoomConfig;
    private ArrayList<Participant> mParticipants;

    private String mRoomID;
    private String mHostPlayerID;

    private boolean isGameStarted;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (mAccount == null) {
            finish();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LoadingSrceenFragment()).commit();

        mMultiplayerClient = Games.getRealTimeMultiplayerClient(this, mAccount);
        isGameStarted = false;

        newQuickGame();
    }

    @Override
    protected void onStop() {
        super.onStop();

        leaveRoom();
    }

    private void newQuickGame() {
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(1, 1, 0);

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mMultiplayerClient.create(mRoomConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_WAITING_ROOM) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: STARTING GAME");
                startGame();
            } else {
                leaveRoom();
            }
        }
    }

    private void startGame() {
        isGameStarted = true;

        String[] ids = new String[mParticipants.size()];
        for (int i = 0; i < mParticipants.size(); i++) {
            ids[i] = mParticipants.get(i).getParticipantId();
        }

        Arrays.sort(ids);
        mHostPlayerID = ids[0];

        mSudokuViewModel.getLastCellChanged().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer != null) {
                    if (mSudokuViewModel.getCellValue(integer) != 0) {
                        broadcastMove(integer, true);
                    } else {
                        broadcastMove(integer, false);
                    }
                }
            }
        });
    }

    private void showGameError(String msg) {
        Log.d(TAG, msg);
        new AlertDialog.Builder(this).setMessage(msg).setNeutralButton("ok", null).create();
    }

    RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {
        @Override
        public void onRoomCreated(int statusCode, @Nullable Room room) {
            Log.d(TAG, "onRoomCreated: CREATED ROOM");

            if (statusCode != GamesCallbackStatusCodes.OK)
                showGameError("OnRoomCreate: ERROR CODE  " + GamesCallbackStatusCodes.getStatusCodeString(statusCode));

            mRoomID = room.getRoomId();

            mMultiplayerClient.getWaitingRoomIntent(room, 2)
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

            mRoomID = room.getRoomId();
            updateRoom(room);
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

    private void leaveRoom() {
        Log.d(TAG, "leaveRoom: LEAVING ROOM");

        if (mRoomID != null) {
            mMultiplayerClient.leave(mRoomConfig, mRoomID);
        }

        finish();
    }

    OnRealTimeMessageReceivedListener mMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            byte[] bytes = realTimeMessage.getMessageData();

            if (bytes[0] == RealtimeProtocol.FILL_SQUARE) {
                mSudokuViewModel.setCompetitorFilledCell(bytes[1], true);
            }

            else if (bytes[0] == RealtimeProtocol.UNFILL_SQUARE) {
                 mSudokuViewModel.setCompetitorFilledCell(bytes[1], false);
            }
        }
    };

    public void broadcastMove(int cellNumber, boolean isFilled) {
        byte[] bytes = new byte[2];

        bytes[0] = (isFilled ? RealtimeProtocol.FILL_SQUARE : RealtimeProtocol.UNFILL_SQUARE);
        bytes[1] = (byte) cellNumber;

        mMultiplayerClient.sendUnreliableMessageToOthers(bytes, mRoomID); //TODO: reliable msg?
    }
}
