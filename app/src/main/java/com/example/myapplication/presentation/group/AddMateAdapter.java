package com.example.myapplication.presentation.group;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View;

import com.example.myapplication.R;
import com.example.myapplication.data.group.GroupItem;
import com.example.myapplication.data.onmate.AddMateItem;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddMateAdapter extends ListAdapter<AddMateItem, AddMateAdapter.MateHorizontalViewHolder>   {

    private boolean isDeleteMode = false;
    public AddMateAdapter() {
        super(new AddMateAdapter.AddMateItemDiffCallback());
    }
    @NonNull
    @Override
    public MateHorizontalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_add_mate, parent, false);
        return new AddMateAdapter.MateHorizontalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MateHorizontalViewHolder holder, int position) {
        AddMateItem item = getItem(position);
        holder.bind(item, isDeleteMode);

    }
    public void setDeleteMode(boolean isDeleteMode) {
        this.isDeleteMode = isDeleteMode;
    }
    private static class AddMateItemDiffCallback extends DiffUtil.ItemCallback<AddMateItem> {
        @Override
        public boolean areItemsTheSame(@NonNull AddMateItem oldItem, @NonNull AddMateItem newItem) {
            return oldItem.getName().equals(newItem.getName());
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull AddMateItem oldItem, @NonNull AddMateItem newItem) {
            return oldItem.equals(newItem);
        }
    }
    public static class MateHorizontalViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final CircleImageView profileImage;

        public MateHorizontalViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_onmate_nickname);
            profileImage = itemView.findViewById(R.id.profile_image);
        }

        public void bind(AddMateItem item, boolean isDeleteMode) {
            profileImage.setImageResource(item.getProfileImageUrl());
            name.setText(item.getName());

            if(isDeleteMode){
                itemView.findViewById(R.id.btn_delete_mate).setVisibility(View.VISIBLE);
            }


        }


    }

}
