package com.example.myapplication.presentation.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.data.home.FeedItem;
import com.example.myapplication.databinding.FragmentHomeBinding;
import com.example.myapplication.presentation.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeFeedAdapter adapter;
    private List<FeedItem> allFeedItems = new ArrayList<>();
    private String  currentCategory = "전체";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).showBottom();

        initAdapter();
        setupSpinner();
        setupClickListeners();
        setupRecordCreationListener();
        loadDummyData();
    }

    private void initAdapter() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        binding.rvHomeFeed.setLayoutManager(gridLayoutManager);

        adapter = new HomeFeedAdapter();
        adapter.setOnItemClickListener(item -> {
            onFeedItemClick(item);
        });

        binding.rvHomeFeed.setAdapter(adapter);
    }

    private void setupSpinner() {
        String[] categories = {"전체", "문학", "비문학"};

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(spinnerAdapter);

        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCategory = categories[position];
                filterFeedItems(currentCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void filterFeedItems(String category) {
        List<FeedItem> filteredList;

        if (category.equals("전체")) {
            filteredList = new ArrayList<>(allFeedItems);
        } else {
            filteredList = new ArrayList<>();
            for (FeedItem item : allFeedItems) {
                if (item.getCategory() != null && item.getCategory().equals(category)) {
                    filteredList.add(item);
                }
            }
        }

        adapter.submitList(filteredList);
    }

    private void setupClickListeners() {
        binding.ivAddRecord.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_homeFragment_to_recordCreateFragment);
        });

        binding.tvLogout.setOnClickListener(v -> {

        });
    }

    private void setupRecordCreationListener() {
        getParentFragmentManager().setFragmentResultListener(
                "record_created",
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    FeedItem newItem = result.getParcelable("new_feed_item");
                    if (newItem != null) {
                        addNewFeedItem(newItem);
                    }
                }
        );
    }

    private void addNewFeedItem(FeedItem newItem) {
        List<FeedItem> currentList = new ArrayList<>(adapter.getCurrentList());
        currentList.add(0, newItem);
        adapter.submitList(currentList);
        binding.rvHomeFeed.smoothScrollToPosition(0);
    }

    private void onFeedItemClick(FeedItem item) {
        // TODO: 피드 아이템 클릭 시 상세 페이지 이동
    }

    private void loadDummyData() {
        // 초기 더미 데이터 (카테고리 포함)
        allFeedItems.clear();

        FeedItem item1 = new FeedItem("1", "안녕이라 그랬어", "김애란", R.drawable.sayhello);
        item1.setCategory("문학");
        allFeedItems.add(item1);

        FeedItem item2 = new FeedItem("2", "채식주의자", "한강", R.drawable.sayhello);
        item2.setCategory("문학");
        allFeedItems.add(item2);

        FeedItem item3 = new FeedItem("3", "82년생 김지영", "조남주", R.drawable.sayhello);
        item3.setCategory("문학");
        allFeedItems.add(item3);

        FeedItem item4 = new FeedItem("4", "달러구트 꿈 백화점", "이미예", R.drawable.sayhello);
        item4.setCategory("문학");
        allFeedItems.add(item4);

        FeedItem item5 = new FeedItem("5", "트렌드 코리아 2024", "김난도", R.drawable.sayhello);
        item5.setCategory("비문학");
        allFeedItems.add(item5);

        FeedItem item6 = new FeedItem("6", "불편한 편의점", "김호연", R.drawable.sayhello);
        item6.setCategory("문학");
        allFeedItems.add(item6);

        // 초기에는 전체 항목 표시
        filterFeedItems("전체");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
