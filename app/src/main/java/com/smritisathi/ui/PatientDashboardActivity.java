package com.smritisathi.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.QuerySnapshot;
import com.smritisathi.R;
import com.smritisathi.data.FirestoreHelper;
import com.smritisathi.data.SessionManager;
import com.smritisathi.model.Reminder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * High-accessibility Patient Dashboard for elderly cognitive care.
 * Displays greeting, today's brain game, live next-reminder preview,
 * memory moments shortcut, and floating voice companion button.
 * All user text formatted at minimum 18sp for optimal readability.
 */
public class PatientDashboardActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private TextView tvDate;
    private MaterialButton btnLogout;

    private MaterialCardView cardPlayGame;
    private MaterialButton btnPlayGame;

    private MaterialCardView cardNextReminder;
    private LinearLayout layoutReminderDetails;
    private TextView tvReminderTitle;
    private TextView tvReminderTime;
    private TextView tvReminderDosage;
    private TextView tvNoReminders;

    private MaterialCardView cardMemoryMoments;
    private MaterialButton btnMemoryMoments;

    private MaterialButton fabTalkToSathi;

    private SessionManager sessionManager;
    private FirestoreHelper firestoreHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard);

        sessionManager = new SessionManager(this);
        firestoreHelper = FirestoreHelper.getInstance();

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        initViews();
        setupGreetingAndDate();
        loadNextPendingReminder();
        setupListeners();
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tvPatientGreeting);
        tvDate = findViewById(R.id.tvPatientDate);
        btnLogout = findViewById(R.id.btnPatientLogout);

        cardPlayGame = findViewById(R.id.cardPlayGame);
        btnPlayGame = findViewById(R.id.btnPlayGame);

        cardNextReminder = findViewById(R.id.cardNextReminder);
        layoutReminderDetails = findViewById(R.id.layoutReminderDetails);
        tvReminderTitle = findViewById(R.id.tvReminderTitle);
        tvReminderTime = findViewById(R.id.tvReminderTime);
        tvReminderDosage = findViewById(R.id.tvReminderDosage);
        tvNoReminders = findViewById(R.id.tvNoReminders);

        cardMemoryMoments = findViewById(R.id.cardMemoryMoments);
        btnMemoryMoments = findViewById(R.id.btnMemoryMoments);

        fabTalkToSathi = findViewById(R.id.fabTalkToSathi);
    }

    private void setupGreetingAndDate() {
        String patientName = sessionManager.getUserName();
        if (patientName == null || patientName.trim().isEmpty()) {
            patientName = "Friend";
        }

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greetingPrefix;
        if (hour < 12) {
            greetingPrefix = "Good morning, " + patientName + "! 🌸";
        } else if (hour < 17) {
            greetingPrefix = "Good afternoon, " + patientName + "! ☀️";
        } else {
            greetingPrefix = "Good evening, " + patientName + "! 🌙";
        }
        tvGreeting.setText(greetingPrefix);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        String formattedDate = "Today is " + dateFormat.format(new Date());
        tvDate.setText(formattedDate);
    }

    /**
     * Executes live Firestore query for next pending reminder:
     * patientId == current user, status == "Pending", sorted by scheduledTime ASC, limit 1.
     */
    private void loadNextPendingReminder() {
        String patientUid = sessionManager.getUserId();
        if (patientUid == null) return;

        firestoreHelper.getNextPendingReminder(patientUid)
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        Reminder reminder = querySnapshot.getDocuments().get(0).toObject(Reminder.class);
                        if (reminder != null) {
                            displayPendingReminder(reminder);
                            return;
                        }
                    }
                    showNoRemindersState();
                })
                .addOnFailureListener(e -> showNoRemindersState());
    }

    private void displayPendingReminder(Reminder reminder) {
        layoutReminderDetails.setVisibility(View.VISIBLE);
        tvNoReminders.setVisibility(View.GONE);

        String title = reminder.getTitle();
        if (title == null || title.isEmpty()) {
            title = reminder.getMedicineName() != null ? reminder.getMedicineName() : "Care Alert";
        }
        tvReminderTitle.setText(title);

        long time = reminder.getScheduledTime();
        if (time > 0) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            tvReminderTime.setText("⏰ Scheduled for " + timeFormat.format(new Date(time)));
        } else {
            tvReminderTime.setText("⏰ Scheduled for today");
        }

        String medicine = reminder.getMedicineName() != null ? reminder.getMedicineName() : "";
        String dosage = reminder.getDosage() != null ? reminder.getDosage() : "";
        if (!medicine.isEmpty() || !dosage.isEmpty()) {
            tvReminderDosage.setVisibility(View.VISIBLE);
            tvReminderDosage.setText("Dosage: " + medicine + (dosage.isEmpty() ? "" : " • " + dosage));
        } else {
            tvReminderDosage.setVisibility(View.GONE);
        }
    }

    private void showNoRemindersState() {
        layoutReminderDetails.setVisibility(View.GONE);
        tvNoReminders.setVisibility(View.VISIBLE);
    }

    private void setupListeners() {
        btnPlayGame.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.smritisathi.ui.games.GamesHubActivity.class);
            startActivity(intent);
        });

        cardPlayGame.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.smritisathi.ui.games.GamesHubActivity.class);
            startActivity(intent);
        });

        // Reminders list shortcut
        cardNextReminder.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.smritisathi.ui.reminders.ReminderActivity.class);
            startActivity(intent);
        });

        // Family Memory Moments
        btnMemoryMoments.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.smritisathi.ui.memories.MemoryMomentsActivity.class);
            startActivity(intent);
        });

        cardMemoryMoments.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.smritisathi.ui.memories.MemoryMomentsActivity.class);
            startActivity(intent);
        });

        // Wellness Progress Journey
        MaterialButton btnViewProgress = findViewById(R.id.btnViewProgress);
        if (btnViewProgress != null) {
            btnViewProgress.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.smritisathi.ui.progress.ProgressActivity.class);
                startActivity(intent);
            });
        }

        MaterialCardView cardWellnessJourney = findViewById(R.id.cardWellnessJourney);
        if (cardWellnessJourney != null) {
            cardWellnessJourney.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.smritisathi.ui.progress.ProgressActivity.class);
                startActivity(intent);
            });
        }

        fabTalkToSathi.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.smritisathi.ui.voice.TalkToSathiActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Rest for Now?")
                .setMessage("Are you sure you want to log out of Smriti Sathi?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    sessionManager.logout();
                    Toast.makeText(this, "Logged out. Take care!", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                })
                .setNegativeButton("Stay", null)
                .show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
