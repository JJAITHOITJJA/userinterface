package com.example.myapplication.presentation.search;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.data.search.Book;
import com.example.myapplication.databinding.FragmentBookSearchSuccessBinding;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class BookSearchSuccessFragment extends Fragment {

    private FragmentBookSearchSuccessBinding binding;
    private BookSearchResultAdapter adapter;
    private ArrayList<Book> bookList = new ArrayList<>();
    private NavController navController;
    private OnBookSelectedListener onBookSelectedListener;

    private static final String ARG_BOOK_LIST = "book_list";

    public interface OnBookSelectedListener {
        void onBookSelected(Book book);
    }

    public static BookSearchSuccessFragment newInstance(ArrayList<Book> books) {
        BookSearchSuccessFragment fragment = new BookSearchSuccessFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_BOOK_LIST, books);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnBookSelectedListener(OnBookSelectedListener listener) {
        this.onBookSelectedListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookList = getArguments().getParcelableArrayList(ARG_BOOK_LIST);
            if (bookList == null) {
                bookList = new ArrayList<>();
            }
        }

    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentBookSearchSuccessBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        updateSearchCount(bookList.size());
    }

    private void setupRecyclerView() {
        adapter = new BookSearchResultAdapter(bookList, book -> {
            if (onBookSelectedListener != null) {
                onBookSelectedListener.onBookSelected(book);
            }
        });

        binding.rvLocationSearchSuccess.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvLocationSearchSuccess.setAdapter(adapter);
    }
    private void updateSearchCount(int count) {
        String countText = count + "개의 검색 결과가 있습니다";
        SpannableString spannable = new SpannableString(countText);

        String countString = String.valueOf(count);
        spannable.setSpan(
                new ForegroundColorSpan(
                        ContextCompat.getColor(requireContext(), R.color.font_main)
                ),
                0,
                countString.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        binding.tvSearchSuccessCount.setText(countString);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
