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
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RecordCreateFragment extends Fragment {

    private static final String TAG = "RecordCreateFragment";

    private FragmentRecordCreateBinding binding;
    private Book selectedBook;
    private int currentRating = 5;
    private ImageView[] stars;
    private CalendarDay selectedCalendarDay;

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

        setCurrentDate();
        initializeStars();
        setupBookSearchClickListeners();
        setupFragmentResultListener();
        setupDatePicker();
        setupStarRating();
        setupCheckBoxes();
        setupBackButton();
        setupConfirmButton();
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
        View.OnClickListener searchClickListener = v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_recordCreateFragment_to_bookSearchFragment);
        };

        binding.ivBookSearchBox.setOnClickListener(searchClickListener);
        binding.ivSearchIc.setOnClickListener(searchClickListener);
        binding.tvBookTitle.setOnClickListener(searchClickListener);
    }

    private void setupFragmentResultListener() {
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
        selectedCalendarDay = CalendarDay.today();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());

        Calendar cal = Calendar.getInstance();
        cal.set(selectedCalendarDay.getYear(),
                selectedCalendarDay.getMonth() - 1,
                selectedCalendarDay.getDay());

        String dateString = sdf.format(cal.getTime());
        binding.tvRecordSelectedDate.setText(dateString);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(selectedCalendarDay.getYear(),
                selectedCalendarDay.getMonth() - 1,
                selectedCalendarDay.getDay());

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedCalendarDay = CalendarDay.from(year, month + 1, dayOfMonth);

                    String dateString = String.format(Locale.getDefault(), "%d.%02d.%02d",
                            year, month + 1, dayOfMonth);
                    binding.tvRecordSelectedDate.setText(dateString);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void setupStarRating() {
        for (int i = 0; i < stars.length; i++){
            final int rating = i + 1;
            stars[i].setOnClickListener(v -> setRating(rating));
        }
    }

    private void setRating(int rating) {
        currentRating = rating;

        for (int i = 0; i < stars.length; i++){
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
        binding.btnRecordCreate.setOnClickListener(v -> {
            if (validateForm()) {
                createRecord();
            }
        });
    }

    private boolean validateForm() {
        if (selectedBook == null) {
            Toast.makeText(getContext(), "책을 선택해주세요", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (currentRating == 0) {
            Toast.makeText(getContext(), "별점을 선택해주세요", Toast.LENGTH_SHORT).show();
            return false;
        }

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

        String review = binding.etRecordReview.getText().toString().trim();
        if (TextUtils.isEmpty(review)) {
            Toast.makeText(getContext(), "감상평을 입력해주세요", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void createRecord() {
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

        // CalendarDay를 "yyyy-MM-dd" 문자열로 변환
        String lastRecordDate = String.format(Locale.getDefault(), "%d-%02d-%02d",
                selectedCalendarDay.getYear(),
                selectedCalendarDay.getMonth(),
                selectedCalendarDay.getDay());

        DocumentReference bookRef = db.collection("users")
                .document(userId)
                .collection("books")
                .document(isbn);

        bookRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                updateBookFields(bookRef, category, status, lastRecordDate, isPublic);
            } else {
                createNewBook(bookRef, category, status, lastRecordDate, isPublic);
            }

            addRecord(userId, isbn, startPage, endPage, review, status, category, isPublic);

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
        bookData.put("author", selectedBook.getAuthor().replace(" (지은이)", "").replace("(지은이)", ""));
        bookData.put("cover", selectedBook.getImageUrl());
        bookData.put("category", category);
        bookData.put("status", status);
        bookData.put("isPublic", isPublic);
        bookData.put("lastRecordDate", lastRecordDate);
        bookData.put("createdAt", FieldValue.serverTimestamp());

        bookRef.set(bookData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Book 생성 성공"))
                .addOnFailureListener(e -> Log.e(TAG, "Book 생성 실패", e));
    }

    private void updateBookFields(DocumentReference bookRef, String category, String status,
                                  String lastRecordDate, boolean isPublic) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("category", category);
        updates.put("status", status);
        updates.put("lastRecordDate", lastRecordDate);
        updates.put("isPublic", isPublic);

        bookRef.update(updates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Book 업데이트 성공"))
                .addOnFailureListener(e -> Log.e(TAG, "Book 업데이트 실패", e));
    }

    private void addRecord(String userId, String isbn, int startPage, int endPage,
                           String review, String status, String category, boolean isPublic) {
        // CalendarDay를 "yyyy-MM-dd" 문자열로 변환
        String dateString = String.format(Locale.getDefault(), "%d-%02d-%02d",
                selectedCalendarDay.getYear(),
                selectedCalendarDay.getMonth(),
                selectedCalendarDay.getDay());

        Map<String, Object> recordData = new HashMap<>();
        recordData.put("isbn", isbn);
        recordData.put("cover", selectedBook.getImageUrl());
        recordData.put("title", selectedBook.getTitle());
        recordData.put("date", dateString);
        recordData.put("startPage", startPage);
        recordData.put("endPage", endPage);
        recordData.put("rating", currentRating);
        recordData.put("review", review);
        recordData.put("isPublic", isPublic);
        recordData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users")
                .document(userId)
                .collection("records")
                .add(recordData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Record 추가 성공: " + documentReference.getId());

                    boolean isPrivate = binding.cbRecordPrivate.isChecked();

                    FeedItem newFeedItem = new FeedItem(
                            documentReference.getId(),
                            selectedBook.getTitle(),
                            selectedBook.getAuthor().replace(" (지은이)", "").replace("(지은이)", ""),
                            selectedBook.getImageUrl(),
                            selectedCalendarDay,
                            currentRating,
                            startPage,
                            endPage,
                            review,
                            status,
                            category,
                            isPrivate
                    );

                    Bundle result = new Bundle();
                    result.putParcelable("new_feed_item", newFeedItem);
                    getParentFragmentManager().setFragmentResult("record_created", result);

                    Toast.makeText(getContext(), "기록이 추가되었습니다", Toast.LENGTH_SHORT).show();

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