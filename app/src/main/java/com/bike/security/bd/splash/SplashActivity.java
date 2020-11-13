package com.bike.security.bd.splash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.bike.security.bd.main.MainActivity;
import com.bike.security.bd.R;
import com.bike.security.bd.databinding.ActivitySplashBinding;
import com.squareup.picasso.Picasso;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        setUpUi();
        initAnimation();

        new Handler(getMainLooper()).postDelayed(this::gotoNextActivity,2500);


    }


    private void setUpUi() {
        Picasso.get().load(R.drawable.ic_launcher).error(R.drawable.ic_launcher).into(binding.splashLogo);
    }

    private void initAnimation() {
        Animation blinkAnimation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.blink_animation);
        binding.welcomeTextView.startAnimation(blinkAnimation);
    }

    private void gotoNextActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }



}