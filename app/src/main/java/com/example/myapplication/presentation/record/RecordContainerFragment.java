package com.example.myapplication.presentation.record;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.R;
import com.example.myapplication.presentation.calendar.CalendarFragment;

public class RecordContainerFragment extends Fragment {

    private Menu optionsMenu;
    private boolean isCalendarView = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.calendar_menu, menu);
        this.optionsMenu = menu;
        updateIconVisibility();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_view_list) {
            showBookListFragment();
            return true;
        } else if (id == R.id.action_view_calendar) {
            showCalendarFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record_container, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            showCalendarFragment();
        }
    }

    private void showCalendarFragment() {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.child_fragment_container, new CalendarFragment());
        ft.commit();

        isCalendarView = true;
        updateIconVisibility();
    }

    private void showBookListFragment() {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.child_fragment_container, new BookListFragment());
        ft.commit();

        isCalendarView = false;
        updateIconVisibility();
    }

    private void updateIconVisibility() {
        // optionsMenu가 null이면 아무것도 하지 않음 (중요!)
        if (optionsMenu == null) return;

        MenuItem listItem = optionsMenu.findItem(R.id.action_view_list);
        MenuItem calendarItem = optionsMenu.findItem(R.id.action_view_calendar);

        // 각 MenuItem이 null인지 체크 (추가 안전장치)
        if (listItem != null) {
            listItem.setVisible(isCalendarView);
        }
        if (calendarItem != null) {
            calendarItem.setVisible(!isCalendarView);
        }
    }
}