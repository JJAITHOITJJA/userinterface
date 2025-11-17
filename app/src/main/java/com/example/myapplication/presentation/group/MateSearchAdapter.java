package com.example.myapplication.presentation.group;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

        holder.itemView.setOnClickListener(view -> {
            if(listener != null){
                listener.onItemClick(getItem(holder.getAbsoluteAdapterPosition()) , holder.getAbsoluteAdapterPosition());
            }
        });
    }

    private static class MateItemDiffCallback extends DiffUtil.ItemCallback<MateItem> {
        @Override
        public boolean areItemsTheSame(@NonNull MateItem oldItem,@NonNull MateItem newItem) {
            return oldItem.getName().equals(newItem.getName());
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
                        .placeholder(R.drawable.capibara)
                        .error(R.drawable.capibara)
                        .circleCrop()
                        .into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.capibara);
            }
            name.setText(item.getName());
            id.setText(item.getId());
        }
    }

}
