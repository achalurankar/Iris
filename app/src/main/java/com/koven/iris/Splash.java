package com.koven.iris;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class Splash extends AppCompatActivity {

    private long DURATION = 1000;
    RelativeLayout appLogoLayout;
    ImageView appNameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setAnimations();
    }

    private void setAnimations() {
        //load animations
        Animation appLogoAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.app_logo_anim); // logo scaling animation
        Animation appNameAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.app_name_text_anim); // app name alpha animation

        //set anim duration
        appLogoAnimation.setDuration(DURATION);
        appNameAnimation.setDuration(DURATION);

        //find and hide layout at the start
        appLogoLayout = findViewById(R.id.splash_app_logo_bg);
        appNameLayout = findViewById(R.id.app_name_text);
        appLogoLayout.setVisibility(View.INVISIBLE);
        appNameLayout.setVisibility(View.INVISIBLE);

        //apply animation after 400 millis
        new Handler().postDelayed(() -> {
            //make layouts visible
            appLogoLayout.setVisibility(View.VISIBLE);
            appNameLayout.setVisibility(View.VISIBLE);

            //start animations now for @DURATION
            appLogoLayout.setAnimation(appLogoAnimation);
            appNameLayout.setAnimation(appNameAnimation);

            new Handler().postDelayed(() -> {
                startActivity(new Intent(Splash.this, UsersActivity.class));
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
            }, DURATION + 500);
        }, 400);
    }
}