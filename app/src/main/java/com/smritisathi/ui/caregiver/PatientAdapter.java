package com.smritisathi.ui.caregiver;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.smritisathi.R;
import com.smritisathi.data.FirestoreHelper;
import com.smritisathi.model.CognitiveAssessment;
import com.smritisathi.model.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter for CaregiverPatientListActivity.
 * Renders linked patient name, last-active timestamp, and colored score dot
 * based on the patient's latest cognitive assessment.
 */
public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.ViewHolder> {

    public interface OnPatientClickListener {
        void onPatientClick(User patient);
    }

    private final List<User> patients;
    private final OnPatientClickListener listener;
    private final FirestoreHelper firestoreHelper;
    private final Map<String, Double> cachedScores = new HashMap<>();

    public PatientAdapter(List<User> patients, OnPatientClickListener listener) {
        this.patients = patients;
        this.listener = listener;
        this.firestoreHelper = FirestoreHelper.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User patient = patients.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(!TextUtils.isEmpty(patient.getName()) ? patient.getName() : "Patient");

        // Format Last Active time
        long activeTime = patient.getLastActive() > 0 ? patient.getLastActive() : patient.getCreatedAt();
        if (activeTime > 0) {
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                    activeTime,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            );
            holder.tvLastActive.setText("Last active: " + relativeTime);
        } else {
            holder.tvLastActive.setText("Last active: Recently");
        }

        // Check if score is cached, otherwise query Firestore
        String uid = patient.getUid();
        if (cachedScores.containsKey(uid)) {
            Double score = cachedScores.get(uid);
            applyScoreDot(holder, context, score != null ? score : 75.0);
        } else {
            // Set temporary default
            applyScoreDot(holder, context, 75.0);

            if (!TextUtils.isEmpty(uid)) {
                firestoreHelper.getLatestAssessment(uid)
                        .addOnSuccessListener(querySnapshot -> {
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                CognitiveAssessment ca = querySnapshot.getDocuments().get(0).toObject(CognitiveAssessment.class);
                                if (ca != null) {
                                    double score = ca.getOverallScore();
                                    cachedScores.put(uid, score);
                                    applyScoreDot(holder, context, score);
                                    return;
                                }
                            }
                            cachedScores.put(uid, 75.0);
                        });
            }
        }

        holder.card.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPatientClick(patient);
            }
        });
    }

    private void applyScoreDot(ViewHolder holder, Context context, double score) {
        if (score >= 70) {
            holder.ivScoreDot.setImageResource(R.drawable.dot_green);
            holder.tvScoreLabel.setText(String.format(Locale.getDefault(), "Good (%.0f)", score));
            holder.tvScoreLabel.setTextColor(ContextCompat.getColor(context, R.color.dotGreen));
            holder.tvStatusText.setText("Cognitive Status: Flourishing 🌸");
        } else if (score >= 45) {
            holder.ivScoreDot.setImageResource(R.drawable.dot_amber);
            holder.tvScoreLabel.setText(String.format(Locale.getDefault(), "Fair (%.0f)", score));
            holder.tvScoreLabel.setTextColor(ContextCompat.getColor(context, R.color.dotAmber));
            holder.tvStatusText.setText("Cognitive Status: Moderate Practice 🌟");
        } else {
            holder.ivScoreDot.setImageResource(R.drawable.dot_red);
            holder.tvScoreLabel.setText(String.format(Locale.getDefault(), "Alert (%.0f)", score));
            holder.tvScoreLabel.setTextColor(ContextCompat.getColor(context, R.color.dotRed));
            holder.tvStatusText.setText("Cognitive Status: Needs Support 🌿");
        }
    }

    @Override
    public int getItemCount() {
        return patients != null ? patients.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView tvName;
        final TextView tvLastActive;
        final TextView tvStatusText;
        final ImageView ivScoreDot;
        final TextView tvScoreLabel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardPatientItem);
            tvName = itemView.findViewById(R.id.tvPatientItemName);
            tvLastActive = itemView.findViewById(R.id.tvPatientItemLastActive);
            tvStatusText = itemView.findViewById(R.id.tvPatientItemStatusText);
            ivScoreDot = itemView.findViewById(R.id.ivPatientScoreDot);
            tvScoreLabel = itemView.findViewById(R.id.tvPatientScoreLabel);
        }
    }
}
