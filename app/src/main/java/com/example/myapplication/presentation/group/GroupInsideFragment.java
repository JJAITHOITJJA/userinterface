package com.example.myapplication.presentation.group;

import android.os.Bundle;
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

import com.example.myapplication.R;
import com.example.myapplication.data.group.GroupItem;
import com.example.myapplication.data.onmate.AddMateItem;
import com.example.myapplication.databinding.FragmentGroupInsideBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class GroupInsideFragment extends Fragment {
    FragmentGroupInsideBinding binding;
    private FirebaseUser user;
    private FirebaseFirestore db;

    private String groupId;
    private List<String> members;
    private List<String> discussionList ;

    private AddMateAdapter mateAdapter;


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


        NavController navController = NavHostFragment.findNavController(this);

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

                    Map<String, Object> membersMap = (Map<String, Object>) document.get("members");
                    Map<String, Object> discussionsMap = (Map<String, Object>) document.get("discussionList");

                    if (membersMap != null) {
                        members = new ArrayList<>();

                        for (Object memberValue : membersMap.values()) {
                            if (memberValue instanceof String) {
                                members.add((String) memberValue);
                            }
                        }
                        loadGroupMate(members);
                    }

                    if(discussionsMap!= null){
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

    }

    private void initMemberAdapter(){
        mateAdapter= new AddMateAdapter();
        mateAdapter.setDeleteMode(false);
        binding.rvGroupMate.setAdapter(mateAdapter);
    }

    // uid 리스트를 받아서 db에서 각 uid별로 객체 조회 후 adapter에 매핑해주는 함수
    private void loadGroupMate(List<String> members){
        for(String memberIds : members){
            List<AddMateItem> mateList = new ArrayList<>();
            db.collection("users").document(memberIds).get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                AddMateItem item = new AddMateItem(document.getString("nickname")
                                        , document.getId()
                                        , R.drawable.capibara);
                                mateList.add(item);


                            }
                            mateAdapter.submitList(mateList);
                        }
                    }

                    );
        }

    }

    private void initDiscussionAdapter(){

    }
}
