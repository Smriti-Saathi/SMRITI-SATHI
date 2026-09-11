package com.smritisathi.ui.caregiver;

import android.content.Intent;
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
import com.smritisathi.model.User;
import com.smritisathi.ui.LinkPatientActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * CaregiverPatientListActivity — lists all patients linked to the currently logged in caregiver.
 * Displays patient name, last-active timestamp, and colored score dot indicator.
 */
public class CaregiverPatientListActivity extends AppCompatActivity implements PatientAdapter.OnPatientClickListener {

    private RecyclerView rvPatients;
    private LinearLayout layoutEmpty;
    private ProgressBar pbLoading;
    private MaterialButton btnBack;
    private MaterialButton btnLinkNew;
    private MaterialButton btnEmptyLink;

    private FirestoreHelper firestoreHelper;
    private SessionManager sessionManager;
    private PatientAdapter adapter;
    private final List<User> patientList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caregiver_patient_list);

        firestoreHelper = FirestoreHelper.getInstance();
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLinkedPatients();
    }

    private void initViews() {
        rvPatients = findViewById(R.id.rvCaregiverPatients);
        layoutEmpty = findViewById(R.id.layoutEmptyPatients);
        pbLoading = findViewById(R.id.pbPatientsLoading);
        btnBack = findViewById(R.id.btnBackFromPatientList);
        btnLinkNew = findViewById(R.id.btnLinkNewPatient);
        btnEmptyLink = findViewById(R.id.btnEmptyLinkPatient);

        rvPatients.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PatientAdapter(patientList, this);
        rvPatients.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnLinkNew.setOnClickListener(v -> {
            Intent intent = new Intent(this, LinkPatientActivity.class);
            startActivity(intent);
        });

        btnEmptyLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, LinkPatientActivity.class);
            startActivity(intent);
        });
    }

    private void loadLinkedPatients() {
        String caregiverUid = sessionManager.getUserId();
        if (caregiverUid == null || caregiverUid.isEmpty()) {
            pbLoading.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            return;
        }

        pbLoading.setVisibility(View.VISIBLE);

        firestoreHelper.getLinkedPatients(caregiverUid)
                .addOnSuccessListener(querySnapshot -> {
                    pbLoading.setVisibility(View.GONE);
                    patientList.clear();

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            User patient = doc.toObject(User.class);
                            if (patient != null) {
                                patientList.add(patient);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                    layoutEmpty.setVisibility(patientList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    pbLoading.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(patientList.isEmpty() ? View.VISIBLE : View.GONE);
                    Toast.makeText(this, "Could not load patients: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onPatientClick(User patient) {
        if (patient == null) return;
        Intent intent = new Intent(this, PatientDetailActivity.class);
        intent.putExtra("patientId", patient.getUid());
        intent.putExtra("patientName", patient.getName());
        startActivity(intent);
    }
}
