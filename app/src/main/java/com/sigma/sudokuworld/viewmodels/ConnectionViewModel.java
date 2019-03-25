package com.sigma.sudokuworld.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
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
import com.sigma.sudokuworld.game.GameDifficulty;
import com.sigma.sudokuworld.game.gen.PuzzleGenerator;
import com.sigma.sudokuworld.persistence.sharedpreferences.KeyConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ConnectionViewModel extends AndroidViewModel {

    private static final String TAG = "Mutiplayer";
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
    private String mHostParticipantID;
    private String mMyParticipantID;
    private Intent mWaitingRoomIntent;

    //Game
    private boolean isGameStarted;
    private int[] mInitialCells;
    private int[] mSolutionCells;

    //ViewModel
    private Application mApplication;
    private MutableLiveData<GameState> mGameStateLiveData;
    private MutableLiveData<Integer> mCompetitorFilledCell;
    private MutableLiveData<Integer> mCompetitorEmptiedCell;

    public ConnectionViewModel(@NonNull Application application) {
        super(application);

        mApplication = application;
        mGoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(mApplication);

        if (mGoogleSignInAccount == null) {
            updateGameState(GameState.SINGED_OUT);
        }

        isGameStarted = false;
        mGameStateLiveData = new MutableLiveData<>();
        mCompetitorFilledCell = new MutableLiveData<>();
        mCompetitorEmptiedCell = new MutableLiveData<>();

        updateGameState(GameState.NEW);

        onConnected();
        newAutoMatchRoom();
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        leaveRoom();
    }


    /**
     * Connect to google
     */
    private void onConnected() {
        Log.d(TAG, "onConnected: CONNECTED TO GOOGLE");

        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(mApplication, mGoogleSignInAccount);
        mPlayersClient = Games.getPlayersClient(mApplication, mGoogleSignInAccount);
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
        updateGameState(GameState.SETUP);

        mHostParticipantID = chooseHost();

        if (isParticipantMe(mHostParticipantID)) {
            performHostSetup();
        }
    }

    private void startGame(int[] initialCells, int[] solution) {
        mInitialCells = initialCells;
        mSolutionCells = solution;

        isGameStarted = true;
        updateGameState(GameState.PLAYING);
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

        int[] initial = puzzle.getIntArray(KeyConstants.CELL_VALUES_KEY);
        int[] solution = puzzle.getIntArray(KeyConstants.SOLUTION_VALUES_KEY);

        broadcastPuzzle(initial, solution);
    }

    /**
     * Logic for if we should cancel the game
     * @return should we cancel the game
     */
    private boolean shouldCancelGame() {
        if (isGameStarted) {
            int playerCount = 0;

            for (Participant p : mParticipants) {
                if (p.isConnectedToRoom()) playerCount++;
            }

            return (playerCount < MIN_PLAYER_COUNT);
        }

        return false;
    }

    /*
     *
     * - Room Session -
     *
     * */

    /**
     * Creates a new auto match lobby
     */
    private void newAutoMatchRoom() {
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(1, 1, 0);

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        mRealTimeMultiplayerClient.create(mRoomConfig);
    }

    private RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {
        @Override
        public void onRoomCreated(int statusCode, @Nullable Room room) {
            Log.d(TAG, "onRoomCreated: CREATED ROOM");

            if (statusCode != GamesCallbackStatusCodes.OK)
                gameError("OnRoomCreate: ERROR CODE  " + GamesCallbackStatusCodes.getStatusCodeString(statusCode));

            mRealTimeMultiplayerClient.getWaitingRoomIntent(room, 2).addOnSuccessListener(new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {
                    mWaitingRoomIntent = intent;
                    updateGameState(GameState.LOBBY);
                }
            });
        }

        @Override
        public void onJoinedRoom(int statusCode, @Nullable Room room) {
            Log.d(TAG, "onJoinedRoom: JOINED ROOM");

            if (statusCode != GamesCallbackStatusCodes.OK)
                gameError("OnRoomJoin: ERROR CODE  " + GamesCallbackStatusCodes.getStatusCodeString(statusCode));

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
                gameError("OnRoomConnected: ERROR CODE  " + GamesCallbackStatusCodes.getStatusCodeString(statusCode));

            updateRoom(room);
            mRoomID = room.getRoomId();
            mMyParticipantID = room.getParticipantId(mMyPlayerID);
        }
    };

    private RoomStatusUpdateCallback mRoomStatusUpdateCallback = new RoomStatusUpdateCallback() {
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

    private void updateRoom(Room room) {
        if (room != null) mParticipants = room.getParticipants();

        if (shouldCancelGame()) {
            updateGameState(GameState.PEER_LEFT);
        }
    }

    /**
     * Leave the room gracefully and return to the menu
     */
    private void leaveRoom() {
        Log.d(TAG, "leaveRoom: LEAVING ROOM");
        updateGameState(GameState.LEAVE);

        if (mRoomID != null) {
            mRealTimeMultiplayerClient.leave(mRoomConfig, mRoomID);
        }
    }

    /*
     *
     * - Communication -
     *
     * */

    /**
     * Listener for incoming messages
     */
    private OnRealTimeMessageReceivedListener mMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            byte[] bytes = realTimeMessage.getMessageData();

            if (bytes[0] == RealtimeProtocol.FILL_SQUARE) {
                mCompetitorFilledCell.setValue(((int) bytes[1]));
            }

            else if (bytes[0] == RealtimeProtocol.UNFILL_SQUARE) {
                mCompetitorEmptiedCell.setValue((int) bytes[1]);
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

    /**
     * Broadcast the puzzle the to lobby
     * @param initial cells
     * @param solution cells
     */
    private void broadcastPuzzle(final int[] initial, final int[] solution) {

        int bytePosition = 0;
        byte[] bytes = new byte[2 + initial.length + solution.length];


        bytes[bytePosition] = RealtimeProtocol.PUZZLE;
        bytePosition++;

        //Puzzle length
        bytes[bytePosition] = (byte) initial.length;
        bytePosition++;

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

    /**
     * Sends a move to the opponent
     * @param cellNumber cell
     * @param isFilled is the cell filled or empty
     */
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

    public int[] getInitialCells() {
        return mInitialCells;
    }

    public int[] getSolutionCells() {
        return mSolutionCells;
    }

    private boolean isParticipantMe(String participantID) {
        return mMyParticipantID.equals(participantID);
    }

    private void gameError(String msg) {
        Log.d(TAG, msg);

        updateGameState(GameState.ERROR);
    }

    private void updateGameState(GameState state) {
        mGameStateLiveData.setValue(state);
    }

    public LiveData<GameState> getGameStateLiveData() {
        return mGameStateLiveData;
    }

    public LiveData<Integer> getCompetitorFilledCell() {
        return mCompetitorFilledCell;
    }

    public LiveData<Integer> getCompetitorEmptiedCell() {
        return mCompetitorEmptiedCell;
    }

    public Intent getWaitingRoomIntent() {
        return mWaitingRoomIntent;
    }

    public void setWaitingRoomResult(int resultCode) {
        if (resultCode == RESULT_OK) {
            setupGame();
        } else {
            leaveRoom();
        }
    }

    public enum GameState {
        NEW, LOBBY, SETUP, PLAYING, ERROR, LEAVE, PEER_LEFT, SINGED_OUT
    }
}
