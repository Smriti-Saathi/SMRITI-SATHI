package com.smritisathi.ui.memories;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.smritisathi.R;
import com.smritisathi.model.MemoryMoment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RecyclerView Adapter for MemoryMomentsActivity.
 * Features a dementia-friendly tap-to-reveal interaction:
 * Tapping a photo card reveals the loved one's name, relationship, and story.
 */
public class MemoryMomentsAdapter extends RecyclerView.Adapter<MemoryMomentsAdapter.ViewHolder> {

    private final List<MemoryMoment> moments;
    private final Set<Integer> revealedPositions = new HashSet<>();

    public MemoryMomentsAdapter(List<MemoryMoment> moments) {
        this.moments = moments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_memory_moment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MemoryMoment moment = moments.get(position);
        boolean isRevealed = revealedPositions.contains(position);

        // Load image using Glide with graceful fallback
        if (!TextUtils.isEmpty(moment.getImageUrl())) {
            Glide.with(holder.itemView.getContext())
                    .load(moment.getImageUrl())
                    .placeholder(R.drawable.ic_photo_library)
                    .error(R.drawable.ic_photo_library)
                    .centerCrop()
                    .into(holder.ivMemoryPhoto);
        } else {
            holder.ivMemoryPhoto.setImageResource(R.drawable.ic_photo_library);
        }

        // Tap-to-reveal toggle state
        if (isRevealed) {
            holder.layoutUnrevealed.setVisibility(View.GONE);
            holder.layoutRevealed.setVisibility(View.VISIBLE);

            holder.tvMemoryPersonName.setText(
                    !TextUtils.isEmpty(moment.getPersonName()) ? moment.getPersonName() : "Loved One"
            );

            String relationship = moment.getRelationship();
            if (!TextUtils.isEmpty(relationship)) {
                holder.tvMemoryRelationship.setVisibility(View.VISIBLE);
                holder.tvMemoryRelationship.setText(relationship);
            } else {
                holder.tvMemoryRelationship.setVisibility(View.GONE);
            }

            String description = moment.getDescription();
            if (!TextUtils.isEmpty(description)) {
                holder.tvMemoryDescription.setVisibility(View.VISIBLE);
                holder.tvMemoryDescription.setText(description);
            } else {
                holder.tvMemoryDescription.setVisibility(View.GONE);
            }
        } else {
            holder.layoutUnrevealed.setVisibility(View.VISIBLE);
            holder.layoutRevealed.setVisibility(View.GONE);
        }

        // Tap listener to toggle reveal state
        View.OnClickListener toggleListener = v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            if (revealedPositions.contains(currentPos)) {
                revealedPositions.remove(currentPos);
            } else {
                revealedPositions.add(currentPos);
            }
            notifyItemChanged(currentPos);
        };

        holder.cardMemoryMoment.setOnClickListener(toggleListener);
    }

    @Override
    public int getItemCount() {
        return moments != null ? moments.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardMemoryMoment;
        final ImageView ivMemoryPhoto;
        final LinearLayout layoutTapContainer;
        final LinearLayout layoutUnrevealed;
        final LinearLayout layoutRevealed;
        final TextView tvMemoryPersonName;
        final TextView tvMemoryRelationship;
        final TextView tvMemoryDescription;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardMemoryMoment = itemView.findViewById(R.id.cardMemoryMoment);
            ivMemoryPhoto = itemView.findViewById(R.id.ivMemoryPhoto);
            layoutTapContainer = itemView.findViewById(R.id.layoutTapContainer);
            layoutUnrevealed = itemView.findViewById(R.id.layoutUnrevealed);
            layoutRevealed = itemView.findViewById(R.id.layoutRevealed);
            tvMemoryPersonName = itemView.findViewById(R.id.tvMemoryPersonName);
            tvMemoryRelationship = itemView.findViewById(R.id.tvMemoryRelationship);
            tvMemoryDescription = itemView.findViewById(R.id.tvMemoryDescription);
        }
    }
}
