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
import com.google.android.gms.games.*;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.game.GameDifficulty;
import com.sigma.sudokuworld.game.gen.PuzzleGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ConnectionViewModel extends AndroidViewModel {

    private static final String TAG = "Multiplayer";

    private static final int MIN_PLAYERS = 2;
    private static final int MIN_OPPONENTS = 1;
    private static final int MAX_OPPONENTS = 3;

    //Protocol
    private static byte FILL_SQUARE_PROTOCOL = 1;
    private static byte EMPTY_SQUARE_PROTOCOL = 2;
    private static byte PUZZLE_PROTOCOL = 3;
    private static byte WINNER_PROTOCOL = 4;

    //Clients
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient;
    private PlayersClient mPlayersClient;
    private LeaderboardsClient mLeaderboardsClient;

    //My account
    private GoogleSignInAccount mGoogleSignInAccount;
    private String mMyPlayerID;

    //Room
    private String mRoomID;
    private RoomConfig mRoomConfig;
    private ArrayList<Participant> mParticipants;
    private String mHostParticipantID;
    private String mMyParticipantID;

    //Intents
    private Intent mWaitingRoomIntent;
    private Intent mSelectOpponentsIntent;

    //Game
    private int[] mInitialCells;
    private int[] mSolutionCells;
    public enum GameState {
        NEW, INVITE, LOBBY, SETUP, PLAYING, OVER, ERROR, LEAVE, PEER_LEFT, SINGED_OUT
    }

    //Game over
    private Participant mWinnerParticipant;

    //ViewModel
    private Application mApplication;
    private MutableLiveData<GameState> mGameStateLiveData;
    private MutableLiveData<Integer> mCompetitorFilledCell;
    private MutableLiveData<Integer> mCompetitorEmptiedCell;


    public ConnectionViewModel(@NonNull Application application) {
        super(application);

        mApplication = application;
        mGameStateLiveData = new MutableLiveData<>();
        mCompetitorFilledCell = new MutableLiveData<>();
        mCompetitorEmptiedCell = new MutableLiveData<>();

        mGoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(mApplication);
        if (mGoogleSignInAccount == null) {
            updateGameState(GameState.SINGED_OUT);
        } else {
            updateGameState(GameState.NEW);
            onConnected();
        }
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
        mLeaderboardsClient = Games.getLeaderboardsClient(mApplication, mGoogleSignInAccount);
    }

    /*
        Intents for google services UI.
     */

    public Intent getSelectOpponentsIntent() {
        return mSelectOpponentsIntent;
    }

    public void setSelectOpponentsResult(int resultCode, Bundle data) {
        if (resultCode != RESULT_OK) {
            leaveRoom();
        } else {
            buildHostedRoom(data);
        }
    }

    public Intent getWaitingRoomIntent() {
        return mWaitingRoomIntent;
    }

    public void setWaitingRoomResult(int resultCode) {
        if (resultCode == RESULT_OK) {
            setupGame();
        } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM){
            leaveRoom();
        }
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

        PuzzleGenerator.Puzzle puzzle = new PuzzleGenerator(4).generatePuzzle(GameDifficulty.EASY);
        broadcastPuzzle(puzzle.getCellValues(), puzzle.getSoltuion());
    }

    public void claimWin() {
        Log.d(TAG, "claimWin: I WON");

        mWinnerParticipant = getParticipant(mMyParticipantID);
        broadcastWin();
    }

    private void endGame() {
        //TODO leave lobby gracefully
        updateLeaderboards(isWinnerMe());
        updateGameState(GameState.OVER);
    }


    /*
     *
     * - Room Session -
     *
     * */

    /**
     * Creates a new auto match lobby
     */
    public void newAutoMatchRoom() {
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS, MAX_OPPONENTS, 0);

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        mRealTimeMultiplayerClient.create(mRoomConfig);
    }

    public void newHostedRoom() {
        mRealTimeMultiplayerClient.getSelectOpponentsIntent(MIN_OPPONENTS, MAX_OPPONENTS).addOnSuccessListener(new OnSuccessListener<Intent>() {
            @Override
            public void onSuccess(Intent intent) {
                mSelectOpponentsIntent = intent;
                updateGameState(GameState.INVITE);
            }
        });
    }

    public void joinHostedRoom(String inviteID) {
        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setInvitationIdToAccept(inviteID)
                .build();

        mRealTimeMultiplayerClient.join(mRoomConfig);
    }

    private void buildHostedRoom(Bundle data) {

        //Settings
        final ArrayList<String> invitees = data.getStringArrayList(Games.EXTRA_PLAYER_IDS);
        int minAutoPlayers = data.getInt(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoPlayers = data.getInt(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

        RoomConfig.Builder builder = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .addPlayersToInvite(invitees);

        if (minAutoPlayers > 0) {
            Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoPlayers, maxAutoPlayers, 0);
            builder.setAutoMatchCriteria(autoMatchCriteria);
        }

        mRoomConfig = builder.build();

        mRealTimeMultiplayerClient.create(mRoomConfig);
    }

    private RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {

        @Override
        public void onRoomCreated(int statusCode, @Nullable Room room) {
            Log.d(TAG, "onRoomCreated: CREATED ROOM");

            if (statusCode != GamesCallbackStatusCodes.OK)
                gameError("OnRoomCreate: ERROR CODE  " + GamesCallbackStatusCodes.getStatusCodeString(statusCode));

            mRealTimeMultiplayerClient.getWaitingRoomIntent(room, MIN_PLAYERS).addOnSuccessListener(new OnSuccessListener<Intent>() {
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

            mRealTimeMultiplayerClient.getWaitingRoomIntent(room, MIN_PLAYERS).addOnSuccessListener(new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {
                    mWaitingRoomIntent = intent;
                    updateGameState(GameState.LOBBY);
                }
            });
        }

        @Override
        public void onLeftRoom(int statusCode, @NonNull String s) {
            Log.d(TAG, "onLeftRoom: LEFT ROOM");

            //Called through leaveRoom
        }

        //Called when all players are connected
        @Override
        public void onRoomConnected(int statusCode, @Nullable Room room) {
            Log.d(TAG, "onRoomConnected: CONNECTED TO ROOM" +
                    "\nROOM ID: " + room.getRoomId() +
                    "\nCREATOR ID: " + room.getCreatorId());

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
            Log.d(TAG, "onPeerJoined: PEER JOINED ROOM");
            updateRoom(room);
        }

        @Override
        public void onPeerLeft(@Nullable Room room, @NonNull List<String> list) {
            Log.d(TAG, "onPeerLeft: PEER LEFT ROOM");
            updateRoom(room);
            updateGameState(GameState.PEER_LEFT);
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

            if (bytes[0] == FILL_SQUARE_PROTOCOL) {
                mCompetitorFilledCell.setValue(((int) bytes[1]));
            }

            else if (bytes[0] == EMPTY_SQUARE_PROTOCOL) {
                mCompetitorEmptiedCell.setValue((int) bytes[1]);
            }

            else if (bytes[0] == PUZZLE_PROTOCOL) {
                Log.d(TAG, "onRealTimeMessageReceived: PUZZLE_PROTOCOL RECEIVED");

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

            else if (bytes[0] == WINNER_PROTOCOL) {
                mWinnerParticipant =  getParticipant(realTimeMessage.getSenderParticipantId());
                endGame();
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


        bytes[bytePosition] = PUZZLE_PROTOCOL;
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
                            Log.d(TAG, "onSuccess: PUZZLE_PROTOCOL SUCCESSFULLY DELIVERED");
                            startGame(initial, solution);   //TODO wont work with more players. Should only start once all of them have won;
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

        bytes[0] = (isFilled ? FILL_SQUARE_PROTOCOL : EMPTY_SQUARE_PROTOCOL);
        bytes[1] = (byte) cellNumber;

        mRealTimeMultiplayerClient.sendUnreliableMessageToOthers(bytes, mRoomID); //TODO: reliable msg?
    }

    private void broadcastWin() {

        for (Participant p : mParticipants) {
            if (isParticipantMe(p.getParticipantId())) continue;
            byte[] bytes = { WINNER_PROTOCOL };

            mRealTimeMultiplayerClient.sendReliableMessage(
                    bytes,
                    mRoomID,
                    p.getParticipantId(),
                    null)
                    .addOnSuccessListener(new OnSuccessListener<Integer>() {
                        @Override
                        public void onSuccess(Integer integer) {
                            Log.d(TAG, "onSuccess: GAME_WIN_PROTOCOL SUCCESSFULLY DELIVERED");
                            endGame();
                        }
                    });
        }
    }

    /*
     *
     * - Misc -
     *
     * */

    /**
     * Updates both the wins and game played boards
     * @param didWin did the player win
     */
    private void updateLeaderboards(final boolean didWin) {
        final String winsBoardID = mApplication.getString(R.string.leaderboard_wins);
        final String gamesBoardID = mApplication.getString(R.string.leaderboard_games);

        //increments the number of games won by the user
        mLeaderboardsClient
                .loadCurrentPlayerLeaderboardScore(
                    winsBoardID,
                    LeaderboardVariant.TIME_SPAN_ALL_TIME,
                    LeaderboardVariant.COLLECTION_PUBLIC)
                .addOnSuccessListener(new OnSuccessListener<AnnotatedData<LeaderboardScore>>() {
                    @Override
                    public void onSuccess(AnnotatedData<LeaderboardScore> leaderboardScoreAnnotatedData) {
                        Log.d(TAG, "onClaimWin: SUCCESSFULLY FETCHED WINS SCOREBOARD");

                        long wins;
                        LeaderboardScore score = leaderboardScoreAnnotatedData.get();

                        if (score != null) {
                            wins = score.getRawScore();
                            wins += didWin ? 1 : 0;

                        } else wins = didWin ? 1 : 0;

                        mLeaderboardsClient.submitScore(winsBoardID, wins);
                    }});

        //increments the number of games played by the user
        mLeaderboardsClient.loadCurrentPlayerLeaderboardScore(
                gamesBoardID,
                LeaderboardVariant.TIME_SPAN_ALL_TIME,
                LeaderboardVariant.COLLECTION_PUBLIC)
                .addOnSuccessListener(new OnSuccessListener<AnnotatedData<LeaderboardScore>>() {
                    @Override
                    public void onSuccess(AnnotatedData<LeaderboardScore> leaderboardScoreAnnotatedData) {
                        Log.d(TAG, "onClaimWin: SUCCESSFULLY FETCHED GAMES SCOREBOARD");

                        long games;
                        LeaderboardScore score = leaderboardScoreAnnotatedData.get();

                        if (score != null) {
                            games = score.getRawScore();
                            games++;
                        } else games = 1;

                        mLeaderboardsClient.submitScore(gamesBoardID, games);
                    }
                });
    }

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
        if (state != mGameStateLiveData.getValue()) {
            mGameStateLiveData.setValue(state);
        }
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

    public void leaveGameRoom() {
        leaveRoom();
    }

    public Participant getWinnerParticipant() {
        return mWinnerParticipant;
    }

    public boolean isWinnerMe() {
        if (mWinnerParticipant == null) return false;

        return isParticipantMe(mWinnerParticipant.getParticipantId());
    }

    private Participant getParticipant(String participantID) {
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(participantID)) {
                return p;
            }
        }

        return null;
    }
}
