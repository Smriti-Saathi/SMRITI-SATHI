package com.smritisathi.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.smritisathi.R;
import com.smritisathi.data.FirestoreHelper;
import com.smritisathi.data.SessionManager;
import com.smritisathi.model.User;

/**
 * Caregiver activity to search for and link to a patient using a 6-character invite code.
 * Displays explicit "Invalid code" and "Already linked" error messages.
 */
public class LinkPatientActivity extends AppCompatActivity {

    private TextInputEditText etInviteCode;
    private MaterialButton btnVerifyAndLink;
    private ProgressBar pbLoading;
    private LinearLayout layoutErrorBanner;
    private TextView tvErrorMessage;
    private LinearLayout layoutSuccessBanner;
    private TextView tvSuccessMessage;

    private FirestoreHelper firestoreHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_patient);

        firestoreHelper = FirestoreHelper.getInstance();
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etInviteCode = findViewById(R.id.etInviteCode);
        btnVerifyAndLink = findViewById(R.id.btnVerifyAndLink);
        pbLoading = findViewById(R.id.pbLinkLoading);
        layoutErrorBanner = findViewById(R.id.layoutErrorBanner);
        tvErrorMessage = findViewById(R.id.tvLinkErrorMessage);
        layoutSuccessBanner = findViewById(R.id.layoutSuccessBanner);
        tvSuccessMessage = findViewById(R.id.tvLinkSuccessMessage);
    }

    private void setupListeners() {
        btnVerifyAndLink.setOnClickListener(v -> attemptLinkPatient());
    }

    private void attemptLinkPatient() {
        String code = etInviteCode.getText() != null ? etInviteCode.getText().toString().trim().toUpperCase() : "";

        hideFeedbackBanners();

        if (TextUtils.isEmpty(code)) {
            showError("Please enter the patient's 6-character invite code.");
            return;
        }

        if (code.length() != 6) {
            showError("Invite code must be exactly 6 characters (e.g. SM7K9P).");
            return;
        }

        String caregiverUid = sessionManager.getUserId();
        if (caregiverUid == null) {
            showError("Caregiver session invalid. Please log in again.");
            return;
        }

        setLoading(true);

        // Search Firestore users collection where inviteCode == code and role == "patient"
        firestoreHelper.findPatientByInviteCode(code)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        setLoading(false);
                        showError("Failed to verify invite code. Please check your network connection.");
                        return;
                    }

                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot.isEmpty()) {
                        setLoading(false);
                        // Explicit "Invalid code" error requirement
                        showError("Invalid code: No patient found matching invite code '" + code + "'. Please verify with your patient.");
                        return;
                    }

                    DocumentSnapshot patientDoc = querySnapshot.getDocuments().get(0);
                    User patient = patientDoc.toObject(User.class);
                    if (patient == null) {
                        setLoading(false);
                        showError("Invalid patient profile data. Please try again.");
                        return;
                    }

                    String existingCaregiverId = patient.getCaregiverId();

                    // Explicit "Already linked" error check
                    if (existingCaregiverId != null && !existingCaregiverId.trim().isEmpty()) {
                        setLoading(false);
                        if (existingCaregiverId.equals(caregiverUid)) {
                            showError("Already linked: You are already connected to " + patient.getName() + "!");
                        } else {
                            showError("Already linked: This patient is already linked to another caregiver account.");
                        }
                        return;
                    }

                    // Proceed to link caregiver to patient
                    linkCaregiver(patient, caregiverUid);
                });
    }

    private void linkCaregiver(User patient, String caregiverUid) {
        firestoreHelper.linkCaregiverToPatient(patient.getUid(), caregiverUid)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    // Save linked patient to caregiver session
                    sessionManager.saveLinkedPatient(patient.getUid(), patient.getName());

                    showSuccess("Success! You are now linked with " + patient.getName() + ".");

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Intent intent = new Intent(LinkPatientActivity.this, CaregiverDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }, 1200);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError("Failed to update patient record: " + e.getLocalizedMessage());
                });
    }

    private void setLoading(boolean isLoading) {
        pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnVerifyAndLink.setEnabled(!isLoading);
        etInviteCode.setEnabled(!isLoading);
    }

    private void showError(String message) {
        layoutErrorBanner.setVisibility(View.VISIBLE);
        layoutSuccessBanner.setVisibility(View.GONE);
        tvErrorMessage.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        layoutSuccessBanner.setVisibility(View.VISIBLE);
        layoutErrorBanner.setVisibility(View.GONE);
        tvSuccessMessage.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void hideFeedbackBanners() {
        layoutErrorBanner.setVisibility(View.GONE);
        layoutSuccessBanner.setVisibility(View.GONE);
    }
}
