package com.smritisathi.ui.games;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.smritisathi.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * SimonSaysActivity — 4 large colored buttons in a 2x2 grid.
 * App plays a growing sequence highlighting buttons with sound & vibration.
 * Patient repeats sequence. Sequence grows by 1 each successful round.
 * Ends on first mistake.
 * Logs: highest sequence length reached, total correct taps, total mistakes, avg reaction time per tap.
 * Tests working memory and sequencing.
 */
public class SimonSaysActivity extends BaseGameActivity {

    private static final int GREEN = 0;
    private static final int RED = 1;
    private static final int YELLOW = 2;
    private static final int BLUE = 3;

    private View[] colorViews = new View[4];
    private TextView tvStatus;
    private TextView tvSequenceInfo;
    private MaterialButton btnStart;
    private MaterialButton btnExit;

    private final List<Integer> sequence = new ArrayList<>();
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private ToneGenerator toneGenerator;
    private Vibrator vibrator;

    private boolean isPlayingSequence = false;
    private boolean isPatientTurn = false;
    private int patientStepIndex = 0;

    // Metrics tracking
    private int highestSequenceLengthReached = 0;
    private int totalCorrectTaps = 0;
    private int totalMistakes = 0;
    private long totalReactionTimeMs = 0;
    private long lastTapTimestampMs = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simon_says);

        initHardware();
        initViews();
        setupListeners();
    }

    private void initHardware() {
        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 75);
        } catch (Exception ignored) {
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vm = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vm != null) {
                    vibrator = vm.getDefaultVibrator();
                }
            } else {
                vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            }
        } catch (Exception ignored) {
        }
    }

    private void initViews() {
        colorViews[GREEN] = findViewById(R.id.btnSimonGreen);
        colorViews[RED] = findViewById(R.id.btnSimonRed);
        colorViews[YELLOW] = findViewById(R.id.btnSimonYellow);
        colorViews[BLUE] = findViewById(R.id.btnSimonBlue);

        tvStatus = findViewById(R.id.tvSimonStatus);
        tvSequenceInfo = findViewById(R.id.tvSimonSequenceLength);
        btnStart = findViewById(R.id.btnStartSimon);
        btnExit = findViewById(R.id.btnExitSimonGame);
    }

    private void setupListeners() {
        btnStart.setOnClickListener(v -> startNewGame());

        colorViews[GREEN].setOnClickListener(v -> onPatientTapped(GREEN));
        colorViews[RED].setOnClickListener(v -> onPatientTapped(RED));
        colorViews[YELLOW].setOnClickListener(v -> onPatientTapped(YELLOW));
        colorViews[BLUE].setOnClickListener(v -> onPatientTapped(BLUE));

        btnExit.setOnClickListener(v -> showExitConfirmationDialog());
    }

    private void startNewGame() {
        btnStart.setVisibility(View.GONE);
        sequence.clear();
        highestSequenceLengthReached = 0;
        totalCorrectTaps = 0;
        totalMistakes = 0;
        totalReactionTimeMs = 0;

        advanceSequenceAndPlay();
    }

    private void advanceSequenceAndPlay() {
        sequence.add(random.nextInt(4));
        highestSequenceLengthReached = Math.max(highestSequenceLengthReached, sequence.size());

        tvSequenceInfo.setText("Sequence Length: " + sequence.size() + " • Correct Taps: " + totalCorrectTaps);
        tvStatus.setText("Watch the pattern... 🎵");
        isPatientTurn = false;
        isPlayingSequence = true;

        handler.postDelayed(this::playSequenceStepByStep, 800);
    }

    private void playSequenceStepByStep() {
        long delay = 0;
        final int highlightDuration = 450;
        final int pauseDuration = 200;

        for (int i = 0; i < sequence.size(); i++) {
            final int colorIndex = sequence.get(i);
            final boolean isLast = (i == sequence.size() - 1);

            handler.postDelayed(() -> highlightButton(colorIndex), delay);

            delay += highlightDuration;
            handler.postDelayed(() -> unhighlightButton(colorIndex), delay);

            delay += pauseDuration;

            if (isLast) {
                handler.postDelayed(() -> {
                    isPlayingSequence = false;
                    isPatientTurn = true;
                    patientStepIndex = 0;
                    lastTapTimestampMs = System.currentTimeMillis();
                    tvStatus.setText("Your turn! Repeat the colors in order 🌸");
                }, delay);
            }
        }
    }

    private void onPatientTapped(int colorIndex) {
        if (!isPatientTurn || isPlayingSequence) return;

        // Visual & audio feedback for tap
        highlightButton(colorIndex);
        handler.postDelayed(() -> unhighlightButton(colorIndex), 200);

        long now = System.currentTimeMillis();
        long tapReactionTime = now - lastTapTimestampMs;
        totalReactionTimeMs += tapReactionTime;
        lastTapTimestampMs = now;

        // Validate sequence
        if (colorIndex == sequence.get(patientStepIndex)) {
            totalCorrectTaps++;
            patientStepIndex++;

            if (patientStepIndex == sequence.size()) {
                // Successfully completed round!
                isPatientTurn = false;
                tvStatus.setText("Great memory! Next pattern coming up... 🌟");
                handler.postDelayed(this::advanceSequenceAndPlay, 1000);
            }
        } else {
            // First mistake ends game gently
            isPatientTurn = false;
            totalMistakes = 1;
            tvStatus.setText("Good try! Let's see your results 🌸");

            handler.postDelayed(this::onGameOver, 600);
        }
    }

    private void highlightButton(int colorIndex) {
        switch (colorIndex) {
            case GREEN:
                colorViews[GREEN].setBackgroundResource(R.drawable.bg_simon_green_lit);
                playTone(ToneGenerator.TONE_DTMF_1);
                break;
            case RED:
                colorViews[RED].setBackgroundResource(R.drawable.bg_simon_red_lit);
                playTone(ToneGenerator.TONE_DTMF_2);
                break;
            case YELLOW:
                colorViews[YELLOW].setBackgroundResource(R.drawable.bg_simon_yellow_lit);
                playTone(ToneGenerator.TONE_DTMF_3);
                break;
            case BLUE:
                colorViews[BLUE].setBackgroundResource(R.drawable.bg_simon_blue_lit);
                playTone(ToneGenerator.TONE_DTMF_4);
                break;
        }
        triggerVibration();
    }

    private void unhighlightButton(int colorIndex) {
        switch (colorIndex) {
            case GREEN:
                colorViews[GREEN].setBackgroundResource(R.drawable.bg_simon_green);
                break;
            case RED:
                colorViews[RED].setBackgroundResource(R.drawable.bg_simon_red);
                break;
            case YELLOW:
                colorViews[YELLOW].setBackgroundResource(R.drawable.bg_simon_yellow);
                break;
            case BLUE:
                colorViews[BLUE].setBackgroundResource(R.drawable.bg_simon_blue);
                break;
        }
    }

    private void playTone(int toneType) {
        if (toneGenerator != null) {
            try {
                toneGenerator.startTone(toneType, 180);
            } catch (Exception ignored) {
            }
        }
    }

    private void triggerVibration() {
        if (vibrator != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(80);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void onGameOver() {
        long avgReactionTimeMs = totalCorrectTaps > 0 ? (totalReactionTimeMs / totalCorrectTaps) : 0;
        double accuracy = totalCorrectTaps > 0 ? ((double) totalCorrectTaps / (totalCorrectTaps + 1)) * 100.0 : 0.0;
        int score = (highestSequenceLengthReached * 25) + (totalCorrectTaps * 5);

        // Unified Firestore logger
        saveGameSession("SimonSays", "Normal", score, accuracy, avgReactionTimeMs, totalMistakes);

        // Encouraging overlay
        showGameCompleteOverlay(
                score,
                accuracy,
                "Highest Sequence: " + highestSequenceLengthReached + " steps • Correct Taps: " + totalCorrectTaps
        );
    }

    @Override
    protected void onDestroy() {
        if (toneGenerator != null) {
            try {
                toneGenerator.release();
            } catch (Exception ignored) {
            }
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
