package com.smritisathi.ui.games;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.smritisathi.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * PatternRecognitionActivity — show 3-image sequence, 3 answer options, 5 rounds,
 * tracks accuracy and average reaction time.
 */
public class PatternRecognitionActivity extends BaseGameActivity {

    private static final int TOTAL_ROUNDS = 5;

    private TextView tvRoundInfo;
    private ImageView ivItem1;
    private ImageView ivItem2;
    private ImageView ivItem3;
    private ImageButton[] btnOptions = new ImageButton[3];
    private TextView tvFeedback;
    private MaterialButton btnExit;

    private int currentRound = 0;
    private int correctAnswers = 0;
    private int mistakes = 0;
    private long totalReactionTimeMs = 0;
    private long roundStartTimeMs = 0;
    private boolean isRoundActive = false;

    private int currentCorrectAnswerResId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_recognition);

        initViews();
        setupListeners();
        startRound(0);
    }

    private void initViews() {
        tvRoundInfo = findViewById(R.id.tvPatternRoundInfo);
        ivItem1 = findViewById(R.id.ivPatternItem1);
        ivItem2 = findViewById(R.id.ivPatternItem2);
        ivItem3 = findViewById(R.id.ivPatternItem3);
        tvFeedback = findViewById(R.id.tvPatternFeedback);
        btnExit = findViewById(R.id.btnExitPatternGame);

        btnOptions[0] = findViewById(R.id.btnOption0);
        btnOptions[1] = findViewById(R.id.btnOption1);
        btnOptions[2] = findViewById(R.id.btnOption2);

        for (int i = 0; i < 3; i++) {
            final int optionIndex = i;
            btnOptions[i].setOnClickListener(v -> onOptionSelected(optionIndex));
        }
    }

    private void setupListeners() {
        btnExit.setOnClickListener(v -> showExitConfirmationDialog());
    }

    private void startRound(int roundIndex) {
        currentRound = roundIndex;
        tvRoundInfo.setText("Round " + (currentRound + 1) + " of " + TOTAL_ROUNDS);
        tvFeedback.setText("");
        isRoundActive = true;

        // Distinct icon sets for each round
        int[][] roundPairs = {
                { R.drawable.ic_card_lotus, R.drawable.ic_card_diya, R.drawable.ic_card_peacock },
                { R.drawable.ic_card_sun, R.drawable.ic_card_leaf, R.drawable.ic_card_star },
                { R.drawable.ic_card_elephant, R.drawable.ic_card_mango, R.drawable.ic_card_bell },
                { R.drawable.ic_card_peacock, R.drawable.ic_card_lotus, R.drawable.ic_card_heart },
                { R.drawable.ic_card_diya, R.drawable.ic_card_sun, R.drawable.ic_card_mango }
        };

        int itemA = roundPairs[currentRound][0];
        int itemB = roundPairs[currentRound][1];
        int distractor = roundPairs[currentRound][2];

        // Alternating Pattern: A -> B -> A -> [ ? = B ]
        ivItem1.setImageResource(itemA);
        ivItem2.setImageResource(itemB);
        ivItem3.setImageResource(itemA);
        currentCorrectAnswerResId = itemB;

        // Prepare 3 randomized options
        List<Integer> options = new ArrayList<>();
        options.add(itemB); // Correct answer
        options.add(itemA); // Distractor 1
        options.add(distractor); // Distractor 2
        Collections.shuffle(options);

        for (int i = 0; i < 3; i++) {
            int icon = options.get(i);
            btnOptions[i].setTag(icon);
            btnOptions[i].setImageResource(icon);
            btnOptions[i].setBackgroundResource(R.drawable.bg_game_card_front);
            btnOptions[i].setEnabled(true);
        }

        roundStartTimeMs = System.currentTimeMillis();
    }

    private void onOptionSelected(int optionIndex) {
        if (!isRoundActive) return;
        isRoundActive = false;

        long reactionTime = System.currentTimeMillis() - roundStartTimeMs;
        totalReactionTimeMs += reactionTime;

        int selectedIcon = (int) btnOptions[optionIndex].getTag();

        for (ImageButton btn : btnOptions) {
            btn.setEnabled(false);
        }

        if (selectedIcon == currentCorrectAnswerResId) {
            correctAnswers++;
            btnOptions[optionIndex].setBackgroundResource(R.drawable.bg_game_card_matched);
            tvFeedback.setText("Lovely! Correct pattern 🌸");
            tvFeedback.setTextColor(getResources().getColor(R.color.colorSuccess, null));
        } else {
            mistakes++;
            tvFeedback.setText("Good try! Moving forward 🌿");
            tvFeedback.setTextColor(getResources().getColor(R.color.textSecondary, null));
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (currentRound + 1 < TOTAL_ROUNDS) {
                startRound(currentRound + 1);
            } else {
                onAllRoundsCompleted();
            }
        }, 900);
    }

    private void onAllRoundsCompleted() {
        double accuracy = (correctAnswers / (double) TOTAL_ROUNDS) * 100.0;
        long avgReactionTimeMs = totalReactionTimeMs / TOTAL_ROUNDS;
        int score = (int) (accuracy * 10);

        // Unified Firestore logger
        saveGameSession("PatternRecognition", "Normal", score, accuracy, avgReactionTimeMs, mistakes);

        // Encouraging overlay
        showGameCompleteOverlay(
                score,
                accuracy,
                "Average reaction time: " + String.format(Locale.getDefault(), "%.2f", avgReactionTimeMs / 1000.0) + "s"
        );
    }
}
