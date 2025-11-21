package com.example.myapplication.presentation.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.home.FeedItem;

import java.util.List;

public class BookRecordAdapter extends RecyclerView.Adapter<BookRecordAdapter.BookViewHolder> {
    private List<FeedItem> displayList;
    private OnItemClickListener listener;

    // 클릭 리스너 인터페이스
    public interface OnItemClickListener {
        void onItemClick(FeedItem item);
    }

    public BookRecordAdapter(List<FeedItem> displayList, OnItemClickListener listener) {
        this.displayList = displayList;
        this.listener = listener;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBookCover;
        TextView tvBookTitle, tvBookAuthor, tvBookPage, tvBookQuote;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookAuthor = itemView.findViewById(R.id.tvBookAuthor);
            tvBookPage = itemView.findViewById(R.id.tvBookPage);
            tvBookQuote = itemView.findViewById(R.id.tvBookQuote);
        }

        public void bind(FeedItem record, OnItemClickListener listener) {
            tvBookTitle.setText(record.getTitle());
            tvBookAuthor.setText(record.getAuthor());

            String pageInfo = record.getStartPage() + " ~ " + record.getEndPage();
            tvBookPage.setText(pageInfo);

            tvBookQuote.setText(record.getReview());

            if (record.getCoverImageUrl() != null && !record.getCoverImageUrl().isEmpty()) {
                Glide.with(ivBookCover.getContext())
                        .load(record.getCoverImageUrl())
                        .error(R.drawable.sayhello)  // placeholder 제거
                        .into(ivBookCover);
            } else if (record.getCoverImage() != 0) {
                ivBookCover.setImageResource(record.getCoverImage());
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(record);
                }
            });
        }    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_record, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        holder.bind(displayList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return (displayList != null) ? displayList.size() : 0;
    }

    public void addItem(FeedItem record) {
        displayList.add(record);
        notifyItemInserted(displayList.size() - 1);
    }
}