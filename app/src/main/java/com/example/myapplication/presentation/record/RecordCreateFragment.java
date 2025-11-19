package com.example.myapplication.presentation.record;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.data.home.FeedItem;
import com.example.myapplication.data.search.Book;
import com.example.myapplication.databinding.FragmentRecordCreateBinding;
import com.example.myapplication.presentation.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RecordCreateFragment extends Fragment {

    private static final String TAG = "RecordCreateFragment";

    private FragmentRecordCreateBinding binding;
    private Book selectedBook;
    private int currentRating = 0;
    private ImageView[] stars;
    private String selectedDate;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecordCreateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).hideBottom();

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initializeStars();
        setupBookSearchClickListeners();
        setupFragmentResultListener();
        setupDatePicker();
        setupStarRating();
        setupCheckBoxes();
        setupBackButton();
        setupConfirmButton();
        setCurrentDate();
    }

    private void initializeStars() {
        stars = new ImageView[]{
                binding.ivRecordStar1,
                binding.ivRecordStar2,
                binding.ivRecordStar3,
                binding.ivRecordStar4,
                binding.ivRecordStar5
        };
    }

    private void setupBookSearchClickListeners() {
        // 책 검색 박스 클릭 시 BookSearchFragment로 이동
        View.OnClickListener searchClickListener = v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_recordCreateFragment_to_bookSearchFragment);
        };

        // iv_book_search_box, iv_search_ic, tv_book_title 클릭 시 검색 화면으로 이동
        binding.ivBookSearchBox.setOnClickListener(searchClickListener);
        binding.ivSearchIc.setOnClickListener(searchClickListener);
        binding.tvBookTitle.setOnClickListener(searchClickListener);
    }

    private void setupFragmentResultListener() {
        // BookSearchFragment에서 선택한 책 정보 받기
        getParentFragmentManager().setFragmentResultListener(
                "book_selection",
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    Book book = result.getParcelable("selected_book");
                    if (book != null) {
                        selectedBook = book;
                        updateBookInfo(book);
                    }
                }
        );
    }

    private void updateBookInfo(Book book) {
        binding.tvBookTitle.setText(book.getTitle());
        binding.tvBookTitle.setTextColor(getResources().getColor(R.color.font_main));
    }

    private void setupDatePicker() {
        binding.ivRecordCalender.setOnClickListener(v -> showDatePicker());
        binding.tvRecordSelectedDate.setOnClickListener(v -> showDatePicker());
    }

    private void setCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        selectedDate = sdf.format(calendar.getTime());
        binding.tvRecordSelectedDate.setText(selectedDate);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate = String.format(Locale.getDefault(), "%d.%02d.%02d",
                            year, month + 1, dayOfMonth);
                    binding.tvRecordSelectedDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void setupStarRating() {
        for (int i = 0;i < stars.length;i++){
            final int rating = i + 1;
            stars[i].setOnClickListener(v -> setRating(rating));
        }
    }

    private void setRating(int rating) {
        currentRating = rating;

        for (int i = 0;i < stars.length;i++){
            if (i < rating) {
                stars[i].setBackgroundResource(R.drawable.ic_star_filled);
            } else {
                stars[i].setBackgroundResource(R.drawable.ic_star_unfilled);
            }
        }
    }

    private void setupCheckBoxes() {
        binding.cbRecordStateReading.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.cbRecordStateFinish.setChecked(false);
            } else {
                if (!binding.cbRecordStateFinish.isChecked()) {
                    buttonView.setChecked(true);
                }
            }
        });

        binding.cbRecordStateFinish.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (isChecked) {
                binding.cbRecordStateReading.setChecked(false);
            } else {
                if (!binding.cbRecordStateReading.isChecked()) {
                    buttonView.setChecked(true);
                }
            }
        }));

        binding.cbRecordCategoryLiterature.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.cbRecordCategoryNonliterature.setChecked(false);
            } else {
                if (!binding.cbRecordCategoryNonliterature.isChecked()) {
                    buttonView.setChecked(true);
                }
            }
        });

        binding.cbRecordCategoryNonliterature.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.cbRecordCategoryLiterature.setChecked(false);
            } else {
                if (!binding.cbRecordCategoryLiterature.isChecked()) {
                    buttonView.setChecked(true);
                }
            }
        });
    }

    private void setupBackButton() {
        binding.ivBackRecordCreate.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setupConfirmButton() {
        // 확인 버튼 클릭 시
        binding.btnRecordCreate.setOnClickListener(v -> {
            if (validateForm()) {
                createRecord();
            }
        });
    }

    private boolean validateForm() {
        // 책 선택 확인
        if (selectedBook == null) {
            Toast.makeText(getContext(), "책을 선택해주세요", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 별점 확인
        if (currentRating == 0) {
            Toast.makeText(getContext(), "별점을 선택해주세요", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 페이지 입력 확인
        String startPageStr = binding.etRecordPage1.getText().toString().trim();
        String endPageStr = binding.etRecordPage2.getText().toString().trim();

        if (TextUtils.isEmpty(startPageStr) || TextUtils.isEmpty(endPageStr)) {
            Toast.makeText(getContext(), "페이지를 입력해주세요", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int startPage = Integer.parseInt(startPageStr);
            int endPage = Integer.parseInt(endPageStr);

            if (startPage < 0 || endPage < 0) {
                Toast.makeText(getContext(), "페이지는 0 이상이어야 합니다", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (startPage > endPage) {
                Toast.makeText(getContext(), "시작 페이지가 끝 페이지보다 클 수 없습니다", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "올바른 페이지 번호를 입력해주세요", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 감상평 확인
        String review = binding.etRecordReview.getText().toString().trim();
        if (TextUtils.isEmpty(review)) {
            Toast.makeText(getContext(), "감상평을 입력해주세요", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void createRecord() {
        // 현재 사용자 확인
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // 입력된 정보 수집
        String startPageStr = binding.etRecordPage1.getText().toString().trim();
        String endPageStr = binding.etRecordPage2.getText().toString().trim();
        int startPage = Integer.parseInt(startPageStr);
        int endPage = Integer.parseInt(endPageStr);

        String review = binding.etRecordReview.getText().toString().trim();

        String status = binding.cbRecordStateReading.isChecked() ? "읽는중" : "완독";
        String category = binding.cbRecordCategoryLiterature.isChecked() ? "문학" : "비문학";
        boolean isPublic = !binding.cbRecordPrivate.isChecked();

        String isbn = selectedBook.getIsbn();

        // Book 컬렉션 참조
        DocumentReference bookRef = db.collection("users")
                .document(userId)
                .collection("books")
                .document(isbn);

        // Book 문서가 존재하는지 확인
        bookRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Book이 이미 존재하면 category와 status 업데이트
                updateBookFields(bookRef, category, status, selectedDate, isPublic);
            } else {
                // Book이 존재하지 않으면 새로 생성
                createNewBook(bookRef, category, status, selectedDate, isPublic);
            }

            // Record 추가 (books와 같은 계층에)
            addRecord(userId, isbn, startPage, endPage, review, isPublic);

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Book 확인 실패", e);
            Toast.makeText(getContext(), "기록 추가에 실패했습니다", Toast.LENGTH_SHORT).show();
        });
    }

    private void createNewBook(DocumentReference bookRef, String category, String status,
                                String lastRecordDate, boolean isPublic) {
        Map<String, Object> bookData = new HashMap<>();
        bookData.put("isbn", selectedBook.getIsbn());
        bookData.put("title", selectedBook.getTitle());
        bookData.put("author", selectedBook.getAuthor());
        bookData.put("cover", selectedBook.getImageUrl());
        bookData.put("category", category);
        bookData.put("status", status);
        bookData.put("isPublic", isPublic);
        bookData.put("lastRecordDate", lastRecordDate);
        bookData.put("createdAt", FieldValue.serverTimestamp());

        bookRef.set(bookData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Book 생성 성공");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Book 생성 실패", e);
                });
    }

    private void updateBookFields(DocumentReference bookRef, String category, String status,
                                   String lastRecordDate, boolean isPublic) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("category", category);
        updates.put("status", status);
        updates.put("lastRecordDate", lastRecordDate);
        updates.put("isPublic", isPublic);

        bookRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Book 업데이트 성공");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Book 업데이트 실패", e);
                });
    }

    private void addRecord(String userId, String isbn, int startPage, int endPage,
                           String review, boolean isPublic) {
        // Record 데이터 생성
        Map<String, Object> recordData = new HashMap<>();
        recordData.put("isbn", isbn);
        recordData.put("cover", selectedBook.getImageUrl());
        recordData.put("title", selectedBook.getTitle());
        recordData.put("date", selectedDate);
        recordData.put("startPage", startPage);
        recordData.put("endPage", endPage);
        recordData.put("rating", currentRating);
        recordData.put("review", review);
        recordData.put("isPublic", isPublic);
        recordData.put("createdAt", FieldValue.serverTimestamp());

        // users/{userId}/records 컬렉션에 추가 (books와 같은 계층)
        db.collection("users")
                .document(userId)
                .collection("records")
                .add(recordData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Record 추가 성공: " + documentReference.getId());

                    // FeedItem 생성 (기존 로직 유지)
                    String status = binding.cbRecordStateReading.isChecked() ? "읽는중" : "완독";
                    String category = binding.cbRecordCategoryLiterature.isChecked() ? "문학" : "비문학";
                    boolean isPrivate = binding.cbRecordPrivate.isChecked();

                    FeedItem newFeedItem = new FeedItem(
                            documentReference.getId(),
                            selectedBook.getTitle(),
                            selectedBook.getAuthor(),
                            selectedBook.getImageUrl(),
                            selectedDate,
                            currentRating,
                            startPage,
                            endPage,
                            review,
                            status,
                            category,
                            isPrivate
                    );

                    // HomeFragment로 결과 전달
                    Bundle result = new Bundle();
                    result.putParcelable("new_feed_item", newFeedItem);
                    getParentFragmentManager().setFragmentResult("record_created", result);

                    Toast.makeText(getContext(), "기록이 추가되었습니다", Toast.LENGTH_SHORT).show();

                    // 이전 화면으로 돌아가기
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Record 추가 실패", e);
                    Toast.makeText(getContext(), "기록 추가에 실패했습니다", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
