package com.sigma.sudokuworld;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.sigma.sudokuworld.Persistence.KeyConstants;
import com.sigma.sudokuworld.Persistence.PersistenceService;
import com.sigma.sudokuworld.Audio.SoundPlayer;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MENU";
    private static final int REQUEST_CODE = 1;

    private SoundPlayer mSoundPlayer;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //HIDE STATUS BAR
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mSoundPlayer = new SoundPlayer(this);

        ImageView imageView = findViewById(R.id.menuAVD);
        AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) imageView.getDrawable();
        animatedVectorDrawable.start();
    }

    /**
     * Called when play button is pressed. (Action defined in xml onClick)
     */
    public void onPlayPressed(View v) {
        //Settings bundle
        Bundle settings = PersistenceService.loadSettingsData(getBaseContext());
        Intent intent = new Intent(getBaseContext(), SudokuActivity.class);
        intent.putExtras(settings);
        intent.putExtra(KeyConstants.CONTINUE_KEY, false);

        mSoundPlayer.playPlaceCellSound();
        startActivity(intent);
    }

    /**
     * Called when Continue button is pressed. (Action defined in xml onClick)
     */
    public void onContinuePressed(View v) {
        try {
            Intent intent = new Intent(getBaseContext(), SudokuActivity.class);
            intent.putExtras(PersistenceService.loadGameData(getBaseContext()));
            intent.putExtra(KeyConstants.CONTINUE_KEY, true);
            mSoundPlayer.playPlaceCellSound();
            Log.d(TAG, "onContinueClick: starting game with data");
            startActivity(intent);
        } catch (NullPointerException e) {
            mSoundPlayer.playPlaceCellSound();
            Log.d(TAG, "onContinueClick: no game data");
        }
    }

    /**
     * Called when Settings button is pressed. (Action defined in xml onClick)
     */
    public void onSettingsPressed(View v) {
        Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
        mSoundPlayer.playPlaceCellSound();
        startActivityForResult(intent, REQUEST_CODE);
    }
}

