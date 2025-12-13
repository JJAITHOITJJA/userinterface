package com.example.myapplication.presentation.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.data.home.FeedItem;
import com.example.myapplication.databinding.FragmentUserHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.List;

public class UserHomeFragment extends Fragment {
    private static final String TAG = "UserHomeFragment";
    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_USER_NAME = "user_name";

    private FragmentUserHomeBinding binding;
    private HomeFeedAdapter adapter;
    private List<FeedItem> allFeedItems = new ArrayList<>();
    private String currentCategory = "전체";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // 표시할 사용자 정보
    private String targetUserId;
    private String targetUserName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Arguments에서 userId와 userName 가져오기
        if (getArguments() != null) {
            targetUserId = getArguments().getString(ARG_USER_ID);
            targetUserName = getArguments().getString(ARG_USER_NAME);
        }

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUserHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initAdapter();
        setupSpinner();
        setupClickListeners();
        loadUserProfile();

        // 로딩 오버레이 표시
        binding.rvHomeFeed.setVisibility(View.GONE);
        binding.loadingOverlay.setVisibility(View.VISIBLE);

        loadBooksFromFirebase();
    }

    private void initAdapter() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        binding.rvHomeFeed.setLayoutManager(gridLayoutManager);

        adapter = new HomeFeedAdapter();
        adapter.setOnItemClickListener(this::onFeedItemClick);
        // 다른 사람의 피드이므로 롱클릭 리스너는 설정하지 않음

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
                // Do nothing
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
        // 뒤로가기 버튼
        binding.ivBack.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });
    }

    private void onFeedItemClick(FeedItem item) {
        // RecordDetailFragment로 이동
        Bundle bundle = new Bundle();
        bundle.putParcelable("feed_item", item);
        bundle.putString("user_id", targetUserId);
        bundle.putBoolean("is_other_user", true); // 다른 사람의 피드임을 표시

    }

    private void loadUserProfile() {
        if (targetUserId == null) {
            Log.w(TAG, "targetUserId가 null입니다");
            Toast.makeText(getContext(), "사용자 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firestore에서 사용자 정보 가져오기
        db.collection("users")
                .document(targetUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // nickname 필드 가져오기
                        String nickname = documentSnapshot.getString("nickname");
                        if (nickname != null && !nickname.isEmpty()) {
                            binding.tvUserName.setText(nickname);
                        } else if (targetUserName != null) {
                            binding.tvUserName.setText(targetUserName);
                        } else {
                            binding.tvUserName.setText("사용자");
                        }

                        // profileImageUrl 필드 가져오기
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            com.bumptech.glide.Glide.with(requireContext())
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.bookie)
                                    .error(R.drawable.bookie)
                                    .circleCrop()
                                    .into(binding.ivUserProfile);
                        } else {
                            binding.ivUserProfile.setImageResource(R.drawable.bookie);
                        }

                        Log.d(TAG, "사용자 프로필 로드 성공");
                    } else {
                        Log.w(TAG, "사용자 문서가 존재하지 않습니다");
                        if (targetUserName != null) {
                            binding.tvUserName.setText(targetUserName);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "사용자 프로필 로드 실패", e);
                    if (targetUserName != null) {
                        binding.tvUserName.setText(targetUserName);
                    }
                });
    }

    private void loadBooksFromFirebase() {
        if (targetUserId == null) {
            Log.w(TAG, "targetUserId가 null입니다");
            Toast.makeText(getContext(), "사용자 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show();
            binding.rvHomeFeed.setVisibility(View.VISIBLE);
            binding.loadingOverlay.setVisibility(View.GONE);
            return;
        }

        allFeedItems.clear();

        // Firestore에서 books 컬렉션 불러오기
        // 먼저 모든 책을 가져온 후 클라이언트 측에서 필터링
        db.collection("users")
                .document(targetUserId)
                .collection("books")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<FeedItem> tempList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            // isPublic 확인 - 공개된 책만 처리
                            Boolean isPublic = document.getBoolean("isPublic");
                            if (isPublic == null || !isPublic) {
                                continue; // 비공개 책은 건너뛰기
                            }

                            // Firestore 문서에서 데이터 가져오기
                            String isbn = document.getString("isbn");
                            String title = document.getString("title");
                            String author = document.getString("author");
                            String coverUrl = document.getString("cover");
                            String category = document.getString("category");
                            String status = document.getString("status");

                            CalendarDay calendarDay = null;
                            String lastRecordDateStr = document.getString("lastRecordDate");
                            if (lastRecordDateStr != null && !lastRecordDateStr.isEmpty()) {
                                try {
                                    String[] dateParts = lastRecordDateStr.split("-");
                                    if (dateParts.length == 3) {
                                        int year = Integer.parseInt(dateParts[0]);
                                        int month = Integer.parseInt(dateParts[1]);
                                        int day = Integer.parseInt(dateParts[2]);
                                        calendarDay = CalendarDay.from(year, month, day);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "날짜 파싱 오류: " + lastRecordDateStr, e);
                                }
                            }

                            // FeedItem 생성
                            FeedItem feedItem = new FeedItem(
                                    isbn != null ? isbn : document.getId(),
                                    title != null ? title : "제목 없음",
                                    author != null ? author : "저자 미상",
                                    coverUrl != null ? coverUrl : "",
                                    calendarDay,
                                    0,
                                    0,
                                    0,
                                    "",
                                    status != null ? status : "읽는중",
                                    category != null ? category : "문학",
                                    !isPublic
                            );

                            tempList.add(feedItem);
                        } catch (Exception e) {
                            Log.e(TAG, "문서 파싱 오류: " + document.getId(), e);
                        }
                    }

                    // lastRecordDate 기준으로 정렬 (최신순)
                    tempList.sort((item1, item2) -> {
                        CalendarDay date1 = item1.getDate();
                        CalendarDay date2 = item2.getDate();

                        if (date1 == null && date2 == null) return 0;
                        if (date1 == null) return 1;
                        if (date2 == null) return -1;

                        // 년도 비교
                        if (date2.getYear() != date1.getYear()) {
                            return date2.getYear() - date1.getYear();
                        }
                        // 월 비교
                        if (date2.getMonth() != date1.getMonth()) {
                            return date2.getMonth() - date1.getMonth();
                        }
                        // 일 비교
                        return date2.getDay() - date1.getDay();
                    });
                    allFeedItems.addAll(tempList);

                    Log.d(TAG, "불러온 책 개수: " + allFeedItems.size());
                    filterFeedItems("전체");

                    binding.rvHomeFeed.setVisibility(View.VISIBLE);
                    binding.loadingOverlay.setVisibility(View.GONE);

                    if (allFeedItems.isEmpty()) {
                        Toast.makeText(getContext(), "공개된 피드가 없습니다", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Books 불러오기 실패", e);
                    Toast.makeText(getContext(), "책 목록을 불러오는데 실패했습니다", Toast.LENGTH_SHORT).show();

                    binding.rvHomeFeed.setVisibility(View.VISIBLE);
                    binding.loadingOverlay.setVisibility(View.GONE);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}