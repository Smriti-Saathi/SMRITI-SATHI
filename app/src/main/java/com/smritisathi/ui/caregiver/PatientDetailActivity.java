package com.smritisathi.ui.caregiver;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.smritisathi.R;
import com.smritisathi.data.FirestoreHelper;
import com.smritisathi.data.SessionManager;
import com.smritisathi.model.CognitiveAssessment;
import com.smritisathi.ui.memories.MemoryMomentsActivity;
import com.smritisathi.ui.reminders.ReminderActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * PatientDetailActivity — renders longitudinal cognitive analytics using MPAndroidChart.
 * Visualizes the last 7 cognitive assessments with composite and sub-domain scores.
 */
public class PatientDetailActivity extends AppCompatActivity {

    private TextView tvPatientName;
    private TextView tvCurrentScoreValue;
    private TextView tvRecommendedTier;
    private ImageView ivStatusDot;
    private LineChart lineChart;
    private TextView tvChartEmptyNotice;
    private TextView tvMemoryScore;
    private TextView tvAttentionScore;
    private TextView tvRecognitionScore;
    private MaterialButton btnBack;
    private MaterialButton btnViewReminders;
    private MaterialButton btnViewMemories;

    private FirestoreHelper firestoreHelper;
    private SessionManager sessionManager;
    private String patientId;
    private String patientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);

        firestoreHelper = FirestoreHelper.getInstance();
        sessionManager = new SessionManager(this);

        patientId = getIntent().getStringExtra("patientId");
        patientName = getIntent().getStringExtra("patientName");

        if (patientId == null || patientId.isEmpty()) {
            if (sessionManager.hasLinkedPatient()) {
                patientId = sessionManager.getLinkedPatientId();
                patientName = sessionManager.getLinkedPatientName();
            } else {
                patientId = sessionManager.getUserId();
                patientName = sessionManager.getUserName();
            }
        }

        initViews();
        setupListeners();
        loadAssessmentHistory();
    }

    private void initViews() {
        tvPatientName = findViewById(R.id.tvDetailPatientName);
        tvCurrentScoreValue = findViewById(R.id.tvCurrentScoreValue);
        tvRecommendedTier = findViewById(R.id.tvRecommendedTier);
        ivStatusDot = findViewById(R.id.ivDetailStatusDot);
        lineChart = findViewById(R.id.lineChartAssessments);
        tvChartEmptyNotice = findViewById(R.id.tvChartEmptyNotice);
        tvMemoryScore = findViewById(R.id.tvDetailMemoryScore);
        tvAttentionScore = findViewById(R.id.tvDetailAttentionScore);
        tvRecognitionScore = findViewById(R.id.tvDetailRecognitionScore);
        btnBack = findViewById(R.id.btnBackFromDetail);
        btnViewReminders = findViewById(R.id.btnViewPatientReminders);
        btnViewMemories = findViewById(R.id.btnViewPatientMemories);

        tvPatientName.setText(patientName != null && !patientName.isEmpty() ? patientName + " - Analytics" : "Patient Analytics");
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnViewReminders.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReminderActivity.class);
            intent.putExtra("patientId", patientId);
            startActivity(intent);
        });

        btnViewMemories.setOnClickListener(v -> {
            Intent intent = new Intent(this, MemoryMomentsActivity.class);
            intent.putExtra("patientId", patientId);
            startActivity(intent);
        });
    }

    private void loadAssessmentHistory() {
        if (patientId == null || patientId.isEmpty()) {
            showEmptyChart();
            return;
        }

        firestoreHelper.getLastSevenAssessments(patientId)
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        List<CognitiveAssessment> assessments = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            CognitiveAssessment ca = doc.toObject(CognitiveAssessment.class);
                            if (ca != null) {
                                assessments.add(ca);
                            }
                        }

                        if (!assessments.isEmpty()) {
                            renderChart(assessments);
                            renderLatestMetrics(assessments.get(assessments.size() - 1));
                            return;
                        }
                    }
                    showEmptyChart();
                })
                .addOnFailureListener(e -> {
                    showEmptyChart();
                    Toast.makeText(this, "Could not load assessments: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void renderChart(List<CognitiveAssessment> assessments) {
        lineChart.setVisibility(View.VISIBLE);
        tvChartEmptyNotice.setVisibility(View.GONE);

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < assessments.size(); i++) {
            float score = (float) assessments.get(i).getOverallScore();
            entries.add(new Entry(i + 1, score));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Cognitive Wellness Score");
        dataSet.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
        dataSet.setCircleColor(ContextCompat.getColor(this, R.color.colorSecondary));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setCircleHoleRadius(2.5f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.textPrimary));
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(ContextCompat.getColor(this, R.color.colorPrimaryContainer));
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.0f", value);
            }
        });

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Styling Chart
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(true);
        lineChart.setPinchZoom(false);
        lineChart.setDoubleTapToZoomEnabled(false);

        // X Axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "Session " + (int) value;
            }
        });

        // Y Axis
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        lineChart.animateX(800);
        lineChart.invalidate();
    }

    private void renderLatestMetrics(CognitiveAssessment latest) {
        double overall = latest.getOverallScore();
        tvCurrentScoreValue.setText(String.format(Locale.getDefault(), "%.0f / 100", overall));

        String tier = latest.getRecommendedDifficulty();
        if (tier == null || tier.isEmpty()) {
            tier = overall >= 75 ? "Difficult" : (overall >= 45 ? "Moderate" : "Easy");
        }
        tvRecommendedTier.setText("Recommended Level: " + tier);

        if (overall >= 70) {
            ivStatusDot.setImageResource(R.drawable.dot_green);
        } else if (overall >= 45) {
            ivStatusDot.setImageResource(R.drawable.dot_amber);
        } else {
            ivStatusDot.setImageResource(R.drawable.dot_red);
        }

        tvMemoryScore.setText(String.format(Locale.getDefault(), "%.0f%%", latest.getMemoryScore()));
        tvAttentionScore.setText(String.format(Locale.getDefault(), "%.0f%%", latest.getAttentionScore()));
        tvRecognitionScore.setText(String.format(Locale.getDefault(), "%.0f%%", latest.getRecognitionScore()));
    }

    private void showEmptyChart() {
        lineChart.setVisibility(View.GONE);
        tvChartEmptyNotice.setVisibility(View.VISIBLE);
        tvCurrentScoreValue.setText("75 / 100");
        tvRecommendedTier.setText("Baseline: Moderate");
        ivStatusDot.setImageResource(R.drawable.dot_green);
        tvMemoryScore.setText("75%");
        tvAttentionScore.setText("70%");
        tvRecognitionScore.setText("80%");
    }
}
