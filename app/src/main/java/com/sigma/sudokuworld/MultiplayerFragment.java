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
                ((MenuActivity) getActivity()).startMultiplayerGame();
            }
        });

        return mView;
    }
}
