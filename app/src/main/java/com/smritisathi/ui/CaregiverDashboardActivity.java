package com.smritisathi.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.smritisathi.R;
import com.smritisathi.data.FirestoreHelper;
import com.smritisathi.data.SessionManager;
import com.smritisathi.model.User;

public class CaregiverDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private MaterialButton btnLogout;
    private MaterialCardView cardLinkedPatient;
    private TextView tvLinkedPatientName;
    private TextView tvLinkedPatientId;
    private MaterialButton btnRelinkPatient;
    private MaterialCardView cardNoPatient;
    private MaterialButton btnLinkPatientNow;

    private SessionManager sessionManager;
    private FirestoreHelper firestoreHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caregiver_dashboard);

        sessionManager = new SessionManager(this);
        firestoreHelper = FirestoreHelper.getInstance();

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDashboardState();
        checkFirestoreForLinkedPatient();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvCaregiverWelcome);
        btnLogout = findViewById(R.id.btnCaregiverLogout);
        cardLinkedPatient = findViewById(R.id.cardLinkedPatient);
        tvLinkedPatientName = findViewById(R.id.tvLinkedPatientName);
        tvLinkedPatientId = findViewById(R.id.tvLinkedPatientId);
        btnRelinkPatient = findViewById(R.id.btnRelinkPatient);
        cardNoPatient = findViewById(R.id.cardNoPatient);
        btnLinkPatientNow = findViewById(R.id.btnLinkPatientNow);

        String caregiverName = sessionManager.getUserName();
        tvWelcome.setText("Welcome, " + (caregiverName.isEmpty() ? "Caregiver" : caregiverName) + "!");
    }

    private void updateDashboardState() {
        if (sessionManager.hasLinkedPatient()) {
            cardLinkedPatient.setVisibility(View.VISIBLE);
            cardNoPatient.setVisibility(View.GONE);
            tvLinkedPatientName.setText(sessionManager.getLinkedPatientName());
            tvLinkedPatientId.setText("Patient UID: " + sessionManager.getLinkedPatientId());
        } else {
            cardLinkedPatient.setVisibility(View.GONE);
            cardNoPatient.setVisibility(View.VISIBLE);
        }
    }

    private void checkFirestoreForLinkedPatient() {
        String caregiverUid = sessionManager.getUserId();
        if (caregiverUid == null) return;

        firestoreHelper.getUsersCollection()
                .whereEqualTo("caregiverId", caregiverUid)
                .whereEqualTo("role", "patient")
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        User patient = doc.toObject(User.class);
                        if (patient != null) {
                            sessionManager.saveLinkedPatient(patient.getUid(), patient.getName());
                            updateDashboardState();
                        }
                    }
                });
    }

    private void setupListeners() {
        btnLinkPatientNow.setOnClickListener(v -> {
            Intent intent = new Intent(this, LinkPatientActivity.class);
            startActivity(intent);
        });

        btnRelinkPatient.setOnClickListener(v -> {
            Intent intent = new Intent(this, LinkPatientActivity.class);
            startActivity(intent);
        });

        // Quick shortcuts on linked patient card
        MaterialButton btnQuickAnalytics = findViewById(R.id.btnQuickAnalytics);
        if (btnQuickAnalytics != null) {
            btnQuickAnalytics.setOnClickListener(v -> {
                if (sessionManager.hasLinkedPatient()) {
                    Intent intent = new Intent(this, com.smritisathi.ui.caregiver.PatientDetailActivity.class);
                    intent.putExtra("patientId", sessionManager.getLinkedPatientId());
                    intent.putExtra("patientName", sessionManager.getLinkedPatientName());
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Please link a patient first", Toast.LENGTH_SHORT).show();
                }
            });
        }

        MaterialButton btnQuickReminders = findViewById(R.id.btnQuickReminders);
        if (btnQuickReminders != null) {
            btnQuickReminders.setOnClickListener(v -> {
                if (sessionManager.hasLinkedPatient()) {
                    Intent intent = new Intent(this, com.smritisathi.ui.reminders.ReminderActivity.class);
                    intent.putExtra("patientId", sessionManager.getLinkedPatientId());
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Please link a patient first", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Section F: All Linked Patients List
        MaterialCardView cardCaregiverPatientList = findViewById(R.id.cardCaregiverPatientList);
        if (cardCaregiverPatientList != null) {
            cardCaregiverPatientList.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.smritisathi.ui.caregiver.CaregiverPatientListActivity.class);
                startActivity(intent);
            });
        }

        // Section F: Caregiver Alerts Hub
        MaterialCardView cardCaregiverAlertsHub = findViewById(R.id.cardCaregiverAlertsHub);
        if (cardCaregiverAlertsHub != null) {
            cardCaregiverAlertsHub.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.smritisathi.ui.caregiver.CaregiverAlertsActivity.class);
                startActivity(intent);
            });
        }

        // Section F: Family Memory Moments
        MaterialCardView cardCaregiverMemoriesHub = findViewById(R.id.cardCaregiverMemoriesHub);
        if (cardCaregiverMemoriesHub != null) {
            cardCaregiverMemoriesHub.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.smritisathi.ui.memories.MemoryMomentsActivity.class);
                if (sessionManager.hasLinkedPatient()) {
                    intent.putExtra("patientId", sessionManager.getLinkedPatientId());
                }
                startActivity(intent);
            });
        }

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out of Smriti Sathi?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    sessionManager.logout();
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
