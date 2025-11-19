package com.example.myapplication.presentation.group;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.group.CommentItem;
import com.example.myapplication.databinding.FragmentGroupBookBinding;
import com.example.myapplication.presentation.MainActivity;
import com.example.myapplication.presentation.group.discussion.CommentAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class GroupBookFragment extends Fragment {
    private FragmentGroupBookBinding binding;

    private String discussionId;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private CommentAdapter commentAdapter;

    // 대댓글 모드 관리
    private boolean isReplyMode = false;
    private String replyToCommentId = null;

    public GroupBookFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        ((MainActivity) getActivity()).hideBottom();
        Log.d("GroupBookFragment", "onCreate 실행");

        if (getArguments() != null) {
            discussionId = getArguments().getString("discussionId");
            Log.d("GroupBookFragment", "discussionId 로드: " + discussionId);
        } else {
            Log.e("GroupBookFragment", "arguments가 null입니다.");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d("GroupBookFragment", "onCreateView 실행");
        binding = FragmentGroupBookBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController navController = NavHostFragment.findNavController(this);

        binding.ivBackGroupInside.setOnClickListener(v->{
            Log.d("GroupBookFragment", "뒤로 가기 버튼 클릭");
            navController.popBackStack();
        });
        loadDiscussionDetail();

        setupRecyclerView();

        binding.tvCommentTransmitBtn.setOnClickListener(v-> {
            if (isReplyMode) {
                writeRecomment();
            } else {
                writeComment();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).hideBottom();
        loadCommentData();
        Log.d("GroupBookFragment", "onResume 실행: loadCommentData 시작");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity) getActivity()).showBottom();
        binding = null;
        Log.d("GroupBookFragment", "onDestroyView 실행");
    }

    private void writeRecomment(){
        String content = binding.etComment.getText().toString();
        String userId= user.getUid();

        Map<String, Object> map = new HashMap<>();
        map.put("content", content);
        map.put("userId", userId);
        map.put("createdAt", new Date());

        db.collection("discussion").document(discussionId)
                .collection("comment").document(replyToCommentId)
                .collection("recomment").add(map)
                .addOnSuccessListener(documentReference -> {
                    Log.d("GroupBookFragment", "대댓글 작성 성공");

                    // 대댓글 모드 해제
                    isReplyMode = false;
                    replyToCommentId = null;

                    // UI 초기화
                    binding.etComment.setText("");
                    binding.etComment.setHint("댓글을 입력하세요");

                    loadCommentData();
                });
    }

    private void loadDiscussionDetail() {
        if (getArguments() != null) {
            String bookName = getArguments().getString("bookname");
            String author = getArguments().getString("author");
            String topic = getArguments().getString("topic");
            String bookCoverUrl = getArguments().getString("bookCover");

            Log.d("GroupBookFragment", "토론 상세 정보 로드:");
            Log.d("GroupBookFragment", "책 이름: " + bookName);
            Log.d("GroupBookFragment", "주제: " + topic);

            binding.tvBookSelectTitle.setText(bookName);
            binding.tvBookSelectAuthor.setText(author);
            binding.tvBookSelectTopic.setText(topic);

            if (bookCoverUrl != null && !bookCoverUrl.isEmpty()) {
                Glide.with(this)
                        .load(bookCoverUrl)
                        .placeholder(R.color.g2)
                        .into(binding.ivBookSelect);
                Log.d("GroupBookFragment", "책 표지 이미지 로드 시도: " + bookCoverUrl);
            } else{
                binding.ivBookSelect.setImageResource(R.color.g2);
            }
        }
    }

    private void setupRecyclerView() {
        commentAdapter = new CommentAdapter();
        commentAdapter.setOnCommentActionListener(
                (item, position) -> {
                    // 대댓글 모드 활성화
                    isReplyMode = true;
                    replyToCommentId = item.getId();

                    // EditText에 포커스 이동 및 힌트 변경
                    binding.etComment.setHint("@" + item.getNickname() + "님에게 답글");
                    binding.etComment.requestFocus();
                    Log.d("GroupBookFragment", "대댓글 모드 활성화: " + replyToCommentId);
                }
        );
        binding.rvCommentList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCommentList.setAdapter(commentAdapter);
        Log.d("GroupBookFragment", "RecyclerView 및 Adapter 설정 완료");
    }

    private void loadCommentData(){
        binding.loadingOverlay.setVisibility(View.VISIBLE);
        binding.flDiscussionRecord.setVisibility(View.INVISIBLE);
        Log.d("GroupBookFragment", "댓글 로딩 시작");

        Map<String, List<CommentItem>> commentMap = new HashMap<>();
        List<String> commentIds = new ArrayList<>();

        db.collection("discussion").document(discussionId)
                .collection("comment").get()
                .addOnCompleteListener(commentSnapshots-> {
                    if(commentSnapshots.getResult() != null && !commentSnapshots.getResult().isEmpty()){
                        AtomicInteger counter = new AtomicInteger(0);
                        int totalComments = commentSnapshots.getResult().size();
                        Log.d("GroupBookFragment", "총 댓글 개수: " + totalComments);

                        for (DocumentSnapshot commentDoc : commentSnapshots.getResult()) {
                            String commentId = commentDoc.getId();
                            commentIds.add(commentId);
                            Log.d("GroupBookFragment", "댓글 ID 처리 시작: " + commentId);

                            Date createdAt = commentDoc.getDate("createdAt");
                            String commentUserId = commentDoc.getString("userId");
                            Long pageValue = commentDoc.getLong("page");
                            int page = pageValue != null ? pageValue.intValue() : 0;
                            String content = commentDoc.getString("content");

                            db.collection("users").document(commentUserId).get()
                                    .addOnSuccessListener(userDoc -> {
                                        String username = userDoc.getString("nickname");
                                        String profileImageUrl = userDoc.getString("profileImageUrl");
                                        Log.d("GroupBookFragment", "댓글 작성자 정보 로드 성공: " + username);

                                        CommentItem comment = new CommentItem(
                                                commentId,
                                                username,
                                                content,
                                                page,
                                                createdAt,
                                                false,
                                                profileImageUrl
                                        );

                                        List<CommentItem> items = new ArrayList<>();
                                        items.add(comment);
                                        commentMap.put(commentId, items);

                                        commentDoc.getReference().collection("recomment")
                                                .orderBy("createdAt", Query.Direction.ASCENDING)
                                                .get()
                                                .addOnSuccessListener(replySnapshots -> {
                                                    if (replySnapshots.isEmpty()) {
                                                        Log.d("GroupBookFragment", commentId + " 대댓글 없음");
                                                        if (counter.incrementAndGet() == totalComments) {
                                                            Log.d("GroupBookFragment", "모든 댓글/대댓글 처리 완료 (대댓글 없음)");
                                                            List<CommentItem> flatList = sortByPage(commentMap, commentIds);
                                                            updateUI(flatList);
                                                        }
                                                        return;
                                                    }

                                                    AtomicInteger replyCounter = new AtomicInteger(0);
                                                    int totalReplies = replySnapshots.size();
                                                    Log.d("GroupBookFragment", commentId + " 대댓글 개수: " + totalReplies);

                                                    for (DocumentSnapshot replyDoc : replySnapshots) {
                                                        Date createdAtRecom = replyDoc.getDate("createdAt");
                                                        String replyUserId = replyDoc.getString("userId");
                                                        String replyContent = replyDoc.getString("content");
                                                        String replyId = replyDoc.getId();

                                                        db.collection("users").document(replyUserId).get()
                                                                .addOnSuccessListener(replyUserDoc -> {
                                                                    String replyUsername = replyUserDoc.getString("nickname");
                                                                    String replyProfileUrl = replyUserDoc.getString("profileImageUrl");
                                                                    Log.d("GroupBookFragment", "대댓글 작성자 정보 로드 성공: " + replyUsername);

                                                                    CommentItem reply = new CommentItem(
                                                                            replyId,
                                                                            replyUsername,
                                                                            replyContent,
                                                                            page,
                                                                            createdAtRecom,
                                                                            true,
                                                                            replyProfileUrl
                                                                    );
                                                                    commentMap.get(commentId).add(reply);

                                                                    if (replyCounter.incrementAndGet() == totalReplies) {
                                                                        Log.d("GroupBookFragment", commentId + "의 모든 대댓글 처리 완료");
                                                                        if (counter.incrementAndGet() == totalComments) {
                                                                            Log.d("GroupBookFragment", "모든 댓글/대댓글 처리 완료");
                                                                            List<CommentItem> flatList = sortByPage(commentMap, commentIds);
                                                                            updateUI(flatList);
                                                                        }
                                                                    }
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Log.e("GroupBookFragment", "대댓글 작성자 정보 로드 실패", e);
                                                                    if (replyCounter.incrementAndGet() == totalReplies) {
                                                                        if (counter.incrementAndGet() == totalComments) {
                                                                            List<CommentItem> flatList = sortByPage(commentMap, commentIds);
                                                                            updateUI(flatList);
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("GroupBookFragment", commentId + " 대댓글 조회 실패", e);
                                                    if (counter.incrementAndGet() == totalComments) {
                                                        List<CommentItem> flatList = sortByPage(commentMap, commentIds);
                                                        updateUI(flatList);
                                                    }
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("GroupBookFragment", commentId + " 댓글 작성자 정보 로드 실패", e);
                                        if (counter.incrementAndGet() == totalComments) {
                                            List<CommentItem> flatList = sortByPage(commentMap, commentIds);
                                            updateUI(flatList);
                                        }
                                    });
                        }
                    } else {
                        Log.d("GroupBookFragment", "댓글 문서 없음. UI 업데이트.");
                        updateUI(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("GroupBookFragment", "댓글 컬렉션 로드 실패", e);
                    binding.loadingOverlay.setVisibility(View.GONE);
                    binding.flDiscussionRecord.setVisibility(View.VISIBLE);
                });
    }

    private void updateUI(List<CommentItem> flatList) {
        if (getActivity() != null) {
            Log.d("GroupBookFragment", "UI 업데이트 시작. 최종 항목 개수: " + flatList.size());
            getActivity().runOnUiThread(() -> {
                commentAdapter.setItems(flatList);
                binding.loadingOverlay.setVisibility(View.GONE);
                binding.flDiscussionRecord.setVisibility(View.VISIBLE);
                Log.d("GroupBookFragment", "UI 업데이트 완료. 로딩 오버레이 숨김.");
            });
        }
    }

    private List<CommentItem> sortByPage(Map<String, List<CommentItem>> commentMap,
                                         List<String> commentIds) {
        Log.d("GroupBookFragment", "sortByPage 실행");
        List<CommentItem> flatList = new ArrayList<>();
        List<CommentItem> allComments = new ArrayList<>();

        for (String id : commentIds) {
            List<CommentItem> items = commentMap.get(id);
            if (items != null && !items.isEmpty()) {
                allComments.add(items.get(0));
            }
        }

        Collections.sort(allComments, new Comparator<CommentItem>() {
            @Override
            public int compare(CommentItem o1, CommentItem o2) {
                int pageCompare = Integer.compare(o1.getPage(), o2.getPage());
                if (pageCompare != 0) return pageCompare;

                if (o1.getCreatedAt() != null && o2.getCreatedAt() != null) {
                    return o1.getCreatedAt().compareTo(o2.getCreatedAt());
                }
                return 0;
            }
        });

        for (CommentItem comment : allComments) {
            List<CommentItem> items = commentMap.get(comment.getId());
            if (items != null) {
                flatList.addAll(items);
            }
        }
        return flatList;
    }

    private void writeComment(){
        String content = binding.etComment.getText().toString();
        String userId= user.getUid();
        String page= binding.etPageInput.getText().toString();
        Long pageNumber = Long.parseLong(page);


        Map<String, Object> map = new HashMap<>();
        map.put("content", content);
        map.put("userId", userId);
        map.put("createdAt", new Date());
        map.put("page", pageNumber);

        db.collection("discussion").document(discussionId).collection("comment").add(map);
        binding.etComment.setText("");
        binding.etPageInput.setText("");

        loadCommentData();
    }


}