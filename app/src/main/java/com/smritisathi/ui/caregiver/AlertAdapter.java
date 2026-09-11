package com.smritisathi.ui.caregiver;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.smritisathi.R;
import com.smritisathi.model.CaregiverAlert;

import java.util.List;

/**
 * Adapter for CaregiverAlertsActivity.
 * Visualizes client-computed cognitive drop, missed medicine, and session inactivity alerts.
 */
public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {

    private final List<CaregiverAlert> alertList;

    public AlertAdapter(List<CaregiverAlert> alertList) {
        this.alertList = alertList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alert, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CaregiverAlert alert = alertList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvTitle.setText(alert.getTitle());
        holder.tvPatientName.setText("Patient: " + alert.getPatientName());
        holder.tvDescription.setText(alert.getDescription());

        if (alert.getTimestamp() > 0) {
            CharSequence relative = DateUtils.getRelativeTimeSpanString(
                    alert.getTimestamp(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
            );
            holder.tvTimestamp.setText("Detected: " + relative);
        } else {
            holder.tvTimestamp.setText("Detected just now");
        }

        // Configure Severity Styling
        if (alert.getSeverity() == CaregiverAlert.Severity.CRITICAL) {
            holder.tvSeverityBadge.setText("CRITICAL");
            holder.tvSeverityBadge.setTextColor(ContextCompat.getColor(context, R.color.colorError));
            holder.tvSeverityBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorErrorBg));
            holder.ivIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_card_bell));
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorError));
            holder.iconBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorErrorBg));
        } else if (alert.getSeverity() == CaregiverAlert.Severity.WARNING) {
            holder.tvSeverityBadge.setText("ATTENTION");
            holder.tvSeverityBadge.setTextColor(ContextCompat.getColor(context, R.color.colorWarning));
            holder.tvSeverityBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.reminderCardBg));
            holder.ivIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_alarm));
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorWarning));
            holder.iconBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.reminderCardBg));
        } else {
            holder.tvSeverityBadge.setText("NOTICE");
            holder.tvSeverityBadge.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            holder.tvSeverityBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorPrimaryContainer));
            holder.ivIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_card_star));
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
            holder.iconBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorPrimaryContainer));
        }
    }

    @Override
    public int getItemCount() {
        return alertList != null ? alertList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvSeverityBadge;
        final TextView tvPatientName;
        final TextView tvDescription;
        final TextView tvTimestamp;
        final ImageView ivIcon;
        final FrameLayout iconBadge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAlertTitle);
            tvSeverityBadge = itemView.findViewById(R.id.tvAlertSeverityBadge);
            tvPatientName = itemView.findViewById(R.id.tvAlertPatientName);
            tvDescription = itemView.findViewById(R.id.tvAlertDescription);
            tvTimestamp = itemView.findViewById(R.id.tvAlertTimestamp);
            ivIcon = itemView.findViewById(R.id.ivAlertIcon);
            iconBadge = itemView.findViewById(R.id.layoutAlertIconBadge);
        }
    }
}
