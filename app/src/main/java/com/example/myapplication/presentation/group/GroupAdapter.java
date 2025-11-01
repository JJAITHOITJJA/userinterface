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
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.group.GroupItem;

// 1. GroupItem을 위한 DiffUtil.ItemCallback 정의
public class GroupAdapter extends ListAdapter<GroupItem, GroupAdapter.GroupViewHolder> {

    public GroupAdapter() {
        super(new GroupItemDiffCallback());
    }

    private static class GroupItemDiffCallback extends DiffUtil.ItemCallback<GroupItem> {
        @Override
        public boolean areItemsTheSame(@NonNull GroupItem oldItem, @NonNull GroupItem newItem) {
            return oldItem.getName().equals(newItem.getName());
        }

        // 아이템의 내용이 동일한지(필드 값 비교) 확인
        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull GroupItem oldItem, @NonNull GroupItem newItem) {
            return oldItem.equals(newItem);
        }
    }

    // --- ViewHolder 구현 ---
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
            thumbnail.setImageResource(item.getThumbnailResId());
            name.setText(item.getName());
            lockIcon.setVisibility(item.isLocked() ? View.VISIBLE : View.GONE);
            startDate.setText(item.getStartDate());
            description.setText(item.getDescription());

            if(tag.equals("문학")) tag.setImageResource(R.drawable.tag_literature);
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
    }
}