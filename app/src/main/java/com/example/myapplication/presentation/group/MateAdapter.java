package com.example.myapplication.presentation.group;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.group.GroupItem;
import com.example.myapplication.data.onmate.MateItem;

public class MateAdapter extends ListAdapter<MateItem, MateAdapter.MateViewHolder> {

    public MateAdapter() {
        super(new MateItemDiffCallback());
    }

    private static class MateItemDiffCallback extends DiffUtil.ItemCallback<MateItem> {
        @Override
        public boolean areItemsTheSame(@NonNull MateItem oldItem, @NonNull MateItem newItem) {
            return oldItem.getName().equals(newItem.getName());
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull MateItem oldItem, @NonNull MateItem newItem) {
            return oldItem.equals(newItem);
        }
    }

    public static class MateViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnail;
        private final TextView name;
        private final TextView id;

        public MateViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.tv_onmate_nickname);
            id= itemView.findViewById(R.id.tv_onmate_id);

        }

        public void bind(MateItem item) {
            String imageUrl = item.getProfileImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.capibara)
                        .error(R.drawable.capibara)
                        .circleCrop()
                        .into(thumbnail);
            } else {
                thumbnail.setImageResource(R.drawable.capibara);
            }
            name.setText(item.getName());
            id.setText(item.getId());

        }
    }

    @NonNull
    @Override
    public MateAdapter.MateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new MateAdapter.MateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MateAdapter.MateViewHolder holder, int position) {
        MateItem item = getItem(position);
        holder.bind(item);
    }
}