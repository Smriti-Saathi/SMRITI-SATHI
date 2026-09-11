package com.smritisathi.ui.reminders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.smritisathi.R;
import com.smritisathi.model.Reminder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for reminders with live Done and Remind Later actions.
 */
public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    public interface OnReminderActionListener {
        void onDone(Reminder reminder);
        void onRemindLater(Reminder reminder);
    }

    private final List<Reminder> reminderList;
    private final OnReminderActionListener actionListener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public ReminderAdapter(List<Reminder> reminderList, OnReminderActionListener actionListener) {
        this.reminderList = reminderList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminderList.get(position);

        String title = reminder.getTitle();
        if (title == null || title.isEmpty()) {
            title = reminder.getMedicineName() != null ? reminder.getMedicineName() : "Care Alert";
        }
        holder.tvTitle.setText(title);

        long time = reminder.getScheduledTime();
        if (time > 0) {
            holder.tvTime.setText("⏰ Scheduled for " + timeFormat.format(new Date(time)));
        } else {
            holder.tvTime.setText("⏰ Scheduled for today");
        }

        String medicine = reminder.getMedicineName() != null ? reminder.getMedicineName() : "";
        String dosage = reminder.getDosage() != null ? reminder.getDosage() : "";
        if (!medicine.isEmpty() || !dosage.isEmpty()) {
            holder.tvDosage.setVisibility(View.VISIBLE);
            holder.tvDosage.setText((medicine.isEmpty() ? "" : medicine) + (dosage.isEmpty() ? "" : " • " + dosage));
        } else {
            holder.tvDosage.setVisibility(View.GONE);
        }

        String status = reminder.getStatus() != null ? reminder.getStatus() : "Pending";
        holder.tvStatusBadge.setText(status);

        if ("Completed".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorSuccess, null));
            holder.layoutActions.setVisibility(View.GONE);
        } else if ("Remind Later".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorWarning, null));
            holder.layoutActions.setVisibility(View.VISIBLE);
        } else {
            holder.tvStatusBadge.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorWarning, null));
            holder.layoutActions.setVisibility(View.VISIBLE);
        }

        holder.btnDone.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDone(reminder);
            }
        });

        holder.btnRemindLater.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onRemindLater(reminder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvDosage, tvStatusBadge;
        LinearLayout layoutActions;
        MaterialButton btnDone, btnRemindLater;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvReminderItemTitle);
            tvTime = itemView.findViewById(R.id.tvReminderItemTime);
            tvDosage = itemView.findViewById(R.id.tvReminderItemDosage);
            tvStatusBadge = itemView.findViewById(R.id.tvReminderStatusBadge);
            layoutActions = itemView.findViewById(R.id.layoutReminderActions);
            btnDone = itemView.findViewById(R.id.btnDoneReminder);
            btnRemindLater = itemView.findViewById(R.id.btnRemindLater);
        }
    }
}
