package com.smritisathi.ui.reminders;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.smritisathi.R;
import com.smritisathi.data.FirestoreHelper;
import com.smritisathi.data.SessionManager;
import com.smritisathi.model.Reminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * ReminderActivity — displays today's reminders with direct Firestore CRUD.
 * "Done" and "Remind Later" buttons update Firestore in real time.
 */
public class ReminderActivity extends AppCompatActivity implements ReminderAdapter.OnReminderActionListener {

    private RecyclerView rvReminders;
    private LinearLayout layoutEmpty;
    private ProgressBar pbLoading;
    private MaterialButton btnAddReminder;

    private FirestoreHelper firestoreHelper;
    private SessionManager sessionManager;
    private ReminderAdapter adapter;
    private final List<Reminder> reminderList = new ArrayList<>();
    private String activePatientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        firestoreHelper = FirestoreHelper.getInstance();
        sessionManager = new SessionManager(this);

        // Can be passed via intent if caregiver is viewing patient reminders
        activePatientId = getIntent().getStringExtra("patientId");
        if (activePatientId == null || activePatientId.isEmpty()) {
            activePatientId = sessionManager.getUserId();
        }

        initViews();
        loadReminders();
        setupListeners();
    }

    private void initViews() {
        rvReminders = findViewById(R.id.rvReminders);
        layoutEmpty = findViewById(R.id.layoutEmptyReminders);
        pbLoading = findViewById(R.id.pbRemindersLoading);
        btnAddReminder = findViewById(R.id.btnAddReminder);

        rvReminders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter(reminderList, this);
        rvReminders.setAdapter(adapter);
    }

    private void setupListeners() {
        btnAddReminder.setOnClickListener(v -> showAddReminderDialog());
    }

    private void loadReminders() {
        if (activePatientId == null) {
            pbLoading.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            return;
        }

        pbLoading.setVisibility(View.VISIBLE);

        firestoreHelper.getRemindersForPatient(activePatientId)
                .addOnSuccessListener(querySnapshot -> {
                    pbLoading.setVisibility(View.GONE);
                    reminderList.clear();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Reminder reminder = doc.toObject(Reminder.class);
                            if (reminder != null) {
                                reminder.setId(doc.getId());
                                reminderList.add(reminder);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                    layoutEmpty.setVisibility(reminderList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    pbLoading.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(reminderList.isEmpty() ? View.VISIBLE : View.GONE);
                    Toast.makeText(this, "Could not load reminders: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDone(Reminder reminder) {
        if (reminder.getId() == null) return;

        firestoreHelper.updateReminderStatus(reminder.getId(), "Completed", 0)
                .addOnSuccessListener(aVoid -> {
                    reminder.setStatus("Completed");
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Marked as completed! Great job! 🌸", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRemindLater(Reminder reminder) {
        if (reminder.getId() == null) return;

        // Postpone by 15 minutes
        long newScheduledTime = System.currentTimeMillis() + (15 * 60 * 1000);

        firestoreHelper.updateReminderStatus(reminder.getId(), "Remind Later", newScheduledTime)
                .addOnSuccessListener(aVoid -> {
                    reminder.setStatus("Remind Later");
                    reminder.setScheduledTime(newScheduledTime);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Remind later set for 15 minutes ⏰", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showAddReminderDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_reminder, null);

        TextInputEditText etTitle = dialogView.findViewById(R.id.etReminderTitle);
        TextInputEditText etMedicine = dialogView.findViewById(R.id.etReminderMedicine);
        TextInputEditText etDosage = dialogView.findViewById(R.id.etReminderDosage);
        MaterialButton btnPickTime = dialogView.findViewById(R.id.btnPickReminderTime);

        Calendar selectedCalendar = Calendar.getInstance();

        btnPickTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedCalendar.set(Calendar.MINUTE, minute);
                selectedCalendar.set(Calendar.SECOND, 0);
                btnPickTime.setText(String.format("Time: %02d:%02d", hourOfDay, minute));
            }, selectedCalendar.get(Calendar.HOUR_OF_DAY), selectedCalendar.get(Calendar.MINUTE), false).show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Add Daily Reminder")
                .setView(dialogView)
                .setPositiveButton("Schedule Reminder", (dialog, which) -> {
                    String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
                    String medicine = etMedicine.getText() != null ? etMedicine.getText().toString().trim() : "";
                    String dosage = etDosage.getText() != null ? etDosage.getText().toString().trim() : "";

                    if (TextUtils.isEmpty(title)) {
                        title = !TextUtils.isEmpty(medicine) ? medicine : "Care Alert";
                    }

                    Reminder reminder = new Reminder(
                            null,
                            activePatientId,
                            "Medication",
                            title,
                            selectedCalendar.getTimeInMillis(),
                            "Daily",
                            "Pending",
                            medicine,
                            dosage
                    );

                    firestoreHelper.addReminder(reminder)
                            .addOnSuccessListener(docRef -> {
                                Toast.makeText(ReminderActivity.this, "Reminder scheduled! ⏰", Toast.LENGTH_SHORT).show();
                                loadReminders();
                            })
                            .addOnFailureListener(e -> Toast.makeText(ReminderActivity.this, "Failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
