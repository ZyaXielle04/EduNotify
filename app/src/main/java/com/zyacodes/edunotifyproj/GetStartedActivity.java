package com.zyacodes.edunotifyproj; // change to your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class GetStartedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        // Find the button
        Button getStartedButton = findViewById(R.id.buttonGetStarted);

        // Set click listener
        getStartedButton.setOnClickListener(v -> {
            // Redirect to LoginActivity
            Intent intent = new Intent(GetStartedActivity.this, LoginActivity.class);
            startActivity(intent);

            // Optional: close GetStartedActivity so it won’t return when back is pressed
            finish();
        });
    }
}
