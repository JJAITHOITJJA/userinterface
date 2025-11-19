package com.example.myapplication.presentation.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.search.Book;
import com.example.myapplication.databinding.ItemBookSearchResultBinding;

import java.util.List;

public class BookSearchResultAdapter extends RecyclerView.Adapter<BookSearchResultAdapter.BookViewHolder> {

    private final List<Book> bookList;
    private final OnBookClickListener onBookClickListener;

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public BookSearchResultAdapter(List<Book> bookList, OnBookClickListener listener) {
        this.bookList = bookList;
        this.onBookClickListener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookSearchResultBinding binding = ItemBookSearchResultBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new BookViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        boolean isLastitem = position == bookList.size() - 1;
        holder.bind(book, isLastitem);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        private final ItemBookSearchResultBinding binding;

        public BookViewHolder(@NonNull ItemBookSearchResultBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Book book, boolean isLastItem) {
            binding.tvBookSearchResultItemTitle.setText(book.getTitle());
            String info = book.getAuthor() + " / " + book.getPublisher();
            binding.tvBookSearchResultItemAuthor.setText(info);

            Glide.with(itemView.getContext())
                    .load(book.getImageUrl())
                    .placeholder(R.drawable.sayhello)
                    .error(R.drawable.sayhello)
                    .into(binding.ivItemAdminPartnerLocationSearchResultItemImage);

            binding.viewBookSearchResultItemDivider.setVisibility(
                    isLastItem ? View.GONE : View.VISIBLE
            );

            binding.getRoot().setOnClickListener(v -> {
                if (onBookClickListener != null) {
                    onBookClickListener.onBookClick(book);
                }
            });
        }
    }
}
