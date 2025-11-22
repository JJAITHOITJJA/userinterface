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
import com.example.myapplication.data.OnItemClickListener;
import com.example.myapplication.data.OnItemLongClickListener;
import com.example.myapplication.data.group.DiscussionItem;
import com.example.myapplication.data.group.GroupItem;
import com.example.myapplication.databinding.FragmentGroupBinding;
import com.example.myapplication.presentation.group.discussion.DiscussionAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GroupFragment extends Fragment {

    FragmentGroupBinding binding;
    private NavController navController;
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


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = NavHostFragment.findNavController(this);

        initAdapter();

        binding.fabAdd.setOnClickListener(v -> {
            navController.navigate(R.id.action_groupFragment_to_groupSearchFragment);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.flGroupList.setVisibility(View.GONE);
        binding.loadingOverlay.setVisibility(View.VISIBLE);
        loadGroupData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initAdapter(){
        adapter = new GroupAdapter(new OnItemClickListener<GroupItem>() {
            @Override
            public void onItemClick(GroupItem item, int position) {
                String groupId = item.getGroupId();

                Bundle bundle = new Bundle();
                bundle.putString("groupId", groupId);
                navController.navigate(R.id.action_groupFragment_to_groupInsideFragment, bundle);
            }
        }, new OnItemLongClickListener<GroupItem>() {
            @Override
            public void onItemLongClick(GroupItem item, int position) {
                showDeleteDialog(item);
            }
        });

        binding.rvMygroupList.setAdapter(adapter);
        binding.rvMygroupList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void showDeleteDialog(GroupItem item){
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("그룹 삭제")
                .setMessage("'" + item.getName() + "' 토론을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    deleteGroup(item.getGroupId());
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteGroup(String groupId) {
        db.collection("group").document(groupId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> members = (List<String>) documentSnapshot.get("members");

                        if (members == null || members.isEmpty()) {
                            db.collection("group").document(groupId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        android.widget.Toast.makeText(requireContext(), "그룹이 성공적으로 삭제되었습니다.", android.widget.Toast.LENGTH_SHORT).show();
                                        loadGroupData();
                                    })
                                    .addOnFailureListener(e -> {
                                        android.widget.Toast.makeText(requireContext(), "그룹 삭제에 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show();
                                    });
                            return;
                        }

                        AtomicInteger membersUpdated = new AtomicInteger(0);
                        int totalMembers = members.size();

                        for (String memberId : members) {
                            db.collection("users").document(memberId)
                                    .update("groupList", FieldValue.arrayRemove(groupId))
                                    .addOnSuccessListener(aVoid -> {
                                        if (membersUpdated.incrementAndGet() == totalMembers) {
                                            db.collection("group").document(groupId)
                                                    .delete()
                                                    .addOnSuccessListener(aVoid2 -> {
                                                        android.widget.Toast.makeText(requireContext(), "그룹이 성공적으로 삭제되었습니다.", android.widget.Toast.LENGTH_SHORT).show();
                                                        loadGroupData();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        android.widget.Toast.makeText(requireContext(), "그룹 삭제에 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (membersUpdated.incrementAndGet() == totalMembers) {
                                            db.collection("group").document(groupId)
                                                    .delete()
                                                    .addOnSuccessListener(aVoid2 -> {
                                                        android.widget.Toast.makeText(requireContext(), "그룹이 성공적으로 삭제되었습니다.", android.widget.Toast.LENGTH_SHORT).show();
                                                        loadGroupData();
                                                    })
                                                    .addOnFailureListener(e2 -> {
                                                        android.widget.Toast.makeText(requireContext(), "그룹 삭제에 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    });
                        }
                    } else {
                        android.widget.Toast.makeText(requireContext(), "삭제하려는 그룹을 찾을 수 없습니다.", android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(requireContext(), "그룹 삭제에 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show();
                });
    }
    private void loadGroupData(){
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
                            item.setGroupId(document.getId());
                            groupList.add(item);
                        }
                        adapter.submitList(groupList);
                        binding.flGroupList.setVisibility(View.VISIBLE);
                        binding.loadingOverlay.setVisibility(View.GONE);
                    } else {
                        Log.e("GroupFragment", "Error getting documents: ", task.getException());
                        Toast.makeText(getContext(), "그룹 목록을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        adapter.submitList(Collections.emptyList());
                        binding.flGroupList.setVisibility(View.VISIBLE);
                        binding.loadingOverlay.setVisibility(View.GONE);
                    }
                });

    }
}