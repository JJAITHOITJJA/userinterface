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

    private Toolbar toolbar;
    private Menu optionsMenu;
    private boolean isCalendarView = true; // 현재 캘린더 뷰인지 확인

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // "두 아이콘이 모두 포함된" 메뉴 XML을 인플레이트합니다.
        // (만약 파일 이름이 main_menu.xml 등 다르면 맞게 수정)
        inflater.inflate(R.menu.calendar_menu, menu);
        this.optionsMenu = menu;

        // 프래그먼트가 처음 로드될 때 현재 상태(isCalendarView)에 맞춰
        // 아이콘 가시성을 설정합니다.
        updateIconVisibility();

        super.onCreateOptionsMenu(menu, inflater);
    }

    // 3. 툴바 아이콘 클릭 이벤트를 처리하는 메서드 (새로 추가)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_view_list) {
            // "책 리스트 보기" 아이콘 클릭 시
            showBookListFragment(); // 리스트 뷰로 전환
            return true;
        } else if (id == R.id.action_view_calendar) {
            // "캘린더 보기" 아이콘 클릭 시
            showCalendarFragment(); // 캘린더 뷰로 전환
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

        toolbar = view.findViewById(R.id.toolbar);

        // Activity가 툴바를 제어하도록 설정 (중요)
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        // 처음 로드 시 캘린더 프래그먼트를 보여줌
        if (savedInstanceState == null) {
            showCalendarFragment();
        }
    }
    // 캘린더 자식 프래그먼트 표시
    private void showCalendarFragment() {
        // getChildFragmentManager() 사용 (중요)
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.child_fragment_container, new CalendarFragment());
        ft.commit();

        isCalendarView = true;
        updateIconVisibility(); // 아이콘 상태 업데이트
    }

    // 책 리스트 자식 프래그먼트 표시
    private void showBookListFragment() {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.child_fragment_container, new BookListFragment());
        ft.commit();

        isCalendarView = false;
        updateIconVisibility(); // 아이콘 상태 업데이트
    }

    // 툴바의 아이콘 가시성 업데이트
    private void updateIconVisibility() {
        if (optionsMenu == null) return;

        optionsMenu.findItem(R.id.action_view_list).setVisible(isCalendarView);
        optionsMenu.findItem(R.id.action_view_calendar).setVisible(!isCalendarView);
    }
}