package com.sigma.sudokuworld.sudoku;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;


import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sigma.sudokuworld.SettingsFragment;
import com.sigma.sudokuworld.persistence.sharedpreferences.PersistenceService;
import com.sigma.sudokuworld.sudoku.singleplayer.LongTouchHandler;
import com.sigma.sudokuworld.viewmodels.GameViewModel;
import com.sigma.sudokuworld.viewmodels.SinglePlayerViewModel;
import com.sigma.sudokuworld.viewmodels.SingleplayerViewModelFactory;
import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.audio.SoundPlayer;
import com.sigma.sudokuworld.persistence.sharedpreferences.KeyConstants;

import java.util.ArrayList;
import java.util.List;

public abstract class SudokuActivity extends AppCompatActivity {

    protected SudokuGridView mSudokuGridView;
    protected int cellTouched;
    private GameViewModel mGameViewModel;
    protected LinearLayout[] mLinearLayouts;
    protected Button[] mInputButtons;
    protected ImageButton mBackButton;
    protected ImageButton mSettingsButton;
    private SoundPlayer mSoundPlayer;


    protected LongTouchHandler mLongTouchHandler;
    protected FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku);

        mGameViewModel = null;

        //Initializing Sudoku grid
        mSudokuGridView = findViewById(R.id.sudokuGrid_view);
        mSudokuGridView.setRectangleMode(PersistenceService.loadRectangleModeEnabledSetting(this));

        //Initialize Buttons
        mBackButton = findViewById(R.id.backButton);
        mBackButton.setOnClickListener(onBackClickListener);

        mSettingsButton = findViewById(R.id.settingsButton);
        mSettingsButton.setOnClickListener(onSettingsClickListener);
        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.addOnBackStackChangedListener(onBackStackChangedListener);

        mSoundPlayer = new SoundPlayer(this);
    }

    public void setGameViewModel(GameViewModel viewModel) {
        mGameViewModel = viewModel;

        //Set up buttons
        initButtons();
        final Observer<List<String>> buttonLabelsObserver = new Observer<List<String>>() {
            @Override
            public void onChanged(@Nullable List<String> strings) {
                setButtonLabels(strings);
            }
        };
        mGameViewModel.getButtonLabels().observe(this, buttonLabelsObserver);

        mLongTouchHandler = new LongTouchHandler(this, mGameViewModel);

        mSudokuGridView.setOnTouchListener(onSudokuGridTouchListener);
        mSudokuGridView.setCellLabels(this, mGameViewModel.getCellLabels());

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    //When sudoku grid is touched
    private SudokuGridView.OnTouchListener onSudokuGridTouchListener = new SudokuGridView.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int eventAction = event.getAction();
            //Through looking at every action case, we can move the highlight to where our finger moves to
            switch (eventAction) {
                case MotionEvent.ACTION_UP:
                    mLongTouchHandler.cancelGridLongClick();
                    break;
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    //If touch in the bounds of the grid
                    if (mSudokuGridView.getGridBounds().contains(x, y)) {
                        //Figure out which cell was touched
                        int cellNum = mSudokuGridView.getCellNumberFromCoordinates(x, y);
                        cellTouched = cellNum;

                        //Handle a potential long click if it is a locked cell
                        if (mGameViewModel.isLockedCell(cellNum)) {mLongTouchHandler.handleGridLongClick(cellTouched);}

                        //Check if the cell has been held down or not (If the last highlighted cell is the current cell)
                        if (mSudokuGridView.getHighlightedCell() >= 0 && event.getAction() == MotionEvent.ACTION_MOVE){
                            if (cellTouched != mSudokuGridView.getHighlightedCell()) {
                                mLongTouchHandler.cancelGridLongClick();
                            }
                        }

                        //Clear previous highlighted cell
                        mSudokuGridView.clearHighlightedCell();

                        //Set highlight on the currently touched cell
                        mSudokuGridView.setHighlightedCell(cellNum);

                        //Force redraw view
                        mSudokuGridView.invalidate();
                        mSudokuGridView.performClick();

                    }
            }
            return true;
        }
    };

    private int getButtonValue(Button button){
        //Loop through all our possible buttons to see which button is clicked
        //Set buttonValue to the corresponding button
        int buttonValue = 0;
        for (int buttonIndex = 0; buttonIndex < mInputButtons.length; buttonIndex++) {
            if (button == mInputButtons[buttonIndex]){
                buttonValue = buttonIndex + 1;
            }
        }

        return buttonValue;
    }

    private View.OnClickListener onButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button button = (Button) v;
            int buttonValue = getButtonValue(button);

            int cellNumber = mSudokuGridView.getHighlightedCell();

            //No cell is highlighted or a locked cell is highlighted
            if (cellNumber == -1 || mGameViewModel.isLockedCell(cellNumber)){
                mSudokuGridView.clearHighlightedCell();
                mSoundPlayer.playEmptyButtonSound();
            }

            else {
                if (mGameViewModel.isCorrectValue(cellNumber, buttonValue) || !mGameViewModel.isHintsEnabled()) {
                    //Correct number is placed in cell
                    mSudokuGridView.clearHighlightedCell();
                    mSudokuGridView.clearIncorrectCell(cellNumber);
                    mSoundPlayer.playPlaceCellSound();
                } else {
                    //Incorrect value has been placed in cell
                    mSudokuGridView.addIncorrectCell(cellNumber);
                    mSoundPlayer.playWrongSound();
                }

                mGameViewModel.setCellValue(cellNumber, buttonValue);
            }
            mSudokuGridView.invalidate();
        }
    };

    public void onCheckAnswerPressed(View v) {
        if (mGameViewModel == null) return;

        //Check if cell is selected
        //If a cell is selected, check if that cell is correct
        int highlightedCell = mSudokuGridView.getHighlightedCell();
        if (highlightedCell != -1)
        {
            //Cell is a locked cell
            if (mGameViewModel.isLockedCell(highlightedCell)){
                mSudokuGridView.clearHighlightedCell();
                mSoundPlayer.playEmptyButtonSound();
            }

            //Cell is right
            else if (
                    mGameViewModel.isCellCorrect(highlightedCell)
                    && !mGameViewModel.isLockedCell(highlightedCell)
            ){
                mSudokuGridView.clearHighlightedCell();
                mSudokuGridView.invalidate();
                mSoundPlayer.playCorrectSound();
                Toast.makeText(getBaseContext(),
                        "The selected cell is correct!",
                        Toast.LENGTH_LONG).show();
            }

            //Cell is wrong
            else {
                mSudokuGridView.addIncorrectCell(highlightedCell);
                mSoundPlayer.playWrongSound();

            }
            mSudokuGridView.invalidate();
            return;
        }

        //Checks if the answers are right and displays the first wrong cell (if any)
        ArrayList<Integer> incorrectCells= mGameViewModel.getIncorrectCells();
        //Clear highlights / what cell is selected for input
        mSudokuGridView.clearHighlightedCell();

        //Case where answer is correct
        if (incorrectCells.size() == 0) {
            mSoundPlayer.playCorrectSound();
            Toast.makeText(getBaseContext(),
                    "Congratulations, You've Won!",
                    Toast.LENGTH_LONG).show();
        }

        //Case where answer is incorrect
        else {
            mSudokuGridView.setIncorrectCells(incorrectCells);
            mSoundPlayer.playWrongSound();
        }

        //Redraw grid
        mSudokuGridView.invalidate();
    }

    public void onClearCellPressed(View v) {
        if (mGameViewModel == null) return;

        int cellNumber = mSudokuGridView.getHighlightedCell();

        if (cellNumber == -1){
            //No cell is highlighted
            mSoundPlayer.playEmptyButtonSound();
        } else {
            mGameViewModel.setCellValue(cellNumber, 0);
            mSudokuGridView.clearHighlightedCell();
            mSudokuGridView.clearIncorrectCell(cellNumber);
            mSoundPlayer.playClearCellSound();
            mSudokuGridView.invalidate();
        }
    }

    /**
     * Sets up the input buttons
     */
    private void initButtons() {
        int boardLength = mGameViewModel.getBoardLength();
        int rowSize = (int) Math.floor( Math.sqrt(boardLength) );
        int columnSize = (int) Math.ceil( Math.sqrt(boardLength) );

        //Get the parent layout everything resides in
        LinearLayout parent = findViewById(R.id.buttonLayout);

        //Initialize linear layouts for each button
        mLinearLayouts = new LinearLayout[columnSize];

        //Initialize layout parameters
        LinearLayout.LayoutParams  myParameters=
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        //Initializing button array
        mInputButtons = new Button[boardLength];


        for(int i = 0; i < columnSize; i++) {
            //Initializes each sub linear layout
            mLinearLayouts[i] = new LinearLayout(this);
            mLinearLayouts[i].setLayoutParams( myParameters );
            mLinearLayouts[i].setOrientation(LinearLayout.HORIZONTAL);

            for (int j = 0; j < rowSize; j++) {
                int buttonIndex = i*rowSize + j;
                //Initializes the buttons for each linear layout row

                //Sets the button array at index to have id button + the current index number
                //One is added because the number 0 is skipped
                mInputButtons[buttonIndex] = new Button(this);
                mInputButtons[buttonIndex].setId(getResources().getIdentifier("button" + (buttonIndex + 1), "id",
                        this.getPackageName()));
                myParameters.weight =1;
                mInputButtons[buttonIndex].setLayoutParams( myParameters );

                //Background
                mInputButtons[buttonIndex].setBackground(getResources().getDrawable( R.drawable.red_button ));

                //Text Color
                mInputButtons[buttonIndex].setTextColor(getResources().getColor( R.color.colorWhite));

                //Links the listeners to the button
                mInputButtons[buttonIndex].setOnClickListener(onButtonClickListener);
                mInputButtons[buttonIndex].setOnLongClickListener(onButtonLongClickListener);

                //Links the button to the linear layout
                mLinearLayouts[i].addView(mInputButtons[buttonIndex]);
            }
            //Links the linear layout to the overall view
            parent.addView(mLinearLayouts[i]);
        }
    }


    private void setButtonLabels(List<String> buttonLabels) {
        for(int i = 0; i < mInputButtons.length; i++) {
            mInputButtons[i].setText(buttonLabels.get(i));
        }
    }

    Button.OnLongClickListener onButtonLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            Button button = (Button) v;
            int buttonValue = getButtonValue(button);
            return mLongTouchHandler.handleButtonLongClick( buttonValue );
        }
    };

    private View.OnClickListener onBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSoundPlayer.playPlaceCellSound();
            finish();
        }
    };

    private View.OnClickListener onSettingsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SettingsFragment())
                    .addToBackStack(null)
                    .commit();
        }
    };

    //Play a sound every time the fragment is opened
    private FragmentManager.OnBackStackChangedListener onBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            mSoundPlayer.playPlaceCellSound();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLongTouchHandler.destroyLongTouchHandler();
    }
}
