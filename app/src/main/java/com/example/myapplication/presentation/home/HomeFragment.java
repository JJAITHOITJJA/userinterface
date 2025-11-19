package com.example.myapplication.presentation.home;

import android.content.Intent;
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.data.home.FeedItem;
import com.example.myapplication.databinding.FragmentHomeBinding;
import com.example.myapplication.presentation.MainActivity;
import com.example.myapplication.presentation.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private FragmentHomeBinding binding;
    private HomeFeedAdapter adapter;
    private List<FeedItem> allFeedItems = new ArrayList<>();
    private String  currentCategory = "전체";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

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

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initAdapter();
        setupSpinner();
        setupClickListeners();
        setupRecordCreationListener();
        loadUserProfile();
        loadBooksFromFirebase();
    }

    private void initAdapter() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        binding.rvHomeFeed.setLayoutManager(gridLayoutManager);

        adapter = new HomeFeedAdapter();
        adapter.setOnItemClickListener(this::onFeedItemClick);
        adapter.setOnItemLongClickListener(this::onFeedItemLongClick);

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
            showLogoutDialog();
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

    private void onFeedItemLongClick(FeedItem item) {
        showDeleteDialog(item);
    }

    private void showDeleteDialog(FeedItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("피드 삭제")
                .setMessage("현재 피드를 삭제하시겠습니까?\n해당 책의 모든 기록이 삭제됩니다.")
                .setPositiveButton("예", (dialog, which) -> {
                    deleteFeedItem(item);
                })
                .setNegativeButton("아니오", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void deleteFeedItem(FeedItem item) {
        // 현재 사용자 확인
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String isbn = item.getId(); // FeedItem의 id가 isbn

        // 1. books 컬렉션에서 해당 isbn 문서 삭제
        db.collection("users")
                .document(userId)
                .collection("books")
                .document(isbn)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Book 삭제 성공: " + isbn);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Book 삭제 실패", e);
                });

        // 2. records 컬렉션에서 해당 isbn을 가진 모든 문서 삭제
        db.collection("users")
                .document(userId)
                .collection("records")
                .whereEqualTo("isbn", isbn)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Record 삭제 성공: " + document.getId());
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Record 삭제 실패", e);
                                });
                    }

                    // UI에서 아이템 제거
                    removeItemFromList(item);
                    Toast.makeText(getContext(), "피드가 삭제되었습니다", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Records 조회 실패", e);
                    Toast.makeText(getContext(), "삭제에 실패했습니다", Toast.LENGTH_SHORT).show();
                });
    }

    private void removeItemFromList(FeedItem item) {
        // allFeedItems에서 제거
        allFeedItems.remove(item);

        // 현재 필터링된 리스트 업데이트
        filterFeedItems(currentCategory);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("확인", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void performLogout() {
        // Firebase 로그아웃
        auth.signOut();

        // LoginActivity로 이동
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // 현재 Activity 종료
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void loadUserProfile() {
        // 현재 사용자 확인
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "사용자가 로그인되어 있지 않습니다");
            return;
        }

        String userId = currentUser.getUid();

        // Firestore에서 사용자 정보 가져오기
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // nickname 필드 가져오기
                        String nickname = documentSnapshot.getString("nickname");
                        if (nickname != null && !nickname.isEmpty()) {
                            binding.tvUserName.setText(nickname);
                        } else {
                            // nickname이 없으면 displayName 또는 이메일 사용
                            String displayName = documentSnapshot.getString("displayName");
                            if (displayName != null && !displayName.isEmpty()) {
                                binding.tvUserName.setText(displayName);
                            } else if (currentUser.getEmail() != null) {
                                binding.tvUserName.setText(currentUser.getEmail());
                            }
                        }

                        // profileImageUrl 필드 가져오기
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            com.bumptech.glide.Glide.with(requireContext())
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .error(R.drawable.ic_launcher_background)
                                    .circleCrop()
                                    .into(binding.ivUserProfile);
                        } else {
                            // profileImageUrl이 없으면 기본 이미지 사용
                            binding.ivUserProfile.setImageResource(R.drawable.ic_launcher_background);
                        }

                        Log.d(TAG, "사용자 프로필 로드 성공");
                    } else {
                        Log.w(TAG, "사용자 문서가 존재하지 않습니다");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "사용자 프로필 로드 실패", e);
                });
    }

    private void loadBooksFromFirebase() {
        // 현재 사용자 확인
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "사용자가 로그인되어 있지 않습니다");
            Toast.makeText(getContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        allFeedItems.clear();

        // Firestore에서 books 컬렉션 불러오기 (lastRecordDate 기준 내림차순 정렬)
        db.collection("users")
                .document(userId)
                .collection("books")
                .orderBy("lastRecordDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            // Firestore 문서에서 데이터 가져오기
                            String isbn = document.getString("isbn");
                            String title = document.getString("title");
                            String author = document.getString("author");
                            String coverUrl = document.getString("cover");
                            String category = document.getString("category");
                            String status = document.getString("status");
                            Boolean isPublic = document.getBoolean("isPublic");
                            String lastRecordDate = document.getString("lastRecordDate");

                            // FeedItem 생성
                            FeedItem feedItem = new FeedItem(
                                    isbn != null ? isbn : document.getId(),
                                    title != null ? title : "제목 없음",
                                    author != null ? author : "저자 미상",
                                    coverUrl != null ? coverUrl : "",
                                    lastRecordDate != null ? lastRecordDate : "",
                                    0, // rating - book에는 없으므로 기본값
                                    0, // startPage
                                    0, // endPage
                                    "", // review
                                    status != null ? status : "읽는중",
                                    category != null ? category : "문학",
                                    isPublic != null && !isPublic // isPrivate = !isPublic
                            );

                            allFeedItems.add(feedItem);
                        } catch (Exception e) {
                            Log.e(TAG, "문서 파싱 오류: " + document.getId(), e);
                        }
                    }

                    Log.d(TAG, "불러온 책 개수: " + allFeedItems.size());
                    // 초기에는 전체 항목 표시
                    filterFeedItems("전체");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Books 불러오기 실패", e);
                    Toast.makeText(getContext(), "책 목록을 불러오는데 실패했습니다", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
