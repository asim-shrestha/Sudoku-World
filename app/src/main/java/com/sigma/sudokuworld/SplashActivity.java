package com.sigma.sudokuworld;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends BaseActivity {
    private static final int START_ANIM_DELAY = 500;
    private int exit_delay;

    private ConstraintLayout animationLayout;
    private Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //If we click the layout we can skip the animation and cancel the timer
        animationLayout = findViewById(R.id.animationLayout);
        animationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                startActivity(new Intent(SplashActivity.this, MenuActivity.class));
                finish();
            }
        });

        ImageView imageView = findViewById(R.id.sigmaAnimation);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            final AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) imageView.getDrawable();

            //Timer starts animation on launch and exits splash screen
            TimerTask animationStartTask = new TimerTask() {
                @Override
                public void run() {
                    animatedVectorDrawable.start();
                }
            };

            timer.schedule(animationStartTask, START_ANIM_DELAY);
            exit_delay = 4000;
        } else {
            exit_delay = 1000;
        }

        final TimerTask exitSplashTask = new TimerTask() {
            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, MenuActivity.class);
                startActivity(i);
                finish();
            }
        };

        timer.schedule(exitSplashTask, exit_delay);
    }


    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
