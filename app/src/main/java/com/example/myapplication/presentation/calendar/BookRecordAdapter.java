package com.example.myapplication.presentation.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R; // 본인 R 패키지
import com.example.myapplication.data.calendar.BookRecord;

import java.util.List;

public class BookRecordAdapter extends RecyclerView.Adapter<BookRecordAdapter.BookViewHolder> {
    private final List<BookRecord> bookRecordList;

    public BookRecordAdapter(List<BookRecord> bookRecordList) {
        this.bookRecordList = bookRecordList;
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

        public void bind(BookRecord record) {
            tvBookTitle.setText(record.getTitle());
            tvBookAuthor.setText(record.getAuthor());
            tvBookPage.setText(record.getPage());
            tvBookQuote.setText(record.getQuote());
            ivBookCover.setImageResource(record.getBookCoverImageRes());
        }
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_record, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        holder.bind(bookRecordList.get(position));
    }

    @Override
    public int getItemCount() {
        return (bookRecordList != null) ? bookRecordList.size() : 0;
    }

    public void addItem(BookRecord record) {
        bookRecordList.add(record);
        notifyItemInserted(bookRecordList.size() - 1);
    }
}