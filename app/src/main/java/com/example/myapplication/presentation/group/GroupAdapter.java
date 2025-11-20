package com.example.myapplication.presentation.group;

import androidx.recyclerview.widget.ListAdapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.OnItemClickListener;
import com.example.myapplication.data.group.GroupItem;


public class GroupAdapter extends ListAdapter<GroupItem, GroupAdapter.GroupViewHolder> {

    private OnItemClickListener<GroupItem> listener= null;

    public GroupAdapter(OnItemClickListener<GroupItem> listener) { // 💡 생성자에서 리스너를 받음
        super(new GroupItemDiffCallback());
        this.listener = listener;
    }
    public GroupAdapter() {
        super(new GroupItemDiffCallback());
    }

    private static class GroupItemDiffCallback extends DiffUtil.ItemCallback<GroupItem> {
        @Override
        public boolean areItemsTheSame(@NonNull GroupItem oldItem, @NonNull GroupItem newItem) {
            return oldItem.getName().equals(newItem.getName());
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull GroupItem oldItem, @NonNull GroupItem newItem) {
            return oldItem.equals(newItem);
        }
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnail;
        private final TextView name;
        private final ImageView lockIcon;
        private final TextView startDate;
        private final TextView description;
        private final ImageView tag;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.iv_group_thumbnail);
            name = itemView.findViewById(R.id.tv_group_name);
            lockIcon = itemView.findViewById(R.id.iv_group_lock);
            startDate = itemView.findViewById(R.id.tv_group_startdate);
            description = itemView.findViewById(R.id.tv_group_description);
            tag = itemView.findViewById(R.id.iv_group_tag);
        }

        public void bind(GroupItem item) {
            String imageUrl = item.getThumbnailUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.color.g2)
                        .error(R.color.g2)
                        .into(thumbnail);
            } else {
                thumbnail.setImageResource(R.color.g2);
            }
            name.setText(item.getName());
            lockIcon.setVisibility(item.getIsLocked() ? View.VISIBLE : View.GONE);
            startDate.setText(item.getStartDate());
            description.setText(item.getDescription());

            if(item.getIsLiterature()) tag.setImageResource(R.drawable.tag_literature);
            else tag.setImageResource(R.drawable.tag_non_literature);


        }

    }


    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        GroupItem item = getItem(position);
        holder.bind(item);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onItemClick(getItem(holder.getAdapterPosition()), holder.getAdapterPosition());
            }
        });
    }
}