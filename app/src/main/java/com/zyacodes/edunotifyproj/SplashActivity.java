package com.zyacodes.edunotifyproj;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Find logo elements (TextView + ImageView)
        TextView title = findViewById(R.id.splash_title);
        ImageView bellIcon = findViewById(R.id.splash_icon);

        // Load fade-in animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.splash_fade_in);

        // Start animation
        if (title != null) title.startAnimation(fadeIn);
        if (bellIcon != null) bellIcon.startAnimation(fadeIn);

        // After delay, go to MainActivity with fade transition
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, GetStartedActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, SPLASH_DURATION);
    }
}
