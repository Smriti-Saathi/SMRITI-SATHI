package com.smritisathi.ui.games;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.smritisathi.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ObjectRecallActivity — show 5 images for 5 seconds (Handler / CountDownTimer),
 * hide, show 3x3 grid (5 correct + 4 distractors) for selection,
 * accuracy calculated from correct minus false positives.
 */
public class ObjectRecallActivity extends BaseGameActivity {

    private static final int MEMORIZE_SECONDS = 5;
    private static final int TARGET_COUNT = 5;
    private static final int GRID_COUNT = 9;

    private LinearLayout layoutMemorize;
    private TextView tvCountdown;
    private ImageView[] ivTargets = new ImageView[TARGET_COUNT];

    private LinearLayout layoutRecall;
    private ImageButton[] btnRecallGrid = new ImageButton[GRID_COUNT];
    private MaterialButton btnSubmitRecall;
    private MaterialButton btnExit;

    private final Set<Integer> targetItemSet = new HashSet<>();
    private final List<Integer> gridItems = new ArrayList<>();
    private final boolean[] isCardSelected = new boolean[GRID_COUNT];

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_recall);

        initViews();
        setupGameData();
        startMemorizationTimer();
        setupListeners();
    }

    private void initViews() {
        layoutMemorize = findViewById(R.id.layoutMemorizationPhase);
        tvCountdown = findViewById(R.id.tvCountdownTimer);
        btnExit = findViewById(R.id.btnExitRecallGame);

        ivTargets[0] = findViewById(R.id.ivTarget0);
        ivTargets[1] = findViewById(R.id.ivTarget1);
        ivTargets[2] = findViewById(R.id.ivTarget2);
        ivTargets[3] = findViewById(R.id.ivTarget3);
        ivTargets[4] = findViewById(R.id.ivTarget4);

        layoutRecall = findViewById(R.id.layoutRecallSelectionPhase);
        btnSubmitRecall = findViewById(R.id.btnSubmitRecall);

        int[] gridIds = {
                R.id.recallCard0, R.id.recallCard1, R.id.recallCard2,
                R.id.recallCard3, R.id.recallCard4, R.id.recallCard5,
                R.id.recallCard6, R.id.recallCard7, R.id.recallCard8
        };

        for (int i = 0; i < GRID_COUNT; i++) {
            btnRecallGrid[i] = findViewById(gridIds[i]);
            final int index = i;
            btnRecallGrid[i].setOnClickListener(v -> toggleCardSelection(index));
        }
    }

    private void setupGameData() {
        int[] allIcons = {
                R.drawable.ic_card_lotus,
                R.drawable.ic_card_diya,
                R.drawable.ic_card_peacock,
                R.drawable.ic_card_elephant,
                R.drawable.ic_card_mango,
                R.drawable.ic_card_sun,
                R.drawable.ic_card_leaf,
                R.drawable.ic_card_star,
                R.drawable.ic_card_bell,
                R.drawable.ic_card_heart
        };

        List<Integer> pool = new ArrayList<>();
        for (int icon : allIcons) {
            pool.add(icon);
        }
        Collections.shuffle(pool);

        // First 5 are targets to memorize
        targetItemSet.clear();
        for (int i = 0; i < TARGET_COUNT; i++) {
            int icon = pool.get(i);
            targetItemSet.add(icon);
            ivTargets[i].setImageResource(icon);
            gridItems.add(icon);
        }

        // Next 4 are distractors
        for (int i = TARGET_COUNT; i < TARGET_COUNT + 4; i++) {
            gridItems.add(pool.get(i));
        }

        // Shuffle the 9 items for 3x3 selection
        Collections.shuffle(gridItems);

        for (int i = 0; i < GRID_COUNT; i++) {
            isCardSelected[i] = false;
            btnRecallGrid[i].setImageResource(gridItems.get(i));
            btnRecallGrid[i].setBackgroundResource(R.drawable.bg_game_card_front);
        }
    }

    private void startMemorizationTimer() {
        layoutMemorize.setVisibility(View.VISIBLE);
        layoutRecall.setVisibility(View.GONE);

        countDownTimer = new CountDownTimer(MEMORIZE_SECONDS * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = (millisUntilFinished / 1000L) + 1;
                tvCountdown.setText("Memorize: " + seconds + " seconds");
            }

            @Override
            public void onFinish() {
                layoutMemorize.setVisibility(View.GONE);
                layoutRecall.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void toggleCardSelection(int index) {
        isCardSelected[index] = !isCardSelected[index];
        if (isCardSelected[index]) {
            btnRecallGrid[index].setBackgroundResource(R.drawable.bg_game_card_matched);
        } else {
            btnRecallGrid[index].setBackgroundResource(R.drawable.bg_game_card_front);
        }
    }

    private void setupListeners() {
        btnSubmitRecall.setOnClickListener(v -> evaluateRecall());

        btnExit.setOnClickListener(v -> showExitConfirmationDialog());
    }

    private void evaluateRecall() {
        int correctSelected = 0;
        int falsePositives = 0;

        for (int i = 0; i < GRID_COUNT; i++) {
            if (isCardSelected[i]) {
                int icon = gridItems.get(i);
                if (targetItemSet.contains(icon)) {
                    correctSelected++;
                } else {
                    falsePositives++;
                }
            }
        }

        int missedTargets = TARGET_COUNT - correctSelected;
        int totalMistakes = missedTargets + falsePositives;

        // accuracy from correct minus false positives
        double rawAccuracy = ((correctSelected - falsePositives) / (double) TARGET_COUNT) * 100.0;
        double accuracy = Math.max(0.0, Math.min(100.0, rawAccuracy));
        int score = (int) (accuracy * 10);

        // Log results to Firestore gameSessions
        saveGameSession("ObjectRecall", "Normal", score, accuracy, 0, totalMistakes);

        // Display encouraging overlay
        showGameCompleteOverlay(
                score,
                accuracy,
                "Found " + correctSelected + " of 5 objects (" + falsePositives + " distractors picked)"
        );
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}
