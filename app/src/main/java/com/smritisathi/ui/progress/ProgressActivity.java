package com.smritisathi.ui.progress;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.smritisathi.R;
import com.smritisathi.data.FirestoreHelper;
import com.smritisathi.data.SessionManager;
import com.smritisathi.model.CognitiveAssessment;

/**
 * ProgressActivity — displays cognitive wellness progress for the patient.
 * Features 4 ProgressBars (Memory, Attention, Recognition, and Sequencing) with encouraging
 * verbal labels. Strictly adheres to memory-care psychology: NO raw numerical scores are shown to the patient.
 */
public class ProgressActivity extends AppCompatActivity {

    private ProgressBar pbMemory;
    private TextView tvMemoryLabel;

    private ProgressBar pbAttention;
    private TextView tvAttentionLabel;

    private ProgressBar pbRecognition;
    private TextView tvRecognitionLabel;

    private ProgressBar pbSequencing;
    private TextView tvSequencingLabel;

    private MaterialButton btnBack;

    private FirestoreHelper firestoreHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        firestoreHelper = FirestoreHelper.getInstance();
        sessionManager = new SessionManager(this);

        initViews();
        loadLatestProgress();
        setupListeners();
    }

    private void initViews() {
        pbMemory = findViewById(R.id.pbCategoryMemory);
        tvMemoryLabel = findViewById(R.id.tvMemoryStatusLabel);

        pbAttention = findViewById(R.id.pbCategoryAttention);
        tvAttentionLabel = findViewById(R.id.tvAttentionStatusLabel);

        pbRecognition = findViewById(R.id.pbCategoryRecognition);
        tvRecognitionLabel = findViewById(R.id.tvRecognitionStatusLabel);

        pbSequencing = findViewById(R.id.pbCategorySequencing);
        tvSequencingLabel = findViewById(R.id.tvSequencingStatusLabel);

        btnBack = findViewById(R.id.btnBackFromProgress);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadLatestProgress() {
        String patientId = sessionManager.getUserId();
        if (patientId == null) return;

        firestoreHelper.getLatestAssessment(patientId)
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        CognitiveAssessment assessment = doc.toObject(CognitiveAssessment.class);
                        if (assessment != null) {
                            applyProgressLevels(assessment);
                            return;
                        }
                    }
                    // Baseline default if no assessments yet
                    applyDefaultProgress();
                })
                .addOnFailureListener(e -> applyDefaultProgress());
    }

    private void applyProgressLevels(CognitiveAssessment assessment) {
        int memoryProgress = (int) Math.max(20, Math.min(100, assessment.getMemoryScore()));
        int attentionProgress = (int) Math.max(20, Math.min(100, assessment.getAttentionScore()));
        int recognitionProgress = (int) Math.max(20, Math.min(100, assessment.getRecognitionScore()));
        int sequencingProgress = (int) Math.max(20, Math.min(100, assessment.getOverallScore()));

        pbMemory.setProgress(memoryProgress);
        tvMemoryLabel.setText(getEncouragingStatusText(memoryProgress, "Memory"));

        pbAttention.setProgress(attentionProgress);
        tvAttentionLabel.setText(getEncouragingStatusText(attentionProgress, "Attention"));

        pbRecognition.setProgress(recognitionProgress);
        tvRecognitionLabel.setText(getEncouragingStatusText(recognitionProgress, "Recognition"));

        pbSequencing.setProgress(sequencingProgress);
        tvSequencingLabel.setText(getEncouragingStatusText(sequencingProgress, "Sequencing"));
    }

    private void applyDefaultProgress() {
        pbMemory.setProgress(75);
        tvMemoryLabel.setText("Flourishing & Strong 🌸");

        pbAttention.setProgress(70);
        tvAttentionLabel.setText("Sharp & Steady ☀️");

        pbRecognition.setProgress(80);
        tvRecognitionLabel.setText("Steady & Confident 🌟");

        pbSequencing.setProgress(75);
        tvSequencingLabel.setText("Great Rhythm & Focus 🎵");
    }

    /**
     * Maps scores to reassuring, encouraging verbal labels without revealing raw numbers.
     */
    private String getEncouragingStatusText(int score, String category) {
        if (score >= 75) {
            return "Flourishing & Strong 🌸";
        } else if (score >= 50) {
            return "Steady & Focused 🌟";
        } else {
            return "Gentle Practice & Care 🌿";
        }
    }
}
