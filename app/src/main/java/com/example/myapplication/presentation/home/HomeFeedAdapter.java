package com.example.myapplication.presentation.home;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.home.FeedItem;

public class HomeFeedAdapter extends ListAdapter<FeedItem, HomeFeedAdapter.FeedViewHolder> {

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(FeedItem item);
    }

    public HomeFeedAdapter(){super(new FeedItemDiffCallback());}

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    private static class FeedItemDiffCallback extends DiffUtil.ItemCallback<FeedItem> {

        @Override
        public boolean areItemsTheSame(@NonNull FeedItem oldItem, @NonNull FeedItem newItem) {
            return oldItem.getTitle().equals(newItem.getTitle());
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull FeedItem oldItem, @NonNull FeedItem newItem) {
            return oldItem.equals(newItem);
        }
    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        private final ImageView coverImage;
        private final TextView title;
        private final TextView author;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImage = itemView.findViewById(R.id.iv_home_book_cover);
            title = itemView.findViewById(R.id.tv_home_book_title);
            author = itemView.findViewById(R.id.tv_home_book_author);
        }

        public void bind(FeedItem item, OnItemClickListener listener) {
            if (item.getCoverImageUrl() != null && !item.getCoverImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getCoverImageUrl())
                        .placeholder(R.drawable.sayhello)
                        .error(R.drawable.sayhello)
                        .into(coverImage);
            } else if (item.getCoverImage() != 0) {
                coverImage.setImageResource(item.getCoverImage());
            }

            title.setText(item.getTitle());
            author.setText(item.getAuthor());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feed, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        FeedItem item = getItem(position);
        holder.bind(item, onItemClickListener);
    }
}
