package com.smritisathi.ui.games;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.smritisathi.R;
import com.smritisathi.data.FirestoreHelper;
import com.smritisathi.data.SessionManager;
import com.smritisathi.model.GameSession;

import java.util.Locale;
import java.util.Random;

/**
 * Shared abstract base class for all 4 cognitive games:
 * - MemoryMatchActivity
 * - ObjectRecallActivity
 * - PatternRecognitionActivity
 * - SimonSaysActivity
 *
 * Provides exit-confirmation dialog, encouraging end-of-game overlay,
 * and unified Firestore logging to 'gameSessions'.
 */
public abstract class BaseGameActivity extends AppCompatActivity {

    protected SessionManager sessionManager;
    protected FirestoreHelper firestoreHelper;
    protected long gameStartTimeMs;
    private final Random random = new Random();

    // Bank of positive, reassuring messages for elderly patients (never negative or punitive)
    private static final String[] ENCOURAGING_MESSAGES = {
            "Wonderful effort! Every game you play strengthens your memory! 🌟",
            "Brilliant job! You did wonderfully exercising your focus today! 🌸",
            "Great workout for your brain! Keep up this beautiful spirit! 🌺",
            "You should be proud of your effort! Thank you for playing today! ☀️",
            "Fantastic concentration! Caring for your mind is a precious step! 💛"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        firestoreHelper = FirestoreHelper.getInstance();
        gameStartTimeMs = System.currentTimeMillis();

        // Handle back button with gentle exit confirmation
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmationDialog();
            }
        });
    }

    /**
     * Calculates duration of current game in seconds.
     */
    protected int getDurationSeconds() {
        return (int) Math.max(1, (System.currentTimeMillis() - gameStartTimeMs) / 1000);
    }

    /**
     * Shows a gentle, reassuring exit confirmation dialog.
     */
    protected void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Take a Short Break?")
                .setMessage("Are you sure you want to stop this game? You can always come back and play anytime.")
                .setPositiveButton("Take a Break", (dialog, which) -> finish())
                .setNegativeButton("Keep Playing", null)
                .show();
    }

    /**
     * Writes game results to Firestore 'gameSessions' collection and saves locally.
     * Immediately triggers Section E Retrofit bridge to fetch last 10 sessions,
     * calculate score via Spring Boot, and write a new 'cognitiveAssessments' document.
     */
    protected void saveGameSession(String gameType, String difficulty, int score,
                                   double accuracy, long reactionTimeMs, int mistakes) {
        String patientId = sessionManager.getUserId();
        if (patientId == null) {
            patientId = "guest_patient";
        }

        int durationSeconds = getDurationSeconds();

        GameSession session = new GameSession(
                null,
                patientId,
                gameType,
                difficulty != null ? difficulty : "Normal",
                score,
                accuracy,
                reactionTimeMs,
                mistakes,
                durationSeconds,
                System.currentTimeMillis()
        );

        final String finalPatientId = patientId;
        firestoreHelper.getGameSessionsCollection()
                .add(session)
                .addOnSuccessListener(documentReference -> {
                    // Section E: Fetch patient's last 10 gameSessions & POST to scoring endpoint
                    fetchHistoryAndEvaluateCognitiveScore(finalPatientId);
                })
                .addOnFailureListener(e -> {
                    // Fail silently or locally logged without alarming the patient
                });
    }

    /**
     * Section E Retrofit Bridge:
     * 1. Fetches patient's last 10 gameSessions from Firestore.
     * 2. Formats payload and calls Spring Boot POST /api/score.
     * 3. Writes result into a new 'cognitiveAssessments' Firestore document.
     */
    private void fetchHistoryAndEvaluateCognitiveScore(String patientId) {
        firestoreHelper.getLastTenGameSessions(patientId)
                .addOnSuccessListener(querySnapshot -> {
                    java.util.List<com.smritisathi.network.dto.GameSessionRequest> requests = new java.util.ArrayList<>();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            GameSession s = doc.toObject(GameSession.class);
                            if (s != null) {
                                requests.add(new com.smritisathi.network.dto.GameSessionRequest(
                                        s.getAccuracy(),
                                        s.getReactionTimeMs(),
                                        s.getGameType() != null ? s.getGameType() : "MemoryMatch"
                                ));
                            }
                        }
                    }

                    if (requests.isEmpty()) {
                        requests.add(new com.smritisathi.network.dto.GameSessionRequest(80.0, 1000, "MemoryMatch"));
                    }

                    // Call Spring Boot Scoring Service via Retrofit
                    com.smritisathi.network.ApiClient.getInstance()
                            .getScoringApiService()
                            .calculateScore(requests)
                            .enqueue(new retrofit2.Callback<com.smritisathi.network.dto.ScoreResponse>() {
                                @Override
                                public void onResponse(retrofit2.Call<com.smritisathi.network.dto.ScoreResponse> call,
                                                       retrofit2.Response<com.smritisathi.network.dto.ScoreResponse> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        com.smritisathi.network.dto.ScoreResponse scoreResp = response.body();
                                        saveCognitiveAssessmentDoc(patientId, scoreResp);
                                    } else {
                                        saveLocalFallbackAssessment(patientId, requests);
                                    }
                                }

                                @Override
                                public void onFailure(retrofit2.Call<com.smritisathi.network.dto.ScoreResponse> call, Throwable t) {
                                    // Fallback if backend service is currently offline / starting up
                                    saveLocalFallbackAssessment(patientId, requests);
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    // Firestore fetch error
                });
    }

    private void saveCognitiveAssessmentDoc(String patientId, com.smritisathi.network.dto.ScoreResponse scoreResp) {
        com.smritisathi.model.CognitiveAssessment assessment = new com.smritisathi.model.CognitiveAssessment(
                null,
                patientId,
                scoreResp.getOverallScore(),
                scoreResp.getOverallScore(),
                scoreResp.getOverallScore(),
                scoreResp.getOverallScore(),
                scoreResp.getRecommendedDifficulty(),
                System.currentTimeMillis()
        );

        firestoreHelper.saveCognitiveAssessment(assessment);
    }

    /**
     * Resilient offline fallback implementing the exact same heuristic formula
     * in case the backend server is temporarily unreachable.
     */
    private void saveLocalFallbackAssessment(String patientId, java.util.List<com.smritisathi.network.dto.GameSessionRequest> requests) {
        int n = requests.size();
        double sumAcc = 0.0;
        long sumRt = 0;
        for (com.smritisathi.network.dto.GameSessionRequest r : requests) {
            sumAcc += r.getAccuracy();
            sumRt += r.getReactionTimeMs();
        }
        double avgAcc = sumAcc / n;
        double avgRt = (double) sumRt / n;
        double normRt = Math.max(0.0, 100.0 - Math.min(avgRt / 50.0, 100.0));

        double varSum = 0.0;
        for (com.smritisathi.network.dto.GameSessionRequest r : requests) {
            double diff = r.getAccuracy() - avgAcc;
            varSum += diff * diff;
        }
        double stdDev = Math.sqrt(varSum / n);
        double consistency = Math.max(0.0, Math.min(100.0, 100.0 - stdDev));

        double overall = (0.40 * avgAcc) + (0.25 * normRt) + (0.20 * consistency) + (0.15 * 100.0);
        double score = Math.round(overall * 10.0) / 10.0;
        String tier = score > 75.0 ? "difficult" : (score < 45.0 ? "easy" : "moderate");

        com.smritisathi.network.dto.ScoreResponse fallbackResp =
                new com.smritisathi.network.dto.ScoreResponse(score, tier, "Locally computed assessment (offline fallback)");
        saveCognitiveAssessmentDoc(patientId, fallbackResp);
    }

    /**
     * Displays the encouraging end-of-game overlay dialog with random uplifting feedback.
     */
    protected void showGameCompleteOverlay(int score, double accuracy, String extraInfo) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_complete, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvOverlayTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvEncouragingMessage);
        TextView tvScore = dialogView.findViewById(R.id.tvOverlayScore);
        TextView tvAccuracy = dialogView.findViewById(R.id.tvOverlayAccuracy);
        TextView tvExtra = dialogView.findViewById(R.id.tvOverlayExtra);
        MaterialButton btnPlayAgain = dialogView.findViewById(R.id.btnOverlayPlayAgain);
        MaterialButton btnReturn = dialogView.findViewById(R.id.btnOverlayReturn);

        // Pick random encouraging message
        String message = ENCOURAGING_MESSAGES[random.nextInt(ENCOURAGING_MESSAGES.length)];
        tvMessage.setText(message);
        tvScore.setText(String.valueOf(score));
        tvAccuracy.setText(String.format(Locale.getDefault(), "%.0f%%", Math.max(0, Math.min(100, accuracy))));

        if (extraInfo != null && !extraInfo.isEmpty()) {
            tvExtra.setText(extraInfo);
            tvExtra.setVisibility(View.VISIBLE);
        } else {
            tvExtra.setText("Duration: " + getDurationSeconds() + " seconds");
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnPlayAgain.setOnClickListener(v -> {
            dialog.dismiss();
            recreate();
        });

        btnReturn.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }
}
