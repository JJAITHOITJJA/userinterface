package com.example.myapplication.presentation.group;

import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.data.group.GroupItem;
import com.example.myapplication.databinding.FragmentGroupBinding;

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment {

    FragmentGroupBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentGroupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    // binding.button.setOnClickListener 같은 작업 여기서 처리할 것
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = NavHostFragment.findNavController(this);

        initAdapter();
        binding.fabAdd.setOnClickListener(v -> {
            navController.navigate(R.id.action_groupFragment_to_groupSearchFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initAdapter(){
        GroupAdapter adapter = new GroupAdapter();
        binding.rvMygroupList.setAdapter(adapter);
        binding.rvMygroupList.setLayoutManager(new LinearLayoutManager(getContext()));

        List<GroupItem> dummyData = new ArrayList<>();
        dummyData.add(new GroupItem(
                R.drawable.sayhello,
                "짜잇호잇짜!! 독서팀",
                false,
                "2025.04.08",
                "송실대학교 컴퓨터학부 24학번 동기들끼리 하는 독서모임입니다~",
                "문학"
        ));

        // 2. 스터디 그룹 (잠금 O)
        dummyData.add(new GroupItem(
                R.drawable.sayhello,
                "코딩 테스트 스터디",
                true,
                "2025.05.15",
                "취업을 목표로 알고리즘 문제를 주 2회 풀고 토론하는 스터디 그룹입니다.",
                "비문학"
        ));

        // 3. 운동 모임 (잠금 X)
        dummyData.add(new GroupItem(
                R.drawable.sayhello,
                "주말 농구 크루",
                false,
                "2025.03.01",
                "매주 토요일 오전 10시, 캠퍼스 체육관에서 농구하는 모임입니다.",
                "문학" // 임시 태그
        ));

        // 4. 영화 감상 모임 (잠금 X)
        dummyData.add(new GroupItem(
                R.drawable.sayhello,
                "월간 영화 브레이커",
                false,
                "2025.01.01",
                "한 달에 한 번 만나서 영화를 보고 심층적으로 분석하고 이야기합니다.",
                "비문학"
        ));

        // 5. 프로젝트 팀 (잠금 O, 긴 이름)
        dummyData.add(new GroupItem(
                R.drawable.sayhello,
                "AI 기반 스마트 라이프스타일 앱 개발 프로젝트",
                true,
                "2025.06.20",
                "캡스톤 디자인을 위한 팀으로, MVP 구현 및 발표를 목표로 합니다. 열정적인 팀원 환영!",
                "문학"
        ));

        adapter.submitList(dummyData);
    }
}
