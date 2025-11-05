package com.example.myapplication.presentation.group;

import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.data.group.GroupItem;
import com.example.myapplication.databinding.FragmentGroupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupFragment extends Fragment {

    FragmentGroupBinding binding;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private GroupAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentGroupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    // binding.button.setOnClickListener 같은 작업 여기서 처리할 것
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = NavHostFragment.findNavController(this);

        initAdapter();
        binding.fabAdd.setOnClickListener(v -> {
            navController.navigate(R.id.action_groupFragment_to_groupSearchFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initAdapter(){
        adapter = new GroupAdapter();
        binding.rvMygroupList.setAdapter(adapter);
        binding.rvMygroupList.setLayoutManager(new LinearLayoutManager(getContext()));
        loadGroupData();
    }

    private List<GroupItem> loadGroupData(){
        List<GroupItem> groupList = new ArrayList<>();
        String currentUserId = user.getUid();

        db.collection("group")
                .whereArrayContains("members", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("GroupFragment", "Documents fetched: " + task.getResult().size());
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            GroupItem item = document.toObject(GroupItem.class);
                            groupList.add(item);
                        }
                        adapter.submitList(groupList);
                    } else {
                        Log.e("GroupFragment", "Error getting documents: ", task.getException());
                        // 에러 처리 로직
                        Toast.makeText(getContext(), "그룹 목록을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        adapter.submitList(Collections.emptyList());
                    }
                });
        return groupList;
    }
}
