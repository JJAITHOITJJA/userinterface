package com.example.myapplication.presentation.calendar;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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
import com.example.myapplication.presentation.record.RecordDetailFragment;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private BookRecordAdapter bookAdapter;
    private List<FeedItem> allBookRecords;
    private List<FeedItem> displayedBookRecords;
    private MaterialCalendarView calendarView;
    private final HashSet<CalendarDay> bookDates = new HashSet<>();
    private BookDateDecorator bookDateDecorator;
    private String currentFilter = "all"; // "all", "reading", "finished"

    private static final int DOT_SPAN_RADIUS = 6;

    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setupFragmentResultListener();
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
        setupFilterButtons(); // 필터 버튼 추가
        loadAllBookRecords();
        CalendarDay today = CalendarDay.today();
        calendarView.setSelectedDate(today);
        filterRecordsByDate(today);
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

        // 아이템 클릭 리스너와 함께 어댑터 생성
        bookAdapter = new BookRecordAdapter(displayedBookRecords, this::onRecordClick);

        binding.bookRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.bookRecyclerView.setAdapter(bookAdapter);
    }

    // 기록 클릭 시 상세보기로 이동
    private void onRecordClick(FeedItem item) {
        RecordDetailFragment detailFragment = new RecordDetailFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable("feed_item", item);
        detailFragment.setArguments(bundle);

        // Fragment 교체
        getParentFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    // 필터 버튼 설정
    private void setupFilterButtons() {
        // 필터 버튼들
        Button btnFilterAll = binding.btnFilterAll;
        Button btnFilterReading = binding.btnFilterReading;
        Button btnFilterFinished = binding.btnFilterFinished;

        // 초기 상태: 전체 선택
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

    // 필터 버튼 상태 업데이트
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
        boolean hasRecordsOnDate = !allBookRecords.isEmpty(); // 날짜에 기록이 있는지

        binding.greetingCard.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.bookRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.addRecordCard.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        // 기록이 있을 때만 필터 표시
        binding.filterCard.setVisibility(hasRecordsOnDate ? View.VISIBLE : View.GONE);
    }



    private void filterRecordsByDate(CalendarDay date) {
        displayedBookRecords.clear();

        for (FeedItem record : allBookRecords) {
            if (record.getDate() != null && record.getDate().equals(date)) {
                // 현재 필터에 따라 표시
                if (currentFilter.equals("all") ||
                        (currentFilter.equals("reading") && record.getStatus().equals("읽는중")) ||
                        (currentFilter.equals("finished") && record.getStatus().equals("완독"))) {
                    displayedBookRecords.add(record);
                }
            }
        }

        bookAdapter.notifyDataSetChanged();
        updateUI();
    }

    // 필터 변경 메서드
    public void setFilter(String filter) {
        this.currentFilter = filter;
        // 현재 선택된 날짜로 다시 필터링
        CalendarDay selectedDate = calendarView.getSelectedDate();
        if (selectedDate != null) {
            filterRecordsByDate(selectedDate);
        }
    }

    private void setupButtonListeners() {
        binding.btnAddRecord.setOnClickListener(v -> navigateToCreateRecord());
        binding.btnAddMoreRecord.setOnClickListener(v -> navigateToCreateRecord());
    }

    private void navigateToCreateRecord() {
        try {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_recordContainer_to_recordCreate);
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

    private void loadAllBookRecords() {
        CalendarDay today = CalendarDay.today();

        LocalDate yesterdayLocal = LocalDate.now().minusDays(1);
        CalendarDay yesterday = CalendarDay.from(yesterdayLocal);

        allBookRecords.add(new FeedItem(
                "1",
                "오늘 읽은 첫 책",
                "저자 A",
                "",
                today,
                5,
                1,
                50,
                "좋은 책이었습니다.",
                "읽는중",
                "문학",
                false
        ));

        allBookRecords.add(new FeedItem(
                "2",
                "오늘 읽은 두 번째 책",
                "저자 B",
                "",
                today,
                4,
                51,
                100,
                "흥미로운 내용입니다.",
                "완독",
                "비문학",
                false
        ));

        allBookRecords.add(new FeedItem(
                "3",
                "어제 읽은 책",
                "저자 C",
                "",
                yesterday,
                5,
                1,
                200,
                "감동적이었습니다.",
                "완독",
                "문학",
                false
        ));

        bookDates.add(today);
        bookDates.add(yesterday);
        calendarView.invalidateDecorators();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

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