package com.example.myapplication.presentation.group.discussion;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.search.Book;
import com.example.myapplication.databinding.FragmentDiscussionCreateBinding;
import com.example.myapplication.presentation.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DiscussionCreateFragment extends Fragment {
    private FirebaseAuth auth ;
    private FirebaseFirestore db;
    private FragmentDiscussionCreateBinding binding;
    private NavController navController;
    private Book selectedBook;

    private String groupId;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        getParentFragmentManager().setFragmentResultListener("book_selection", this, (requestKey, result) -> {
            // BookSearchFragment에서 전달된 책 정보 받기
            selectedBook = result.getParcelable("selected_book");
            Log.d("selectedBook", selectedBook.toString());
            if (selectedBook != null) {
                updateBookInfo(selectedBook);
            }
        });

    }

    private void updateBookInfo(Book book) {
        if (binding != null) {
            // 책 제목
            binding.tvBookSelectTitle.setText(book.getTitle());

            // 저자
            binding.tvBookSelectAuthor.setText(book.getAuthor());

            // 책 표지 이미지 (Glide 사용)
            if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(book.getImageUrl())
                        .placeholder(R.color.g2)
                        .error(R.color.g2)
                        .into(binding.ivBookCover);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDiscussionCreateBinding.inflate(inflater, container, false);
        ((MainActivity) getActivity()).hideBottom();
        groupId= getArguments().getString("groupId");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        navController= NavHostFragment.findNavController(this);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            int extraPaddingTop = 3;

            v.setPadding(
                    systemBars.left,
                    systemBars.top + dpToPx(v.getContext(), extraPaddingTop),
                    systemBars.right,
                    navigationBars.bottom  // 시스템 네비게이션 바 높이만큼 패딩
            );
            v.post(() -> ((MainActivity) requireActivity()).hideBottom());

            return insets;
        });
        binding.btnBookSearch.setOnClickListener(v->{
            navController.navigate(R.id.action_discussionCreateFragment_to_bookSearchFragment);
        });

        binding.btnBackGroupInside.setOnClickListener(v->
                navController.popBackStack()
        );

        binding.btnGroupCreate.setOnClickListener(v->
                createDiscussion()
        );

    }

    private int dpToPx(Context context, int dp) {
        return Math.round(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dp,
                        context.getResources().getDisplayMetrics()
                )
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity) getActivity()).showBottom();
        binding = null;
    }

    private void createDiscussion(){
        if (selectedBook == null) {
            Toast.makeText(getContext(), "책을 먼저 선택해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        String topic = binding.etTopic.getText().toString().trim();

        if (topic.isEmpty()) {
            Toast.makeText(getContext(), "토론 주제를 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        // selectedBook에서 직접 데이터 가져오기
        Map<String, Object> discussionData = new HashMap<>();
        discussionData.put("bookName", selectedBook.getTitle());
        discussionData.put("author", selectedBook.getAuthor());
        discussionData.put("topic", topic);
        discussionData.put("bookCover", selectedBook.getImageUrl());


        db.collection("discussion").add(discussionData)
                .addOnSuccessListener(documentReference -> {
                    String discussionId = documentReference.getId();
                    db.collection("group").document(groupId).update("discussionList", FieldValue.arrayUnion(discussionId));
                    if (navController != null) {
                        navController.popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "토론 생성에 실패했습니다", Toast.LENGTH_SHORT).show();
                    Log.e("DiscussionCreate", "Error creating discussion", e);
                });



    }

}