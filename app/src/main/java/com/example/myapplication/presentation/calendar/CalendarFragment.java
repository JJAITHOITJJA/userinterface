package com.example.myapplication.presentation.calendar;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.data.calendar.BookRecord;
import com.example.myapplication.databinding.FragmentCalendarBinding;
import com.example.myapplication.presentation.record.BookListFragment;
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

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private BookRecordAdapter bookAdapter;
    private List<BookRecord> bookRecordList;
    private MaterialCalendarView calendarView;
    private final HashSet<CalendarDay> bookDates = new HashSet<>();
    private BookDateDecorator bookDateDecorator;

    // DotSpan 크기 상수
    private static final int DOT_SPAN_RADIUS = 6;
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
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
        updateUI();
    }

    // 캘린더 설정
    private void setupCalendar() {
        calendarView = binding.calendarView;
        Context context = requireContext();
        int weekendColor = ContextCompat.getColor(context, R.color.g6); // colors.xml에 정의
        int specialDayColor = ContextCompat.getColor(context, R.color.g6); // colors.xml에 정의

        bookDateDecorator = new BookDateDecorator(bookDates, specialDayColor);

        calendarView.addDecorators(
                new WeekdayDecorator(Calendar.SUNDAY, weekendColor),
                new WeekdayDecorator(Calendar.SATURDAY, weekendColor),
                new SpecialDayDecorator(CalendarDay.today(), specialDayColor),
                bookDateDecorator
        );

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            String text = (selected ? "" : "해제 ") + date.getYear() + "/" + date.getMonth() + "/" + date.getDay();
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
        });
    }

    // RecyclerView 초기 설정
    private void setupRecyclerView() {
        bookRecordList = new ArrayList<>();
        bookAdapter = new BookRecordAdapter(bookRecordList);
        binding.bookRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.bookRecyclerView.setAdapter(bookAdapter);
    }

    private void setupButtonListeners() {
        binding.btnAddRecord.setOnClickListener(v -> addNewBookRecord());
        binding.btnAddMoreRecord.setOnClickListener(v -> addNewBookRecord());
    }

    private void addNewBookRecord() {
        BookRecord newRecord = new BookRecord(
                "새로 추가된 책", "저자명", "p. 1 - 10",
                "새로운 인용구입니다...", R.drawable.ic_launcher_background
        );

        bookAdapter.addItem(newRecord);

        // 예시: 오늘 날짜를 추가 (나중에 BookRecord에 날짜를 넣으면 해당 날짜로 추가)
        bookDates.add(CalendarDay.today());
        calendarView.invalidateDecorators(); // Decorator 갱신 요청

        updateUI();
        binding.bookRecyclerView.smoothScrollToPosition(bookAdapter.getItemCount() - 1);
    }

    private void updateUI() {
        boolean isEmpty = bookRecordList.isEmpty();
        binding.greetingCard.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.bookRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.addRecordCard.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /** 요일(일/토 등) 색상 적용 데코레이터 */
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

    /** 특정 날짜 강조(볼드 + 색상) */
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

    /** 책이 있는 날짜를 점으로 표시하는 데코레이터 */
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
