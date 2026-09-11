package com.smritisathi.ui.caregiver;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.smritisathi.R;
import com.smritisathi.data.FirestoreHelper;
import com.smritisathi.data.SessionManager;
import com.smritisathi.model.CaregiverAlert;
import com.smritisathi.model.CognitiveAssessment;
import com.smritisathi.model.GameSession;
import com.smritisathi.model.Reminder;
import com.smritisathi.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CaregiverAlertsActivity — displays automated, client-computed clinical monitoring alerts:
 * 1. Cognitive assessment score drop > 15% over the past 7 days
 * 2. Missed medicine in the past 24 hours (scheduled past time still marked Pending)
 * 3. Zero cognitive game sessions in the last 3 days
 *
 * Adheres strictly to the requirement of having the clinical disclaimer pinned at the top:
 * "This platform supports cognitive engagement and monitoring. It is not a diagnostic tool."
 */
public class CaregiverAlertsActivity extends AppCompatActivity {

    private static final long ONE_DAY_MS = 24 * 60 * 60 * 1000L;
    private static final long THREE_DAYS_MS = 3 * ONE_DAY_MS;
    private static final long SEVEN_DAYS_MS = 7 * ONE_DAY_MS;

    private RecyclerView rvAlerts;
    private LinearLayout layoutEmpty;
    private ProgressBar pbLoading;
    private MaterialButton btnBack;
    private MaterialButton btnRefresh;

    private FirestoreHelper firestoreHelper;
    private SessionManager sessionManager;
    private AlertAdapter adapter;
    private final List<CaregiverAlert> alertList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caregiver_alerts);

        firestoreHelper = FirestoreHelper.getInstance();
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
        computeCaregiverAlerts();
    }

    private void initViews() {
        rvAlerts = findViewById(R.id.rvCaregiverAlerts);
        layoutEmpty = findViewById(R.id.layoutEmptyAlerts);
        pbLoading = findViewById(R.id.pbAlertsLoading);
        btnBack = findViewById(R.id.btnBackFromAlerts);
        btnRefresh = findViewById(R.id.btnRefreshAlerts);

        rvAlerts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlertAdapter(alertList);
        rvAlerts.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnRefresh.setOnClickListener(v -> computeCaregiverAlerts());
    }

    /**
     * Executes client-side rule computation across all patients linked to this caregiver.
     */
    private void computeCaregiverAlerts() {
        String caregiverUid = sessionManager.getUserId();
        if (caregiverUid == null || caregiverUid.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            return;
        }

        pbLoading.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        alertList.clear();
        adapter.notifyDataSetChanged();

        firestoreHelper.getLinkedPatients(caregiverUid)
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        pbLoading.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    List<User> patients = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        User p = doc.toObject(User.class);
                        if (p != null) {
                            patients.add(p);
                        }
                    }

                    if (patients.isEmpty()) {
                        pbLoading.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    // Process each patient with 3 checks (assessments, reminders, game sessions)
                    AtomicInteger pendingTasks = new AtomicInteger(patients.size() * 3);

                    for (User patient : patients) {
                        checkScoreDrop(patient, pendingTasks);
                        checkMissedMedication(patient, pendingTasks);
                        checkSessionInactivity(patient, pendingTasks);
                    }
                })
                .addOnFailureListener(e -> {
                    pbLoading.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Could not fetch patients: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Rule 1: Cognitive score drop > 15% over the past 7 days
     */
    private void checkScoreDrop(User patient, AtomicInteger pendingTasks) {
        long now = System.currentTimeMillis();
        long sevenDaysAgo = now - SEVEN_DAYS_MS;

        firestoreHelper.getCognitiveAssessmentsCollection()
                .whereEqualTo("patientId", patient.getUid())
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot != null && !snapshot.isEmpty()) {
                        List<CognitiveAssessment> list = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            CognitiveAssessment ca = doc.toObject(CognitiveAssessment.class);
                            if (ca != null) {
                                list.add(ca);
                            }
                        }

                        if (list.size() >= 2) {
                            CognitiveAssessment latest = list.get(0);
                            CognitiveAssessment reference = null;

                            // Find earliest assessment within last 7 days or closest prior
                            for (int i = list.size() - 1; i >= 1; i--) {
                                CognitiveAssessment ca = list.get(i);
                                if (ca.getTimestamp() >= sevenDaysAgo) {
                                    reference = ca;
                                    break;
                                }
                            }
                            if (reference == null) {
                                reference = list.get(list.size() - 1);
                            }

                            double scoreDiff = reference.getOverallScore() - latest.getOverallScore();
                            if (scoreDiff > 15.0) {
                                CaregiverAlert alert = new CaregiverAlert(
                                        null,
                                        "Score Drop > 15% in 7 Days",
                                        String.format("Cognitive assessment score declined by %.1f%% over the past week (from %.0f to %.0f). Consider scheduling a gentle review.",
                                                scoreDiff, reference.getOverallScore(), latest.getOverallScore()),
                                        patient.getName(),
                                        "SCORE_DROP",
                                        CaregiverAlert.Severity.CRITICAL,
                                        latest.getTimestamp()
                                );
                                alertList.add(alert);
                            }
                        }
                    }
                    onTaskComplete(pendingTasks);
                })
                .addOnFailureListener(e -> onTaskComplete(pendingTasks));
    }

    /**
     * Rule 2: Missed medication in past 24 hours (past scheduled time still marked Pending)
     */
    private void checkMissedMedication(User patient, AtomicInteger pendingTasks) {
        long now = System.currentTimeMillis();
        long twentyFourHoursAgo = now - ONE_DAY_MS;

        firestoreHelper.getRemindersCollection()
                .whereEqualTo("patientId", patient.getUid())
                .whereEqualTo("status", "Pending")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot != null && !snapshot.isEmpty()) {
                        int missedCount = 0;
                        StringBuilder meds = new StringBuilder();

                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Reminder reminder = doc.toObject(Reminder.class);
                            if (reminder != null) {
                                long scheduled = reminder.getScheduledTime();
                                // Was scheduled between 24h ago and now
                                if (scheduled > 0 && scheduled >= twentyFourHoursAgo && scheduled <= now) {
                                    missedCount++;
                                    if (meds.length() > 0) meds.append(", ");
                                    meds.append(reminder.getTitle() != null ? reminder.getTitle() : "Medication");
                                }
                            }
                        }

                        if (missedCount > 0) {
                            CaregiverAlert alert = new CaregiverAlert(
                                    null,
                                    "Missed Medicine in Last 24 Hours",
                                    patient.getName() + " has " + missedCount + " overdue scheduled medicine reminder(s) (" + meds + "). Please check in on dose administration.",
                                    patient.getName(),
                                    "MISSED_MEDICINE",
                                    CaregiverAlert.Severity.WARNING,
                                    now
                            );
                            alertList.add(alert);
                        }
                    }
                    onTaskComplete(pendingTasks);
                })
                .addOnFailureListener(e -> onTaskComplete(pendingTasks));
    }

    /**
     * Rule 3: Zero cognitive game sessions in 3 days
     */
    private void checkSessionInactivity(User patient, AtomicInteger pendingTasks) {
        long now = System.currentTimeMillis();
        long threeDaysAgo = now - THREE_DAYS_MS;

        firestoreHelper.getGameSessionsCollection()
                .whereEqualTo("patientId", patient.getUid())
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    boolean inactive = false;
                    long lastPlayed = 0;

                    if (snapshot == null || snapshot.isEmpty()) {
                        inactive = true;
                    } else {
                        GameSession session = snapshot.getDocuments().get(0).toObject(GameSession.class);
                        if (session != null) {
                            lastPlayed = session.getTimestamp();
                            if (lastPlayed < threeDaysAgo) {
                                inactive = true;
                            }
                        } else {
                            inactive = true;
                        }
                    }

                    if (inactive) {
                        CaregiverAlert alert = new CaregiverAlert(
                                null,
                                "Zero Brain Games in 3 Days",
                                "No cognitive exercises or brain games have been recorded for " + patient.getName() + " in over 3 days. Encourage a brief 5-minute memory or puzzle session today.",
                                patient.getName(),
                                "INACTIVITY",
                                CaregiverAlert.Severity.WARNING,
                                lastPlayed > 0 ? lastPlayed : now
                        );
                        alertList.add(alert);
                    }
                    onTaskComplete(pendingTasks);
                })
                .addOnFailureListener(e -> onTaskComplete(pendingTasks));
    }

    private void onTaskComplete(AtomicInteger pendingTasks) {
        if (pendingTasks.decrementAndGet() <= 0) {
            runOnUiThread(() -> {
                pbLoading.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                layoutEmpty.setVisibility(alertList.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }
    }
}
