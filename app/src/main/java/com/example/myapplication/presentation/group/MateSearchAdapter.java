package com.example.myapplication.presentation.group;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DiffUtil;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.OnItemClickListener;
import com.example.myapplication.data.onmate.MateItem;
import androidx.recyclerview.widget.ListAdapter;
import de.hdodenhof.circleimageview.CircleImageView;

public class MateSearchAdapter extends ListAdapter<MateItem, MateSearchAdapter.MateSearchViewHolder> {

    private OnItemClickListener<MateItem> listener;

    public void setOnItemClickListener(OnItemClickListener<MateItem> listener){
        this.listener = listener;
    }

    public MateSearchAdapter() {
        super(new MateSearchAdapter.MateItemDiffCallback());
    }

    @Override
    public MateSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onmate, parent, false);
        return new MateSearchAdapter.MateSearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MateSearchViewHolder holder, int position) {
        MateItem item = getItem(position);
        holder.bind(item);

        // 아이템 전체 클릭 (메이트 추가)
        holder.itemView.setOnClickListener(view -> {
            if(listener != null){
                listener.onItemClick(getItem(holder.getAbsoluteAdapterPosition()), holder.getAbsoluteAdapterPosition());
            }
        });

        // 프로필 이미지 클릭 (해당 유저의 UserHomeFragment로 이동)
        holder.profileImage.setOnClickListener(view -> {
            MateItem clickedItem = getItem(holder.getAbsoluteAdapterPosition());
            navigateToUserHome(view, clickedItem);
        });
    }

    private void navigateToUserHome(View view, MateItem item) {
        Bundle bundle = new Bundle();
        bundle.putString("user_id", item.getUId());
        bundle.putString("user_name", item.getName());

        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_onMateSearchFragment_to_userHomeFragment, bundle);
    }

    private static class MateItemDiffCallback extends DiffUtil.ItemCallback<MateItem> {
        @Override
        public boolean areItemsTheSame(@NonNull MateItem oldItem, @NonNull MateItem newItem) {
            return oldItem.getUId().equals(newItem.getUId());
        }

        @Override
        @SuppressLint("DiffUtilEquals")
        public boolean areContentsTheSame(@NonNull MateItem oldItem, @NonNull MateItem newItem) {
            return oldItem.equals(newItem);
        }
    }

    public static class MateSearchViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView id;
        private final CircleImageView profileImage;

        public MateSearchViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_onmate_nickname);
            profileImage = itemView.findViewById(R.id.profile_image);
            id = itemView.findViewById(R.id.tv_onmate_id);
        }

        public void bind(MateItem item) {
            String imageUrl = item.getProfileImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.bookie)
                        .error(R.drawable.bookie)
                        .circleCrop()
                        .into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.bookie);
            }
            name.setText(item.getName());
            id.setText(item.getId());
        }
    }
}