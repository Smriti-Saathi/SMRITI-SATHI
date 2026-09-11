package com.smritisathi.ui.games;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.smritisathi.R;

/**
 * GamesHubActivity — Center to select and launch any of the 4 cognitive games:
 * 1. Memory Match
 * 2. Object Recall
 * 3. Pattern Recognition
 * 4. Simon Says
 */
public class GamesHubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games_hub);

        MaterialButton btnBack = findViewById(R.id.btnBackFromHub);
        MaterialButton btnMemoryMatch = findViewById(R.id.btnLaunchMemoryMatch);
        MaterialButton btnObjectRecall = findViewById(R.id.btnLaunchObjectRecall);
        MaterialButton btnPatternRecognition = findViewById(R.id.btnLaunchPatternRecognition);
        MaterialButton btnSimonSays = findViewById(R.id.btnLaunchSimonSays);

        btnBack.setOnClickListener(v -> finish());

        btnMemoryMatch.setOnClickListener(v -> {
            Intent intent = new Intent(this, MemoryMatchActivity.class);
            startActivity(intent);
        });

        btnObjectRecall.setOnClickListener(v -> {
            Intent intent = new Intent(this, ObjectRecallActivity.class);
            startActivity(intent);
        });

        btnPatternRecognition.setOnClickListener(v -> {
            Intent intent = new Intent(this, PatternRecognitionActivity.class);
            startActivity(intent);
        });

        btnSimonSays.setOnClickListener(v -> {
            Intent intent = new Intent(this, SimonSaysActivity.class);
            startActivity(intent);
        });
    }
}
