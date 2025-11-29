package com.example.myapplication.presentation.calendar;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.navigation.Navigation;
import androidx.navigation.NavController;

import com.example.myapplication.R;
import com.example.myapplication.data.home.FeedItem;
import com.example.myapplication.databinding.FragmentCalendarBinding;
import com.example.myapplication.presentation.MainActivity;
import com.example.myapplication.presentation.record.BookListFragment;
import com.example.myapplication.presentation.record.RecordDetailFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

public class CalendarFragment extends Fragment {

    private static final String TAG = "CalendarFragment";

    private FragmentCalendarBinding binding;
    private BookRecordAdapter bookAdapter;
    private List<FeedItem> allBookRecords;
    private List<FeedItem> displayedBookRecords;
    private MaterialCalendarView calendarView;
    private final HashSet<CalendarDay> bookDates = new HashSet<>();
    private BookDateDecorator bookDateDecorator;
    private String currentFilter = "all";

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private static final int DOT_SPAN_RADIUS = 6;

    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setupFragmentResultListener();

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupCalendar();
        setupRecyclerView();
        setupButtonListeners();
        setupFilterButtons();
        setupMenuButton();
        // Firebase에서 데이터 로드
        loadRecordsFromFirebase();

        CalendarDay today = CalendarDay.today();
        calendarView.setSelectedDate(today);
        filterRecordsByDate(today);
        if (getActivity() != null) {
            ((MainActivity) getActivity()).showBottom();
        }
    }

    private void setupFragmentResultListener() {
        getParentFragmentManager().setFragmentResultListener(
                "record_created",
                this,
                (requestKey, result) -> {
                    FeedItem newItem = result.getParcelable("new_feed_item");
                    if (newItem != null) {
                        addNewBookRecord(newItem);
                    }
                }
        );
    }

    private void setupMenuButton() {
        binding.ivMenuCalendar.setOnClickListener(v -> navigateToBookList());
    }

    private void navigateToBookList() {
        BookListFragment listFragment = new BookListFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("all_records", new ArrayList<>(allBookRecords));
        listFragment.setArguments(bundle);

        Fragment parentFragment = getParentFragment();
        if (parentFragment != null) {
            parentFragment.getChildFragmentManager().beginTransaction()
                    .replace(R.id.child_fragment_container, listFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void setupCalendar() {
        calendarView = binding.calendarView;
        calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE);

        Context context = requireContext();
        int weekendColor = ContextCompat.getColor(context, R.color.g6);
        int specialDayColor = ContextCompat.getColor(context, R.color.g6);

        bookDateDecorator = new BookDateDecorator(bookDates, specialDayColor);

        calendarView.addDecorators(
                new WeekdayDecorator(Calendar.SUNDAY, weekendColor),
                new WeekdayDecorator(Calendar.SATURDAY, weekendColor),
                new SpecialDayDecorator(CalendarDay.today(), specialDayColor),
                bookDateDecorator
        );

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            filterRecordsByDate(date);
        });
    }

    private void setupRecyclerView() {
        allBookRecords = new ArrayList<>();
        displayedBookRecords = new ArrayList<>();
        bookAdapter = new BookRecordAdapter(displayedBookRecords, this::onRecordClick);
        binding.bookRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.bookRecyclerView.setAdapter(bookAdapter);
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

    private void setupFilterButtons() {
        Button btnFilterAll = binding.btnFilterAll;
        Button btnFilterReading = binding.btnFilterReading;
        Button btnFilterFinished = binding.btnFilterFinished;

        updateFilterButtonState(btnFilterAll, true);
        updateFilterButtonState(btnFilterReading, false);
        updateFilterButtonState(btnFilterFinished, false);

        btnFilterAll.setOnClickListener(v -> {
            currentFilter = "all";
            updateFilterButtonState(btnFilterAll, true);
            updateFilterButtonState(btnFilterReading, false);
            updateFilterButtonState(btnFilterFinished, false);

            CalendarDay selectedDate = calendarView.getSelectedDate();
            if (selectedDate != null) {
                filterRecordsByDate(selectedDate);
            }
        });

        btnFilterReading.setOnClickListener(v -> {
            currentFilter = "reading";
            updateFilterButtonState(btnFilterAll, false);
            updateFilterButtonState(btnFilterReading, true);
            updateFilterButtonState(btnFilterFinished, false);

            CalendarDay selectedDate = calendarView.getSelectedDate();
            if (selectedDate != null) {
                filterRecordsByDate(selectedDate);
            }
        });

        btnFilterFinished.setOnClickListener(v -> {
            currentFilter = "finished";
            updateFilterButtonState(btnFilterAll, false);
            updateFilterButtonState(btnFilterReading, false);
            updateFilterButtonState(btnFilterFinished, true);

            CalendarDay selectedDate = calendarView.getSelectedDate();
            if (selectedDate != null) {
                filterRecordsByDate(selectedDate);
            }
        });
    }

    private void updateFilterButtonState(Button button, boolean isSelected) {
        if (isSelected) {
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.g6));
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        } else {
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.g6));
        }
    }

    private void updateUI() {
        boolean isEmpty = displayedBookRecords.isEmpty();

        // 현재 선택된 날짜에 기록이 있는지 확인
        CalendarDay selectedDate = calendarView.getSelectedDate();
        boolean hasRecordsOnDate = false;
        if (selectedDate != null) {
            for (FeedItem record : allBookRecords) {
                if (record.getDate() != null && record.getDate().equals(selectedDate)) {
                    hasRecordsOnDate = true;
                    break;
                }
            }
        }

        binding.greetingCard.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.bookRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.addRecordCard.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.filterContainer.setVisibility(hasRecordsOnDate ? View.VISIBLE : View.GONE);
    }

    public void setFilter(String filter) {
        this.currentFilter = filter;
        CalendarDay selectedDate = calendarView.getSelectedDate();
        if (selectedDate != null) {
            filterRecordsByDate(selectedDate);
        }
    }

    private void setupButtonListeners() {
        binding.btnAddRecord.setOnClickListener(v -> {
            CalendarDay selectedDate = calendarView.getSelectedDate();
            navigateToCreateRecord(selectedDate);
        });

        binding.btnAddMoreRecord.setOnClickListener(v -> {
            CalendarDay selectedDate = calendarView.getSelectedDate();
            navigateToCreateRecord(selectedDate);
        });
    }

    private void navigateToCreateRecord(CalendarDay selectedDate) {
        try {
            NavController navController = Navigation.findNavController(requireView());

            // Bundle로 선택된 날짜 전달
            Bundle bundle = new Bundle();
            if (selectedDate != null) {
                bundle.putInt("selected_year", selectedDate.getYear());
                bundle.putInt("selected_month", selectedDate.getMonth());
                bundle.putInt("selected_day", selectedDate.getDay());
            }

            navController.navigate(R.id.action_recordContainer_to_recordCreate, bundle);
        } catch (Exception e) {
            Toast.makeText(getContext(), "페이지 이동에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addNewBookRecord(FeedItem newItem) {
        allBookRecords.add(newItem);

        if (newItem.getDate() != null) {
            bookDates.add(newItem.getDate());
            calendarView.invalidateDecorators();
            calendarView.setSelectedDate(newItem.getDate());
            filterRecordsByDate(newItem.getDate());
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
        allBookRecords.clear();
        bookDates.clear();

        Log.d(TAG, "Firebase에서 기록 불러오기 시작");

        // 1단계: books 컬렉션 먼저 불러오기
        db.collection("users")
                .document(userId)
                .collection("books")
                .get()
                .addOnSuccessListener(booksSnapshot -> {
                    // books 정보를 Map에 저장
                    java.util.Map<String, java.util.Map<String, String>> booksMap = new java.util.HashMap<>();

                    for (QueryDocumentSnapshot bookDoc : booksSnapshot) {
                        String isbn = bookDoc.getId();
                        java.util.Map<String, String> bookData = new java.util.HashMap<>();
                        bookData.put("author", bookDoc.getString("author"));
                        bookData.put("status", bookDoc.getString("status"));
                        bookData.put("category", bookDoc.getString("category"));
                        booksMap.put(isbn, bookData);
                    }

                    Log.d(TAG, "불러온 책 개수: " + booksMap.size());

                    // 2단계: records 컬렉션 불러오기
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

                                        Log.d(TAG, "처리 중인 기록 - 제목: " + title + ", 날짜: " + dateString);

                                        // CalendarDay로 변환
                                        CalendarDay calendarDay = null;
                                        if (dateString != null && !dateString.isEmpty()) {
                                            try {
                                                // 점(.)과 하이픈(-) 모두 처리
                                                String[] dateParts = dateString.replace(".", "-").split("-");
                                                if (dateParts.length == 3) {
                                                    int year = Integer.parseInt(dateParts[0]);
                                                    int month = Integer.parseInt(dateParts[1]);
                                                    int day = Integer.parseInt(dateParts[2]);
                                                    calendarDay = CalendarDay.from(year, month, day);
                                                    bookDates.add(calendarDay);
                                                    Log.d(TAG, "날짜 변환 성공: " + year + "-" + month + "-" + day);
                                                }
                                            } catch (Exception e) {
                                                Log.e(TAG, "날짜 파싱 오류: " + dateString, e);
                                            }
                                        }

                                        // books 정보 가져오기
                                        String author = "저자 미상";
                                        String status = "읽는중";
                                        String category = "문학";

                                        if (isbn != null && booksMap.containsKey(isbn)) {
                                            java.util.Map<String, String> bookData = booksMap.get(isbn);
                                            author = bookData.get("author");
                                            status = bookData.get("status");
                                            category = bookData.get("category");

                                            if (author == null) author = "저자 미상";
                                            if (status == null) status = "읽는중";
                                            if (category == null) category = "문학";
                                        }

                                        // FeedItem 생성
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

                                        allBookRecords.add(feedItem);
                                        Log.d(TAG, "FeedItem 추가 완료: " + title);

                                    } catch (Exception e) {
                                        Log.e(TAG, "문서 파싱 오류: " + document.getId(), e);
                                    }
                                }

                                Log.d(TAG, "=== 최종 불러온 기록 개수: " + allBookRecords.size() + " ===");
                                Log.d(TAG, "=== 날짜 개수: " + bookDates.size() + " ===");

                                // UI 업데이트
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        // 캘린더 데코레이터 갱신
                                        calendarView.invalidateDecorators();

                                        // 현재 선택된 날짜 필터링
                                        CalendarDay selectedDate = calendarView.getSelectedDate();
                                        if (selectedDate != null) {
                                            Log.d(TAG, "현재 선택된 날짜로 필터링: " + selectedDate);
                                            filterRecordsByDate(selectedDate);
                                        } else {
                                            Log.w(TAG, "선택된 날짜가 없습니다");
                                        }
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

    private void filterRecordsByDate(CalendarDay date) {
        displayedBookRecords.clear();

        Log.d(TAG, "선택된 날짜: " + date);
        Log.d(TAG, "전체 기록 개수: " + allBookRecords.size());

        for (FeedItem record : allBookRecords) {
            if (record.getDate() != null) {
                Log.d(TAG, "기록 날짜: " + record.getDate() + ", 비교: " + record.getDate().equals(date));
            }

            if (record.getDate() != null && record.getDate().equals(date)) {
                if (currentFilter.equals("all") ||
                        (currentFilter.equals("reading") && record.getStatus().equals("읽는중")) ||
                        (currentFilter.equals("finished") && record.getStatus().equals("완독"))) {
                    displayedBookRecords.add(record);
                }
            }
        }

        Log.d(TAG, "필터링된 기록 개수: " + displayedBookRecords.size());

        bookAdapter.notifyDataSetChanged();
        updateUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Decorator 클래스들은 그대로 유지
    private static class WeekdayDecorator implements DayViewDecorator {
        private final int dayOfWeek;
        private final int color;

        public WeekdayDecorator(int dayOfWeek, int color) {
            this.dayOfWeek = dayOfWeek;
            this.color = color;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(day.getYear(), day.getMonth() - 1, day.getDay());
            return calendar.get(Calendar.DAY_OF_WEEK) == dayOfWeek;
        }

        @Override
        public void decorate(@NonNull DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(color));
        }
    }

    private static class SpecialDayDecorator implements DayViewDecorator {
        private final CalendarDay specificDay;
        private final int color;

        public SpecialDayDecorator(CalendarDay specificDay, int color) {
            this.specificDay = specificDay;
            this.color = color;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(specificDay);
        }

        @Override
        public void decorate(@NonNull DayViewFacade view) {
            view.addSpan(new StyleSpan(Typeface.BOLD));
            view.addSpan(new ForegroundColorSpan(color));
        }
    }

    private static class BookDateDecorator implements DayViewDecorator {
        private final Set<CalendarDay> dates;
        private final int dotColor;

        public BookDateDecorator(Set<CalendarDay> dates, int dotColor) {
            this.dates = dates;
            this.dotColor = dotColor;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(@NonNull DayViewFacade view) {
            view.addSpan(new DotSpan(DOT_SPAN_RADIUS, dotColor));
        }
    }
}