package com.example.myapplication.presentation.record;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.data.home.FeedItem;
import com.example.myapplication.databinding.FragmentBookListBinding;
import com.example.myapplication.presentation.MainActivity;
import com.example.myapplication.presentation.calendar.BookRecordAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookListFragment extends Fragment {

    private FragmentBookListBinding binding;
    private BookRecordAdapter readingAdapter;
    private BookRecordAdapter finishedAdapter;
    private List<FeedItem> allBookRecords;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBookListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).hideBottom();

        setupCalendarButton();  // 달력 버튼 설정
        setupRecyclerViews();
        loadAllRecords();
    }

    // 달력 버튼 설정
    private void setupCalendarButton() {
        binding.ivCalendarIconList.setOnClickListener(v -> {
            // CalendarFragment로 돌아가기
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setupRecyclerViews() {
        // 읽는 중 RecyclerView
        readingAdapter = new BookRecordAdapter(new ArrayList<>(), this::onRecordClick);
        binding.rvReadingBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvReadingBooks.setAdapter(readingAdapter);

        // 완독한 책 RecyclerView
        finishedAdapter = new BookRecordAdapter(new ArrayList<>(), this::onRecordClick);
        binding.rvFinishedBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFinishedBooks.setAdapter(finishedAdapter);
    }

    private void loadAllRecords() {
        if (getArguments() != null) {
            allBookRecords = getArguments().getParcelableArrayList("all_records");
            if (allBookRecords != null) {
                filterAndDisplayRecords();
            }
        }
    }

    private void filterAndDisplayRecords() {
        List<FeedItem> readingBooks = new ArrayList<>();
        List<FeedItem> finishedBooks = new ArrayList<>();

        for (FeedItem item : allBookRecords) {
            if (item.getStatus().equals("읽는중")) {
                readingBooks.add(item);
            } else if (item.getStatus().equals("완독")) {
                finishedBooks.add(item);
            }
        }

        // 날짜순 정렬 (최신순)
        Collections.sort(readingBooks, (a, b) -> {
            if (a.getDate() == null || b.getDate() == null) return 0;
            return b.getDate().getDate().compareTo(a.getDate().getDate());
        });

        Collections.sort(finishedBooks, (a, b) -> {
            if (a.getDate() == null || b.getDate() == null) return 0;
            return b.getDate().getDate().compareTo(a.getDate().getDate());
        });

        updateReadingSection(readingBooks);
        updateFinishedSection(finishedBooks);
    }

    private void updateReadingSection(List<FeedItem> readingBooks) {
        if (readingBooks.isEmpty()) {
            binding.tvReadingSection.setVisibility(View.GONE);
            binding.rvReadingBooks.setVisibility(View.GONE);
        } else {
            binding.tvReadingSection.setVisibility(View.VISIBLE);
            binding.rvReadingBooks.setVisibility(View.VISIBLE);

            readingAdapter = new BookRecordAdapter(readingBooks, this::onRecordClick);
            binding.rvReadingBooks.setAdapter(readingAdapter);
        }
    }

    private void updateFinishedSection(List<FeedItem> finishedBooks) {
        if (finishedBooks.isEmpty()) {
            binding.tvFinishedSection.setVisibility(View.GONE);
            binding.rvFinishedBooks.setVisibility(View.GONE);
        } else {
            binding.tvFinishedSection.setVisibility(View.VISIBLE);
            binding.rvFinishedBooks.setVisibility(View.VISIBLE);

            finishedAdapter = new BookRecordAdapter(finishedBooks, this::onRecordClick);
            binding.rvFinishedBooks.setAdapter(finishedAdapter);
        }
    }

    private void onRecordClick(FeedItem item) {
        RecordDetailFragment detailFragment = new RecordDetailFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable("feed_item", item);
        detailFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}