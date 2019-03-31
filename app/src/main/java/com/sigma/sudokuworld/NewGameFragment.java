package com.sigma.sudokuworld;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import android.widget.TextView;
import com.sigma.sudokuworld.game.GameDifficulty;
import com.sigma.sudokuworld.game.GameMode;
import com.sigma.sudokuworld.masterdetail.MasterSelectActivity;
import com.sigma.sudokuworld.persistence.db.entities.Set;
import com.sigma.sudokuworld.viewmodels.MenuViewModel;

public class NewGameFragment extends Fragment {
    private MenuViewModel mMenuViewModel;
    private View mView;
    private SeekBar mDifficultySeekBar;
    private SeekBar mBoardSizeSeekBar;
    private RadioGroup mGameModeRadioGroup;
    private View mSetLayout;
    private TextView mSetTitle;
    private Button mPlayButton;
    private Button mSetButton;
    private Button mCancelButton;
    private TextView mSizeWarning;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get the menu's viewModel
        mMenuViewModel = ViewModelProviders.of(this).get(MenuViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_new_game, container, false);
        mGameModeRadioGroup = mView.findViewById(R.id.gameModeRadioGroup);
        mDifficultySeekBar = mView.findViewById(R.id.difficultyBar);
        mBoardSizeSeekBar = mView.findViewById(R.id.boardSizeBar);
        mSetLayout = mView.findViewById(R.id.setViewLayout);
        mSetTitle = mView.findViewById(R.id.setTitle);
        mSizeWarning = mView.findViewById(R.id.setSizeWarning);

        mSetButton = mView.findViewById(R.id.setBuilderButton);
        mSetButton.setOnClickListener(setButtonListener);

        mPlayButton = mView.findViewById(R.id.playNewGameButton);
        mPlayButton.setOnClickListener(playButtonListener);

        mCancelButton = mView.findViewById(R.id.cancelButton);
        mCancelButton.setOnClickListener(cancelButtonListener);

        initStoredSettings();
        initLabelListeners();
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Set set = mMenuViewModel.getSelectedSet(); //TODO make buttons disappear better

        //Show Set size error
        final long setSize = mMenuViewModel.getSelectedSetSize();
        if (shouldShowSetSizeError(mBoardSizeSeekBar.getProgress(), setSize)) {
            mSizeWarning.setVisibility(View.VISIBLE);
        } else {
            mSizeWarning.setVisibility(View.GONE);
        }

        if (set == null) {
            mSetTitle.setVisibility(View.GONE);
            mPlayButton.setVisibility(View.GONE);

            Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
            mSetButton.startAnimation(shake);
        } else {
            mSetTitle.setVisibility(View.VISIBLE);
            mPlayButton.setVisibility(View.VISIBLE);
            mSetTitle.setText(set.getName());
        }

        mGameModeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == mView.findViewById(R.id.numbersModeRadioButton).getId()) {
                    mSetLayout.setVisibility(View.GONE);

                    //Hide the set size warning if in numbers mode
                    mSizeWarning.setVisibility(View.GONE);

                } else {
                    mSetLayout.setVisibility(View.VISIBLE);

                    //Show the set size warning if not in numbers mode
                    if(shouldShowSetSizeError(mBoardSizeSeekBar.getProgress(), setSize)) {
                        mSizeWarning.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        mBoardSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (shouldShowSetSizeError(progress, setSize)) {
                    mSizeWarning.setVisibility(View.VISIBLE);
                } else {
                    mSizeWarning.setVisibility(View.GONE);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveSettings();
    }

    View.OnClickListener playButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            saveSettings();
            ((MenuActivity) getActivity()).startGame(mMenuViewModel.generateNewGameWithStoredSettings());
        }
    };

    View.OnClickListener setButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Checking GameMode
            Intent intent = new Intent(getActivity().getBaseContext(), MasterSelectActivity.class);
            startActivity(intent);      //TODO start activity for result
        }
    };

    View.OnClickListener cancelButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((MenuActivity) getActivity()).closeFragment();
        }
    };

    private void initStoredSettings() {

        GameDifficulty difficulty = mMenuViewModel.getSelectedGameDifficulty();
        if (difficulty == GameDifficulty.EASY) {
            mDifficultySeekBar.setProgress(0);
        } else if (difficulty == GameDifficulty.MEDIUM) {
            mDifficultySeekBar.setProgress(1);
        } else {
            mDifficultySeekBar.setProgress(2);
        }

        int boardLength = mMenuViewModel.getSelectedBoardLength();
        if (boardLength == 4) {
            mBoardSizeSeekBar.setProgress(0);
        } else if (boardLength == 6) {
            mBoardSizeSeekBar.setProgress(1);
        } else if (boardLength == 9) {
            mBoardSizeSeekBar.setProgress(2);
        } else if (boardLength == 12) {
            mBoardSizeSeekBar.setProgress(3);
        } else {
            mBoardSizeSeekBar.setProgress(4);
        }


        GameMode mode = mMenuViewModel.getSelectedGameMode();
        if (mode == GameMode.NATIVE) {
            mGameModeRadioGroup.check(R.id.nativeModeRadioButton);
        } else if (mode == GameMode.FOREIGN) {
            mGameModeRadioGroup.check(R.id.foreignModeRadioButton);
        } else {
            mGameModeRadioGroup.check(R.id.numbersModeRadioButton);

            //Numbers mode selected hide set layout
            mSetLayout.setVisibility(View.GONE);
        }
    }

    private void initLabelListeners() {

        mView.findViewById(R.id.easyLabel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDifficultySeekBar.setProgress(0);
            }
        });

        mView.findViewById(R.id.mediumLabel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDifficultySeekBar.setProgress(1);
            }
        });

        mView.findViewById(R.id.hardLabel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDifficultySeekBar.setProgress(2);
            }
        });

        mView.findViewById(R.id.tinyLabel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBoardSizeSeekBar.setProgress(0);
            }
        });

        mView.findViewById(R.id.smallLabel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBoardSizeSeekBar.setProgress(1);
            }
        });

        mView.findViewById(R.id.normalLabel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBoardSizeSeekBar.setProgress(2);
            }
        });

        mView.findViewById(R.id.largeLabel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBoardSizeSeekBar.setProgress(3);
            }
        });

        mView.findViewById(R.id.hugeLabel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBoardSizeSeekBar.setProgress(4);
            }
        });
    }

    private void saveSettings(){

        //Checking GameMode
        int checkedRadioButtonID = mGameModeRadioGroup.getCheckedRadioButtonId();
        if (checkedRadioButtonID == R.id.nativeModeRadioButton) {
            mMenuViewModel.setSelectedGameMode(GameMode.NATIVE);
        } else if (checkedRadioButtonID == R.id.foreignModeRadioButton) {
            mMenuViewModel.setSelectedGameMode(GameMode.FOREIGN);
        } else {
            mMenuViewModel.setSelectedGameMode(GameMode.NUMBERS);
        }

        //Checking Difficulty
        int difficultyPos = mDifficultySeekBar.getProgress();
        if (difficultyPos == 0) {
            mMenuViewModel.setSelectedGameDifficulty(GameDifficulty.EASY);
        } else if (difficultyPos == 1) {
            mMenuViewModel.setSelectedGameDifficulty(GameDifficulty.MEDIUM);
        } else {
            mMenuViewModel.setSelectedGameDifficulty(GameDifficulty.HARD);
        }

        //Checking Board Length
        int boardLengthPos = mBoardSizeSeekBar.getProgress();
        if (boardLengthPos == 0) {
            mMenuViewModel.setSelectedBoardLength(4);
        } else if (boardLengthPos == 1) {
            mMenuViewModel.setSelectedBoardLength(6);
        } else if (boardLengthPos == 2){
            mMenuViewModel.setSelectedBoardLength(9);
        } else if (boardLengthPos == 3){
            mMenuViewModel.setSelectedBoardLength(12);
        } else {
            mMenuViewModel.setSelectedBoardLength(16);
        }
    }

    private boolean shouldShowSetSizeError(int seekBarPosition, long setSize) {
        boolean isTooSmall = false;

        switch (seekBarPosition) {
            case 0:
                if (setSize < 4) isTooSmall = true;
                break;
            case 1:
                if (setSize < 6) isTooSmall = true;
                break;
            case 2:
                if (setSize < 9) isTooSmall = true;
                break;
            case 3:
                if (setSize < 12) isTooSmall = true;
                break;
            case 4:
                if (setSize < 16) isTooSmall = true;
                break;
        }

        return isTooSmall;
    }
}
