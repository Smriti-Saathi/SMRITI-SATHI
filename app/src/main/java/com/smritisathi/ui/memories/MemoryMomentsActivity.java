package com.smritisathi.ui.memories;

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
import com.smritisathi.model.MemoryMoment;

import java.util.ArrayList;
import java.util.List;

/**
 * MemoryMomentsActivity — displays family photos & reminiscence therapy cards.
 * Direct Firestore CRUD on 'memoryMoments' collection.
 * Tapping a card reveals the person's identity and relationship to support cognitive memory.
 */
public class MemoryMomentsActivity extends AppCompatActivity {

    private RecyclerView rvMemoryMoments;
    private LinearLayout layoutEmpty;
    private ProgressBar pbLoading;
    private MaterialButton btnBack;
    private MaterialButton btnAddMemory;
    private MaterialButton btnAddFirstMemory;

    private FirestoreHelper firestoreHelper;
    private SessionManager sessionManager;
    private MemoryMomentsAdapter adapter;
    private final List<MemoryMoment> memoryList = new ArrayList<>();
    private String activePatientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_moments);

        firestoreHelper = FirestoreHelper.getInstance();
        sessionManager = new SessionManager(this);

        activePatientId = getIntent().getStringExtra("patientId");
        if (TextUtils.isEmpty(activePatientId)) {
            // Check if patient themselves or caregiver's linked patient
            if (sessionManager.hasLinkedPatient()) {
                activePatientId = sessionManager.getLinkedPatientId();
            } else {
                activePatientId = sessionManager.getUserId();
            }
        }

        initViews();
        loadMemories();
        setupListeners();
    }

    private void initViews() {
        rvMemoryMoments = findViewById(R.id.rvMemoryMoments);
        layoutEmpty = findViewById(R.id.layoutEmptyMemories);
        pbLoading = findViewById(R.id.pbMemoriesLoading);
        btnBack = findViewById(R.id.btnBackFromMemories);
        btnAddMemory = findViewById(R.id.btnAddMemory);
        btnAddFirstMemory = findViewById(R.id.btnAddFirstMemory);

        rvMemoryMoments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MemoryMomentsAdapter(memoryList);
        rvMemoryMoments.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnAddMemory.setOnClickListener(v -> showAddMemoryDialog());
        btnAddFirstMemory.setOnClickListener(v -> showAddMemoryDialog());
    }

    private void loadMemories() {
        if (TextUtils.isEmpty(activePatientId)) {
            pbLoading.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            return;
        }

        pbLoading.setVisibility(View.VISIBLE);

        firestoreHelper.getMemoryMomentsForPatient(activePatientId)
                .addOnSuccessListener(querySnapshot -> {
                    pbLoading.setVisibility(View.GONE);
                    memoryList.clear();

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            MemoryMoment moment = doc.toObject(MemoryMoment.class);
                            if (moment != null) {
                                moment.setId(doc.getId());
                                memoryList.add(moment);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                    layoutEmpty.setVisibility(memoryList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    pbLoading.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(memoryList.isEmpty() ? View.VISIBLE : View.GONE);
                    Toast.makeText(this, "Could not load memories: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddMemoryDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_memory, null);

        TextInputEditText etPersonName = dialogView.findViewById(R.id.etMemoryPersonName);
        TextInputEditText etRelationship = dialogView.findViewById(R.id.etMemoryRelationship);
        TextInputEditText etImageUrl = dialogView.findViewById(R.id.etMemoryImageUrl);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etMemoryDescription);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Save Memory", (dialog, which) -> {
                    String name = etPersonName.getText() != null ? etPersonName.getText().toString().trim() : "";
                    String relationship = etRelationship.getText() != null ? etRelationship.getText().toString().trim() : "";
                    String imageUrl = etImageUrl.getText() != null ? etImageUrl.getText().toString().trim() : "";
                    String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(this, "Please enter the person's name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    MemoryMoment moment = new MemoryMoment(
                            null,
                            activePatientId,
                            imageUrl,
                            name,
                            relationship,
                            description,
                            sessionManager.getUserId(),
                            System.currentTimeMillis()
                    );

                    firestoreHelper.addMemoryMoment(moment)
                            .addOnSuccessListener(docRef -> {
                                Toast.makeText(this, "Memory saved successfully! 🌸", Toast.LENGTH_SHORT).show();
                                loadMemories();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error saving: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
