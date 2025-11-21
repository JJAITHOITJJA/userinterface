package com.example.myapplication.presentation.record;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.data.home.FeedItem;
import com.example.myapplication.databinding.FragmentBookListBinding;
import com.example.myapplication.presentation.calendar.BookRecordAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookListFragment extends Fragment {

    private static final String TAG = "BookListFragment";

    private FragmentBookListBinding binding;
    private BookRecordAdapter readingAdapter;
    private BookRecordAdapter finishedAdapter;
    private List<FeedItem> readingRecords;
    private List<FeedItem> finishedRecords;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

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

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupRecyclerViews();
        setupCalendarButton();
        loadRecordsFromFirebase();
    }

    private void setupRecyclerViews() {
        readingRecords = new ArrayList<>();
        finishedRecords = new ArrayList<>();

        readingAdapter = new BookRecordAdapter(readingRecords, this::onRecordClick);
        binding.rvReadingBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvReadingBooks.setAdapter(readingAdapter);

        finishedAdapter = new BookRecordAdapter(finishedRecords, this::onRecordClick);
        binding.rvFinishedBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFinishedBooks.setAdapter(finishedAdapter);
    }

    private void setupCalendarButton() {
        binding.ivCalendarIconList.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void onRecordClick(FeedItem item) {
        RecordDetailFragment detailFragment = new RecordDetailFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable("feed_item", item);
        detailFragment.setArguments(bundle);

        Fragment parentFragment = getParentFragment();
        if (parentFragment != null) {
            parentFragment.getChildFragmentManager().beginTransaction()
                    .replace(R.id.child_fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void loadRecordsFromFirebase() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "사용자가 로그인되어 있지 않습니다");
            Toast.makeText(getContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        readingRecords.clear();
        finishedRecords.clear();

        Log.d(TAG, "Firebase에서 전체 기록 불러오기 시작");

        db.collection("users")
                .document(userId)
                .collection("books")
                .get()
                .addOnSuccessListener(booksSnapshot -> {
                    Map<String, Map<String, String>> booksMap = new HashMap<>();

                    for (QueryDocumentSnapshot bookDoc : booksSnapshot) {
                        String isbn = bookDoc.getId();
                        Map<String, String> bookData = new HashMap<>();
                        bookData.put("author", bookDoc.getString("author"));
                        bookData.put("status", bookDoc.getString("status"));
                        bookData.put("category", bookDoc.getString("category"));
                        booksMap.put(isbn, bookData);
                    }

                    Log.d(TAG, "불러온 책 개수: " + booksMap.size());

                    db.collection("users")
                            .document(userId)
                            .collection("records")
                            .orderBy("createdAt", Query.Direction.DESCENDING)
                            .get()
                            .addOnSuccessListener(recordsSnapshot -> {
                                Log.d(TAG, "불러온 기록 문서 개수: " + recordsSnapshot.size());

                                for (QueryDocumentSnapshot document : recordsSnapshot) {
                                    try {
                                        String recordId = document.getId();
                                        String isbn = document.getString("isbn");
                                        String title = document.getString("title");
                                        String coverUrl = document.getString("cover");
                                        String dateString = document.getString("date");

                                        Long startPageLong = document.getLong("startPage");
                                        Long endPageLong = document.getLong("endPage");
                                        Long ratingLong = document.getLong("rating");
                                        String review = document.getString("review");
                                        Boolean isPublic = document.getBoolean("isPublic");

                                        int startPage = startPageLong != null ? startPageLong.intValue() : 0;
                                        int endPage = endPageLong != null ? endPageLong.intValue() : 0;
                                        int rating = ratingLong != null ? ratingLong.intValue() : 0;

                                        CalendarDay calendarDay = null;
                                        if (dateString != null && !dateString.isEmpty()) {
                                            try {
                                                String[] dateParts = dateString.replace(".", "-").split("-");
                                                if (dateParts.length == 3) {
                                                    int year = Integer.parseInt(dateParts[0]);
                                                    int month = Integer.parseInt(dateParts[1]);
                                                    int day = Integer.parseInt(dateParts[2]);
                                                    calendarDay = CalendarDay.from(year, month, day);
                                                }
                                            } catch (Exception e) {
                                                Log.e(TAG, "날짜 파싱 오류: " + dateString, e);
                                            }
                                        }

                                        String author = "저자 미상";
                                        String status = "읽는중";
                                        String category = "문학";

                                        if (isbn != null && booksMap.containsKey(isbn)) {
                                            Map<String, String> bookData = booksMap.get(isbn);
                                            author = bookData.get("author");
                                            status = bookData.get("status");
                                            category = bookData.get("category");

                                            if (author == null) author = "저자 미상";
                                            if (status == null) status = "읽는중";
                                            if (category == null) category = "문학";
                                        }

                                        FeedItem feedItem = new FeedItem(
                                                recordId,
                                                title != null ? title : "제목 없음",
                                                author,
                                                coverUrl != null ? coverUrl : "",
                                                calendarDay,
                                                rating,
                                                startPage,
                                                endPage,
                                                review != null ? review : "",
                                                status,
                                                category,
                                                isPublic != null && !isPublic
                                        );

                                        if (status.equals("읽는중")) {
                                            readingRecords.add(feedItem);
                                        } else if (status.equals("완독")) {
                                            finishedRecords.add(feedItem);
                                        }

                                    } catch (Exception e) {
                                        Log.e(TAG, "문서 파싱 오류: " + document.getId(), e);
                                    }
                                }

                                Log.d(TAG, "읽는 중: " + readingRecords.size());
                                Log.d(TAG, "완독한 책: " + finishedRecords.size());

                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        readingAdapter.notifyDataSetChanged();
                                        finishedAdapter.notifyDataSetChanged();
                                        updateEmptyState();
                                    });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Records 불러오기 실패", e);
                                Toast.makeText(getContext(), "기록을 불러오는데 실패했습니다", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Books 불러오기 실패", e);
                    Toast.makeText(getContext(), "책 정보를 불러오는데 실패했습니다", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateEmptyState() {
        binding.tvReadingSection.setVisibility(readingRecords.isEmpty() ? View.GONE : View.VISIBLE);
        binding.rvReadingBooks.setVisibility(readingRecords.isEmpty() ? View.GONE : View.VISIBLE);

        binding.tvFinishedSection.setVisibility(finishedRecords.isEmpty() ? View.GONE : View.VISIBLE);
        binding.rvFinishedBooks.setVisibility(finishedRecords.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}