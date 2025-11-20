package com.example.myapplication.presentation.record;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.home.FeedItem;
import com.example.myapplication.databinding.FragmentRecordDetailBinding;
import com.example.myapplication.presentation.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RecordDetailFragment extends Fragment {

    private FragmentRecordDetailBinding binding;
    private FeedItem feedItem;
    private ImageView[] stars;

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
        ((MainActivity) requireActivity()).hideBottom();

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
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_error)
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
            // RecordEditFragment로 이동
        });
    }

    private void setupDeleteButton() {
        binding.btnDeleteRecord.setOnClickListener(v -> {
            // TODO: 삭제 확인 다이얼로그 표시 후 삭제
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}