package com.sigma.sudokuworld;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MultiplayerFragment extends Fragment {
    private View mView;
    private Button mQuickGameButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_mutliplayer, container, false);

        mQuickGameButton = mView.findViewById(R.id.quickGame);
        mQuickGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MenuActivity) getActivity()).startMultiplayerGame(false);
            }
        });

        Button host = mView.findViewById(R.id.host);
        host.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MenuActivity) getActivity()).startMultiplayerGame(true);
            }
        });

        Button cancel = mView.findViewById(R.id.cancelButton);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MenuActivity) getActivity()).closeFragment();
            }
        });

        Button invites = mView.findViewById(R.id.invites);
        invites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MenuActivity) getActivity()).showInviteInbox();
            }
        });

        Button leader = mView.findViewById(R.id.leaderboards);
        leader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MenuActivity) getActivity()).showLeaderboard();
            }
        });

        return mView;
    }
}
