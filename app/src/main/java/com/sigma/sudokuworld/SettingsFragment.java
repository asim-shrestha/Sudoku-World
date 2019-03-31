package com.sigma.sudokuworld;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import com.sigma.sudokuworld.viewmodels.MenuViewModel;

public class SettingsFragment extends Fragment {
    private MenuViewModel mMenuViewModel;

    private Switch mAudioModeSwitch;
    private Switch mSfxSwitch;
    private Switch mMusicSwitch;
    private Switch mHintsSwitch;
    private Switch mRectangleSwitch;

    private Button mCancelButton;
    private Button mSaveButton;
    private View mView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMenuViewModel = ViewModelProviders.of(this).get(MenuViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceStace) {
        mView = inflater.inflate(R.layout.fragment_settings, container, false);
        mAudioModeSwitch = mView.findViewById(R.id.audioModeSwitch);
        mSfxSwitch = mView.findViewById(R.id.sfxSwitch);
        mMusicSwitch = mView.findViewById(R.id.musicSwitch);
        mHintsSwitch = mView.findViewById(R.id.hintsSwitch);
        mRectangleSwitch = mView.findViewById(R.id.rectangleMode);
        mCancelButton = mView.findViewById(R.id.settingsCancelButton);
        mSaveButton = mView.findViewById(R.id.settingsSaveButton);

        mCancelButton.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MenuActivity) getActivity()).closeFragment();
                }
        });

        mSaveButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
                ((MenuActivity) getActivity()).closeFragment();
            }
        });

        loadSettings();
        return mView;
    }

    private void loadSettings() {
        mAudioModeSwitch.setChecked(mMenuViewModel.isAudioModeEnabled());
        mSfxSwitch.setChecked(mMenuViewModel.isSfxEnabled());
        mMusicSwitch.setChecked(mMenuViewModel.isMusicEnabled());
        mHintsSwitch.setChecked(mMenuViewModel.isHintsEnabled());
        mRectangleSwitch.setChecked(mMenuViewModel.isRectangleModeEnabled());
    }

    private void saveSettings() {
        mMenuViewModel.setAudioModeEnabled(mAudioModeSwitch.isChecked());
        mMenuViewModel.setSfxEnabled(mSfxSwitch.isChecked());
        mMenuViewModel.setMusicEnabled(mMusicSwitch.isChecked());
        mMenuViewModel.setHintsEnabled(mHintsSwitch.isChecked());
        mMenuViewModel.setRectangleModeEnabled(mRectangleSwitch.isChecked());
    }
}
