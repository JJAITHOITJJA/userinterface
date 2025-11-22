package com.example.myapplication.presentation.group;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import androidx.core.graphics.Insets;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplication.data.OnItemClickListener;
import com.example.myapplication.R;
import com.example.myapplication.data.OnItemLongClickListener;
import com.example.myapplication.data.group.DiscussionItem;
import com.example.myapplication.data.onmate.AddMateItem;
import com.example.myapplication.databinding.FragmentGroupInsideBinding;
import com.example.myapplication.presentation.MainActivity;
import com.example.myapplication.presentation.group.discussion.DiscussionAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class GroupInsideFragment extends Fragment {
    FragmentGroupInsideBinding binding;
    private FirebaseUser user;
    private FirebaseFirestore db;

    private String groupId;
    private List<String> members;
    private List<String> discussionList;

    private AddMateAdapter mateAdapter;
    private DiscussionAdapter discussionAdapter;

    private NavController navController;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        ((MainActivity) getActivity()).hideBottom();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentGroupInsideBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            groupId = bundle.getString("groupId");
            Log.d("GroupInsideFragment", "groupId 로드: " + groupId);
        } else {
            Log.e("GroupInsideFragment", "Bundle이 null입니다. groupId 로드 실패.");
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            int extraPaddingTop = 3;

            // 하단 패딩을 navigationBars.bottom으로 설정하여 네비게이션 바 위로 올림
            v.setPadding(
                    systemBars.left,
                    0,
                    systemBars.right,
                    navigationBars.bottom  // 시스템 네비게이션 바 높이만큼 패딩
            );
            v.post(() -> ((MainActivity) requireActivity()).hideBottom());
            return insets;
        });


        initMemberAdapter();
        initDiscussionAdapter();
        loadGroupInfo();

        navController = NavHostFragment.findNavController(this);

        binding.ivBackGroupList.setOnClickListener(v->{
            Log.d("GroupInsideFragment", "뒤로 가기 버튼 클릭");
            navController.popBackStack();
        });

        binding.fabDiscussionCreate.setOnClickListener(v->{
            Bundle bundle1= new Bundle();
            bundle1.putString("groupId", groupId);
            navController.navigate(R.id.action_groupInsideFragment_to_groupDiscussionCreateFragment, bundle1);
        });

        binding.ivGroupEdit.setOnClickListener(v->
                navController.navigate(R.id.action_groupInsideFragment_to_groupEditFragment)
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

    private void loadGroupInfo(){
        Log.d("GroupInsideFragment", "loadGroupInfo() 실행: 그룹 문서 로드 시작");
        db.collection("group").document(groupId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                if(document.exists()){
                    Log.d("GroupInsideFragment", "그룹 문서 로드 성공");
                    binding.tvGroupInsideName.setText(document.getString("name"));
                    binding.tvGroupDescription.setText(document.getString("description"));
                    Log.d("GroupInsideFragment", "그룹 이름: " + document.getString("name"));

                    Object membersObj = document.get("members");
                    Object discussionsObj = document.get("discussionList");

                    if (membersObj instanceof List) {
                        Log.d("GroupInsideFragment", "members 필드 (List) 처리");
                        members = new ArrayList<>((List<String>) membersObj);
                        loadGroupMate(members);
                    } else if (membersObj instanceof Map) {
                        Log.d("GroupInsideFragment", "members 필드 (Map) 처리 (비표준)");
                        Map<String, Object> membersMap = (Map<String, Object>) membersObj;
                        members = new ArrayList<>();
                        for (Object memberValue : membersMap.values()) {
                            if (memberValue instanceof String) {
                                members.add((String) memberValue);
                            }
                        }
                        loadGroupMate(members);
                    } else {
                        Log.w("GroupInsideFragment", "members 필드 타입 알 수 없음 또는 없음");
                    }

                    if(discussionsObj instanceof List){
                        Log.d("GroupInsideFragment", "discussionList 필드 (List) 처리");
                        discussionList = new ArrayList<>((List<String>) discussionsObj);
                        loadDiscussionData(discussionList);
                    } else if(discussionsObj instanceof Map){
                        Log.d("GroupInsideFragment", "discussionList 필드 (Map) 처리 (비표준)");
                        Map<String, Object> discussionsMap = (Map<String, Object>) discussionsObj;
                        discussionList = new ArrayList<>();
                        for(Object discussionValue : discussionsMap.values()){
                            if(discussionValue instanceof String){
                                discussionList.add((String) discussionValue);
                            }
                        }
                        loadDiscussionData(discussionList);
                    } else {
                        Log.w("GroupInsideFragment", "discussionList 필드 타입 알 수 없음 또는 없음");
                    }
                } else {
                    Log.e("GroupInsideFragment", "문서 ID가 존재하지 않음: " + groupId);
                }
            } else {
                Log.e("GroupInsideFragment", "그룹 문서 로드 실패", task.getException());
            }
        });
    }

    private void loadDiscussionData(List<String> discussionIds){
        if(discussionIds == null || discussionIds.isEmpty()){
            discussionAdapter.submitList(Collections.emptyList());
            Log.d("GroupInsideFragment", "loadDiscussionData: 토론 ID 목록이 비어 있습니다.");
            return;
        }

        Log.d("GroupInsideFragment", "loadDiscussionData() 실행: 토론 ID 개수: " + discussionIds.size());

        List<DiscussionItem> discussionItems = new ArrayList<>();

        for(String discussionId : discussionIds){
            db.collection("discussion").document(discussionId).get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                Log.d("GroupInsideFragment", "토론 문서 로드 성공: " + discussionId);
                                String bookName = document.getString("bookName");
                                String author = document.getString("author");
                                String topic = document.getString("topic");
                                String bookCover = document.getString("bookCover");

                                DiscussionItem item = new DiscussionItem(
                                        discussionId,
                                        bookName,
                                        author,
                                        bookCover,
                                        topic,
                                        FieldValue.serverTimestamp().toString()
                                );
                                discussionItems.add(item);
                                Log.d("GroupInsideFragment", "현재 로드된 토론 항목 개수: " + discussionItems.size());

                                if(discussionItems.size() == discussionIds.size()){
                                    discussionItems.sort(DiscussionItem::compareTo);
                                    db.collection("group").document(groupId)
                                            .update("thumbnailUrl", discussionItems.get(0).getBookImageUrl());
                                    discussionAdapter.submitList(new ArrayList<>(discussionItems));
                                }
                            } else {
                                Log.w("GroupInsideFragment", "토론 문서가 존재하지 않음: " + discussionId);
                            }
                        } else {
                            Log.e("GroupInsideFragment", "토론 문서 로드 실패: " + discussionId, task.getException());
                        }
                    });
        }
    }

    private void initMemberAdapter(){
        mateAdapter = new AddMateAdapter();
        mateAdapter.setDeleteMode(false);
        binding.rvGroupMate.setAdapter(mateAdapter);
        Log.d("GroupInsideFragment", "initMemberAdapter 실행");
    }

    private void loadGroupMate(List<String> members){
        if(members == null || members.isEmpty()){
            Log.d("GroupInsideFragment", "loadGroupMate: 멤버 목록이 비어 있습니다.");
            return;
        }

        Log.d("GroupInsideFragment", "loadGroupMate() 실행: 멤버 ID 개수: " + members.size());
        List<AddMateItem> mateList = new ArrayList<>();

        for(String memberId : members){
            db.collection("users").document(memberId).get()
                    .addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    DocumentSnapshot document = task.getResult();
                                    if(document.exists()){
                                        Log.d("GroupInsideFragment", "멤버 문서 로드 성공: " + memberId + " 닉네임: " + document.getString("nickname"));
                                        AddMateItem item = new AddMateItem(
                                                document.getString("nickname"),
                                                document.getId(),
                                                document.getString("profileImageUrl")
                                        );
                                        mateList.add(item);

                                        if(mateList.size() == members.size()){
                                            Log.d("GroupInsideFragment", "모든 멤버 문서 로드 완료. 어댑터 업데이트.");
                                            mateAdapter.submitList(new ArrayList<>(mateList));
                                        }
                                    } else {
                                        Log.w("GroupInsideFragment", "멤버 문서가 존재하지 않음: " + memberId);
                                    }
                                } else {
                                    Log.e("GroupInsideFragment", "멤버 문서 로드 실패: " + memberId, task.getException());
                                }
                            }
                    );
        }
    }

    private void initDiscussionAdapter(){
        discussionAdapter = new DiscussionAdapter();
        discussionAdapter.setOnItemClickListener(new OnItemClickListener<DiscussionItem>() {
            @Override
            public void onItemClick(DiscussionItem item, int position) {
                Log.d("GroupInsideFragment", "토론 항목 클릭: ID " + item.getDiscussionId());
                Bundle bundle = new Bundle();
                bundle.putString("discussionId", item.getDiscussionId());
                bundle.putString("bookname", item.getBookName());
                bundle.putString("author", item.getAuthor());
                bundle.putString("topic", item.getTopic());
                bundle.putString("bookCover", item.getBookImageUrl());
                navController.navigate(R.id.action_groupInsideFragment_to_groupDiscussionFragment, bundle);
            }

        }, new OnItemLongClickListener<DiscussionItem>() {
            @Override
            public void onItemLongClick(DiscussionItem item, int position) {
                showDeleteDialog(item);
            }
        });

        binding.rvDiscussionList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDiscussionList.setAdapter(discussionAdapter);
        discussionAdapter.submitList(Collections.emptyList());
        Log.d("GroupInsideFragment", "initDiscussionAdapter 실행");
    }

    private void showDeleteDialog(DiscussionItem item){
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("토론 삭제")
                .setMessage("'" + item.getBookName() + "' 토론을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    deleteDiscussion(item.getDiscussionId());
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteDiscussion(String discussionId){
        // 1. group 문서에서 discussionList 업데이트
        db.collection("group").document(groupId)
                .update("discussionList", FieldValue.arrayRemove(discussionId))
                .addOnSuccessListener(aVoid -> {

                    db.collection("discussion").document(discussionId)
                            .delete()
                            .addOnSuccessListener(aVoid2 -> {
                                android.widget.Toast.makeText(requireContext(),
                                        "토론이 삭제되었습니다",
                                        android.widget.Toast.LENGTH_SHORT).show();

                                // 3. UI 새로고침
                                loadGroupInfo();
                            })
                            .addOnFailureListener(e -> {
                                android.widget.Toast.makeText(requireContext(),
                                        "토론 삭제에 실패했습니다",
                                        android.widget.Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(requireContext(),
                            "토론 삭제에 실패했습니다",
                            android.widget.Toast.LENGTH_SHORT).show();
                });
    }
}