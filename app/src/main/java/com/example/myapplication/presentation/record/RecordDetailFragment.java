package com.example.myapplication.presentation.record;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.home.FeedItem;
import com.example.myapplication.databinding.FragmentRecordDetailBinding;
import com.example.myapplication.presentation.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RecordDetailFragment extends Fragment {

    private static final String TAG = "RecordDetailFragment";

    private FragmentRecordDetailBinding binding;
    private FeedItem feedItem;
    private ImageView[] stars;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecordDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).hideBottom();  // 진입 시 숨김

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Bundle에서 FeedItem 받기
        if (getArguments() != null) {
            feedItem = getArguments().getParcelable("feed_item");
            if (feedItem != null) {
                displayRecordDetails();
            }
        }

        setupBackButton();
        setupEditButton();
        setupDeleteButton();
    }


    private void displayRecordDetails() {
        // 책 제목과 저자
        binding.tvDetailBookTitle.setText(feedItem.getTitle());
        binding.tvDetailBookAuthor.setText(feedItem.getAuthor());

        // 책 표지 이미지
        if (feedItem.getCoverImageUrl() != null && !feedItem.getCoverImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(feedItem.getCoverImageUrl())
                    .error(R.drawable.sayhello)
                    .into(binding.ivDetailBookCover);
        } else if (feedItem.getCoverImage() != 0) {
            binding.ivDetailBookCover.setImageResource(feedItem.getCoverImage());
        }

        // 날짜
        if (feedItem.getDate() != null) {
            Calendar cal = Calendar.getInstance();
            cal.set(feedItem.getDate().getYear(),
                    feedItem.getDate().getMonth() - 1,
                    feedItem.getDate().getDay());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault());
            binding.tvDetailDate.setText(sdf.format(cal.getTime()));
        }

        // 페이지
        String pageInfo = feedItem.getStartPage() + " ~ " + feedItem.getEndPage() + " 페이지";
        binding.tvDetailPage.setText(pageInfo);

        // 별점 표시
        initializeStars();
        displayRating(feedItem.getRating());

        // 감상평
        binding.tvDetailReview.setText(feedItem.getReview());

        // 상태 (읽는중/완독)
        binding.tvDetailStatus.setText(feedItem.getStatus());
        if (feedItem.getStatus().equals("완독")) {
            binding.tvDetailStatus.setTextColor(getResources().getColor(R.color.g6));
        } else {
            binding.tvDetailStatus.setTextColor(getResources().getColor(R.color.g4));
        }

        // 카테고리 (문학/비문학)
        binding.tvDetailCategory.setText(feedItem.getCategory());

        // 공개 여부
        binding.tvDetailPrivate.setText(feedItem.isPrivate() ? "비공개" : "공개");
    }

    private void initializeStars() {
        stars = new ImageView[]{
                binding.ivDetailStar1,
                binding.ivDetailStar2,
                binding.ivDetailStar3,
                binding.ivDetailStar4,
                binding.ivDetailStar5
        };
    }

    private void displayRating(int rating) {
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star_filled);
            } else {
                stars[i].setImageResource(R.drawable.ic_star_unfilled);
            }
        }
    }

    private void setupBackButton() {
        binding.ivBackDetail.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setupEditButton() {
        binding.btnEditRecord.setOnClickListener(v -> {
            // TODO: 수정 기능 구현
            Toast.makeText(getContext(), "수정 기능은 준비 중입니다", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupDeleteButton() {
        binding.btnDeleteRecord.setOnClickListener(v -> {
            showDeleteDialog();
        });
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("기록 삭제")
                .setMessage("이 기록을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    deleteRecord();
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void deleteRecord() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String recordId = feedItem.getId(); // record의 document ID

        // ⬅️ record에서 isbn 가져오기 (books 처리에 필요)
        db.collection("users")
                .document(userId)
                .collection("records")
                .document(recordId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String isbn = documentSnapshot.getString("isbn");

                        // 1. record 삭제
                        documentSnapshot.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Record 삭제 성공: " + recordId);

                                    // 2. 해당 isbn의 남은 records 확인 및 books 업데이트
                                    if (isbn != null) {
                                        checkAndUpdateBook(userId, isbn);
                                    }

                                    Toast.makeText(getContext(), "기록이 삭제되었습니다", Toast.LENGTH_SHORT).show();

                                    // 이전 화면으로 돌아가기
                                    if (getActivity() != null) {
                                        getActivity().onBackPressed();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Record 삭제 실패", e);
                                    Toast.makeText(getContext(), "삭제에 실패했습니다", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(getContext(), "기록을 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Record 조회 실패", e);
                    Toast.makeText(getContext(), "삭제에 실패했습니다", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkAndUpdateBook(String userId, String isbn) {
        // 해당 ISBN의 남은 records 개수 확인
        db.collection("users")
                .document(userId)
                .collection("records")
                .whereEqualTo("isbn", isbn)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // ⬅️ 남은 record가 없으면 book도 삭제
                        db.collection("users")
                                .document(userId)
                                .collection("books")
                                .document(isbn)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Book도 삭제됨 (남은 기록 없음): " + isbn);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Book 삭제 실패", e);
                                });
                    } else {
                        // ⬅️ 남은 record가 있으면 lastRecordDate와 status 업데이트
                        updateBookAfterDelete(userId, isbn, queryDocumentSnapshots);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Records 조회 실패", e);
                });
    }

    // ⬅️ 삭제 후 book 업데이트 (lastRecordDate, status)
    private void updateBookAfterDelete(String userId, String isbn,
                                       com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots) {
        String latestDate = "";
        String latestStatus = "읽는중";

        // 가장 최근 record 찾기
        for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
            String date = document.getString("date");

            if (date != null && date.compareTo(latestDate) > 0) {
                latestDate = date;
            }
        }

        // 남은 records 중에서 "완독" 상태 확인
        boolean hasFinished = false;
        for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
            // record에서 isbn으로 book의 status 확인
            // 편의상 남은 records가 있으면 status는 유지
        }

        if (!latestDate.isEmpty()) {
            final String finalDate = latestDate;

            // ⬅️ books 컬렉션에서 해당 isbn의 status 확인
            db.collection("users")
                    .document(userId)
                    .collection("books")
                    .document(isbn)
                    .get()
                    .addOnSuccessListener(bookDoc -> {
                        if (bookDoc.exists()) {
                            String currentStatus = bookDoc.getString("status");

                            // lastRecordDate만 업데이트 (status는 books에 저장된 값 유지)
                            java.util.Map<String, Object> updates = new java.util.HashMap<>();
                            updates.put("lastRecordDate", finalDate);

                            bookDoc.getReference().update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Book의 lastRecordDate 업데이트 성공: " + finalDate);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Book 업데이트 실패", e);
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Book 조회 실패", e);
                    });
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getActivity() != null) {
            ((MainActivity) getActivity()).showBottom();
        }

        binding = null;
    }
}