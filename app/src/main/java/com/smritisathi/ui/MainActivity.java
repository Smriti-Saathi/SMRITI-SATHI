package com.smritisathi.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.smritisathi.R;
import com.smritisathi.data.FirestoreHelper;
import com.smritisathi.data.SessionManager;
import com.smritisathi.model.User;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcomeUser;
    private TextView tvRoleBadge;
    private MaterialCardView cardInviteCode;
    private TextView tvInviteCodeValue;
    private MaterialButton btnCopyCode;
    private TextView tvProfileEmail;
    private TextView tvProfilePhone;
    private TextView tvProfileAge;
    private TextView tvProfileLanguage;
    private TextView tvProfileRegion;
    private TextView tvProfileUid;
    private MaterialButton btnLogout;

    private SessionManager sessionManager;
    private FirestoreHelper firestoreHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        firestoreHelper = FirestoreHelper.getInstance();

        // Ensure user is logged in
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        initViews();
        displayUserData();
        fetchLatestFromFirestore();
        setupListeners();
    }

    private void initViews() {
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvRoleBadge = findViewById(R.id.tvRoleBadge);
        cardInviteCode = findViewById(R.id.cardInviteCode);
        tvInviteCodeValue = findViewById(R.id.tvInviteCodeValue);
        btnCopyCode = findViewById(R.id.btnCopyCode);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileAge = findViewById(R.id.tvProfileAge);
        tvProfileLanguage = findViewById(R.id.tvProfileLanguage);
        tvProfileRegion = findViewById(R.id.tvProfileRegion);
        tvProfileUid = findViewById(R.id.tvProfileUid);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void displayUserData() {
        String name = sessionManager.getUserName();
        String role = sessionManager.getUserRole();
        String email = sessionManager.getUserEmail();
        String phone = sessionManager.getPhone();
        String inviteCode = sessionManager.getInviteCode();
        String language = sessionManager.getPreferredLanguage();
        String region = sessionManager.getRegion();
        String uid = sessionManager.getUserId();

        tvWelcomeUser.setText("Welcome, " + (name.isEmpty() ? "Friend" : name) + "!");
        tvProfileEmail.setText("Email: " + email);
        tvProfilePhone.setText("Phone: " + (phone.isEmpty() ? "Not specified" : phone));
        tvProfileLanguage.setText("Preferred Language: " + language);
        tvProfileRegion.setText("Region: " + (region.isEmpty() ? "Not specified" : region));
        tvProfileUid.setText("UID: " + (uid != null ? uid : "-"));

        if ("patient".equalsIgnoreCase(role)) {
            tvRoleBadge.setText("Patient (Memory Care)");
            tvRoleBadge.setTextColor(getResources().getColor(R.color.patientPillText, null));
            tvRoleBadge.setBackgroundColor(getResources().getColor(R.color.patientPillBg, null));

            // Show invite code card for patient
            cardInviteCode.setVisibility(View.VISIBLE);
            tvInviteCodeValue.setText(inviteCode.isEmpty() ? "PENDING" : inviteCode);
        } else {
            tvRoleBadge.setText("Caregiver (Family / Care Provider)");
            tvRoleBadge.setTextColor(getResources().getColor(R.color.caregiverPillText, null));
            tvRoleBadge.setBackgroundColor(getResources().getColor(R.color.caregiverPillBg, null));

            // Caregivers don't generate invite codes, they connect using patients' codes
            cardInviteCode.setVisibility(View.GONE);
        }
    }

    private void fetchLatestFromFirestore() {
        String uid = sessionManager.getUserId();
        if (uid == null) return;

        firestoreHelper.getUser(uid).addOnSuccessListener(doc -> {
            if (doc != null && doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    sessionManager.saveUser(user);
                    displayUserData();
                    if (user.getAge() > 0) {
                        tvProfileAge.setText("Age: " + user.getAge() + " years");
                    }
                }
            }
        });
    }

    private void setupListeners() {
        btnCopyCode.setOnClickListener(v -> {
            String code = tvInviteCodeValue.getText().toString();
            if (!code.isEmpty() && !code.equals("------")) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Smriti Sathi Invite Code", code);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, getString(R.string.code_copied), Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out of Smriti Sathi?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    sessionManager.logout();
                    Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
