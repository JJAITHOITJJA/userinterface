package com.example.myapplication.presentation.group;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.data.group.DiscussionItem;
import com.example.myapplication.data.group.GroupItem;
import com.example.myapplication.data.onmate.AddMateItem;
import com.example.myapplication.databinding.FragmentGroupInsideBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentGroupInsideBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    // binding.button.setOnClickListener 같은 작업 여기서 처리할 것
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        groupId = bundle.getString("groupId");

        initMemberAdapter();
        initDiscussionAdapter();
        loadGroupInfo();

        navController = NavHostFragment.findNavController(this);

        binding.ivBackGroupList.setOnClickListener(v->{
            navController.popBackStack();
        });

        binding.fabDiscussionCreate.setOnClickListener(v->{
        });



    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadGroupInfo(){
        db.collection("group").document(groupId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                if(document.exists()){
                    binding.tvGroupInsideName.setText(document.getString("name"));
                    binding.tvGroupDescription.setText(document.getString("description"));

                    Object membersObj = document.get("members");
                    Object discussionsObj = document.get("discussionList");

                    // members 처리
                    if (membersObj instanceof List) {
                        // ArrayList인 경우
                        members = new ArrayList<>((List<String>) membersObj);
                        loadGroupMate(members);
                    } else if (membersObj instanceof Map) {
                        // Map인 경우
                        Map<String, Object> membersMap = (Map<String, Object>) membersObj;
                        members = new ArrayList<>();
                        for (Object memberValue : membersMap.values()) {
                            if (memberValue instanceof String) {
                                members.add((String) memberValue);
                            }
                        }
                        loadGroupMate(members);
                    }

                    // discussionList 처리
                    if(discussionsObj instanceof List){
                        // ArrayList인 경우
                        discussionList = new ArrayList<>((List<String>) discussionsObj);
                        loadDiscussionData(discussionList);
                    } else if(discussionsObj instanceof Map){
                        // Map인 경우
                        Map<String, Object> discussionsMap = (Map<String, Object>) discussionsObj;
                        discussionList = new ArrayList<>();
                        for(Object discussionValue : discussionsMap.values()){
                            if(discussionValue instanceof String){
                                discussionList.add((String) discussionValue);
                            }
                        }
                        loadDiscussionData(discussionList);
                    }
                }
            }
        });
    }

    private void loadDiscussionData(List<String> discussionIds){
        if(discussionIds == null || discussionIds.isEmpty()){
            discussionAdapter.submitList(Collections.emptyList());
            return;
        }

        Log.d("GroupInsideFragment", "그룹 내부의 discussion data를 불러옵니다");
        List<DiscussionItem> discussionItems = new ArrayList<>();

        for(String discussionId : discussionIds){
            db.collection("discussion").document(discussionId).get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                String bookName = document.getString("bookName");
                                String author = document.getString("author");
                                String topic = document.getString("topic");

                                DiscussionItem item = new DiscussionItem(
                                        discussionId,
                                        bookName,
                                        author,
                                        "",
                                        topic,
                                        FieldValue.serverTimestamp().toString()
                                );
                                discussionItems.add(item);

                                if(discussionItems.size() == discussionIds.size()){
                                    discussionAdapter.submitList(new ArrayList<>(discussionItems));
                                }
                            }
                        }
                    });
        }
    }

    private void initMemberAdapter(){
        mateAdapter = new AddMateAdapter();
        mateAdapter.setDeleteMode(false);
        binding.rvGroupMate.setAdapter(mateAdapter);
    }

    // uid 리스트를 받아서 db에서 각 uid별로 객체 조회 후 adapter에 매핑해주는 함수
    private void loadGroupMate(List<String> members){
        if(members == null || members.isEmpty()){
            return;
        }

        List<AddMateItem> mateList = new ArrayList<>();

        for(String memberId : members){
            db.collection("users").document(memberId).get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                AddMateItem item = new AddMateItem(
                                        document.getString("nickname"),
                                        document.getId(),
                                        document.getString("profileImageUrl")
                                );
                                mateList.add(item);

                                // 모든 멤버 데이터가 로드되었을 때 어댑터 업데이트
                                if(mateList.size() == members.size()){
                                    mateAdapter.submitList(new ArrayList<>(mateList));
                                }
                            }
                        }
                    }
            );
        }
    }

    private void initDiscussionAdapter(){
        // 토론 어댑터 초기화 구현
        discussionAdapter = new DiscussionAdapter();
        binding.rvDiscussionList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDiscussionList.setAdapter(discussionAdapter);
        discussionAdapter.submitList(Collections.emptyList());
    }
}