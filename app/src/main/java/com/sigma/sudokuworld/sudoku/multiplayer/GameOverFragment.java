package com.sigma.sudokuworld.sudoku.multiplayer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.gms.games.multiplayer.Participant;
import com.sigma.sudokuworld.R;

public class GameOverFragment extends Fragment {
    private static final String WINNER_NAME_KEY = "name";
    private static final String IS_WINNER_KEY = "winner?";

    private View mView;
    private boolean isWinner;
    private String winnerName;


    public static GameOverFragment newInstance(Participant winner, boolean isWinner) {
        Bundle args = new Bundle();
        args.putBoolean(IS_WINNER_KEY, isWinner);

        String winnerName;
        if (winner !=  null) winnerName = winner.getDisplayName();
        else winnerName = "Unknown";

        args.putString(WINNER_NAME_KEY, winnerName);
        args.putBoolean(IS_WINNER_KEY, isWinner);

        GameOverFragment fragment = new GameOverFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            isWinner = args.getBoolean(IS_WINNER_KEY, false);
            winnerName = args.getString(WINNER_NAME_KEY, "Unknown");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         mView = inflater.inflate(R.layout.fragment_game_over, container, false);

        String msg;
        if (isWinner) {
            msg = "You won!";
        } else {
            msg = winnerName + " won!";
        }

        ((TextView) mView.findViewById(R.id.whoWon)).setText(msg);

        return mView;
    }
}
