package com.smritisathi.ui.games;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.smritisathi.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MemoryMatchActivity — 4x3 grid of face-down ImageButtons.
 * Tap to reveal/match pairs, track attempts/mistakes/duration,
 * accuracy = (pairs/attempts) * 100.
 */
public class MemoryMatchActivity extends BaseGameActivity {

    private static final int TOTAL_PAIRS = 6;
    private static final int TOTAL_CARDS = 12;

    private final ImageButton[] cardButtons = new ImageButton[TOTAL_CARDS];
    private final int[] cardIconResIds = new int[TOTAL_CARDS];
    private final boolean[] matchedCards = new boolean[TOTAL_CARDS];

    private int firstCardIndex = -1;
    private boolean isProcessing = false;

    private int attempts = 0;
    private int mistakes = 0;
    private int pairsMatched = 0;

    private TextView tvStats;
    private MaterialButton btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_match);

        initViews();
        setupGameDeck();
        setupListeners();
    }

    private void initViews() {
        tvStats = findViewById(R.id.tvMatchStats);
        btnExit = findViewById(R.id.btnExitMatchGame);

        int[] buttonIds = {
                R.id.card0, R.id.card1, R.id.card2, R.id.card3,
                R.id.card4, R.id.card5, R.id.card6, R.id.card7,
                R.id.card8, R.id.card9, R.id.card10, R.id.card11
        };

        for (int i = 0; i < TOTAL_CARDS; i++) {
            cardButtons[i] = findViewById(buttonIds[i]);
            final int index = i;
            cardButtons[i].setOnClickListener(v -> onCardClicked(index));
        }

        updateStatsDisplay();
    }

    private void setupGameDeck() {
        int[] iconPairs = {
                R.drawable.ic_card_lotus,
                R.drawable.ic_card_diya,
                R.drawable.ic_card_peacock,
                R.drawable.ic_card_elephant,
                R.drawable.ic_card_mango,
                R.drawable.ic_card_sun
        };

        List<Integer> deck = new ArrayList<>();
        for (int icon : iconPairs) {
            deck.add(icon);
            deck.add(icon);
        }
        Collections.shuffle(deck);

        for (int i = 0; i < TOTAL_CARDS; i++) {
            cardIconResIds[i] = deck.get(i);
            matchedCards[i] = false;
            cardButtons[i].setBackgroundResource(R.drawable.bg_game_card_back);
            cardButtons[i].setImageResource(0);
            cardButtons[i].setEnabled(true);
        }
    }

    private void setupListeners() {
        btnExit.setOnClickListener(v -> showExitConfirmationDialog());
    }

    private void onCardClicked(int index) {
        if (isProcessing || matchedCards[index] || index == firstCardIndex) {
            return;
        }

        // Reveal card
        revealCard(index);

        if (firstCardIndex == -1) {
            // First card of pair tapped
            firstCardIndex = index;
        } else {
            // Second card tapped
            attempts++;
            isProcessing = true;
            int secondCardIndex = index;

            if (cardIconResIds[firstCardIndex] == cardIconResIds[secondCardIndex]) {
                // Pair matched!
                pairsMatched++;
                matchedCards[firstCardIndex] = true;
                matchedCards[secondCardIndex] = true;

                cardButtons[firstCardIndex].setBackgroundResource(R.drawable.bg_game_card_matched);
                cardButtons[secondCardIndex].setBackgroundResource(R.drawable.bg_game_card_matched);

                firstCardIndex = -1;
                isProcessing = false;
                updateStatsDisplay();

                if (pairsMatched == TOTAL_PAIRS) {
                    onGameCompleted();
                }
            } else {
                // Mismatch
                mistakes++;
                updateStatsDisplay();

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    hideCard(firstCardIndex);
                    hideCard(secondCardIndex);
                    firstCardIndex = -1;
                    isProcessing = false;
                }, 800);
            }
        }
    }

    private void revealCard(int index) {
        cardButtons[index].setBackgroundResource(R.drawable.bg_game_card_front);
        cardButtons[index].setImageResource(cardIconResIds[index]);
    }

    private void hideCard(int index) {
        if (!matchedCards[index]) {
            cardButtons[index].setBackgroundResource(R.drawable.bg_game_card_back);
            cardButtons[index].setImageResource(0);
        }
    }

    private void updateStatsDisplay() {
        tvStats.setText("Pairs Found: " + pairsMatched + " / " + TOTAL_PAIRS + " • Attempts: " + attempts);
    }

    private void onGameCompleted() {
        // accuracy = (pairs/attempts) * 100
        double accuracy = attempts > 0 ? (pairsMatched / (double) attempts) * 100.0 : 100.0;
        int durationSec = getDurationSeconds();
        int score = (int) (accuracy * 10 + Math.max(0, 150 - durationSec * 2));

        // Shared Firestore logger
        saveGameSession("MemoryMatch", "Normal", score, accuracy, 0, mistakes);

        // Encouraging overlay
        showGameCompleteOverlay(
                score,
                accuracy,
                "Matched all 6 pairs in " + attempts + " attempts (" + durationSec + "s)"
        );
    }
}
