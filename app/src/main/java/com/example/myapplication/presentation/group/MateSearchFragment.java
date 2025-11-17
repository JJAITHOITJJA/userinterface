package com.example.myapplication.presentation.group;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.example.myapplication.R;
import com.example.myapplication.data.OnItemClickListener;
import com.example.myapplication.data.group.GroupItem;
import com.example.myapplication.data.onmate.AddMateItem;
import com.example.myapplication.data.onmate.MateItem;
import com.example.myapplication.databinding.FragmentSearchMateBinding;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MateSearchFragment extends Fragment {

    private FragmentSearchMateBinding binding;
    private AddMateViewModel viewModel;
    private NavController navController;
    private MateSearchAdapter searchAdapter;

    private AddMateAdapter addedAdapter;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public MateSearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();


        // ViewModel 초기화 (중요!)
        viewModel = new ViewModelProvider(requireActivity()).get(AddMateViewModel.class);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentSearchMateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    // binding.button.setOnClickListener 같은 작업 여기서 처리할 것
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = NavHostFragment.findNavController(this);

        // Adapter 초기화를 onViewCreated에서 (binding이 준비된 후)
        initSearchListAdapter();
        initAddedMateListAdapter();
        observeAddedMates();

        binding.etOnmateSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH||
                        actionId == EditorInfo.IME_ACTION_GO||(keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                        keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                    String keyword = binding.etOnmateSearch.getText().toString().trim();

                    if(!keyword.isEmpty())
                        searchUser(keyword);
                    return true;
                }
                return false;
            }
        });

        binding.ivBackCreateGroupBtn.setOnClickListener(v-> {
            navController.popBackStack();
        });

        binding.tvReturnToCreateBtn.setOnClickListener(v->
                navController.popBackStack()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initSearchListAdapter(){
        searchAdapter = new MateSearchAdapter();
        searchAdapter.setOnItemClickListener(new OnItemClickListener<MateItem>() {
            @Override
            public void onItemClick(MateItem item, int position) {
                if(viewModel != null) {
                    viewModel.addMateToSelection(toAddMateItem(item));
                }
            }
        });

        if(binding != null && binding.rvSearchList != null) {
            binding.rvSearchList.setAdapter(searchAdapter);
        }

        binding.rvAddedMate.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        binding.rvSearchList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSearchList.setAdapter(searchAdapter);
    }

    private AddMateItem toAddMateItem(MateItem item){
        return new AddMateItem(item.getName(), item.getUId(), item.getProfileImageUrl());
    }

    private void initAddedMateListAdapter(){
        addedAdapter = new AddMateAdapter(
                new OnItemClickListener<AddMateItem>() {
                    @Override
                    public void onItemClick(AddMateItem item, int position) {
                        viewModel.removeMateFromSelection(item);
                    }
                }
        );
        String myId= auth.getCurrentUser().getUid();
        db.collection("users").document(myId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String nickname = document.getString("nickname");
                            String profileImageUrl = document.getString("profileImageUrl");
                            AddMateItem myMate = new AddMateItem(nickname, myId, profileImageUrl);
                            viewModel.addMateToSelection(myMate);
                        }
                    }
                });
        addedAdapter.submitList(viewModel.addedMates.getValue());

        addedAdapter.setDeleteMode(true); // 삭제 모드 설정

        if(binding != null && binding.rvAddedMate != null) {
            binding.rvAddedMate.setAdapter(addedAdapter);
        }

        binding.rvAddedMate.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
    }

    private void observeAddedMates() {
        if(viewModel != null) {
            viewModel.addedMates.observe(getViewLifecycleOwner(), new Observer<List<AddMateItem>>() {
                @Override
                public void onChanged(List<AddMateItem> newMates) {
                    if(newMates != null && !newMates.isEmpty()) {
                        addedAdapter.submitList(newMates);

                        if(binding != null && binding.rvAddedMate != null) {
                            binding.rvAddedMate.scrollToPosition(newMates.size() - 1);
                        }
                    } else {
                        addedAdapter.submitList(new ArrayList<>());
                    }
                }
            });
        }
    }

    private void searchUser(String keyword){
        db.collection("users").whereEqualTo("nickname", keyword)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if(binding == null) return; // Fragment가 파괴된 경우 방지

                    List<MateItem> searchResultList = new ArrayList<>();

                    if(binding.tvOnmateSearch != null) {
                        binding.tvOnmateSearch.setVisibility(View.VISIBLE);
                    }

                    if(!queryDocumentSnapshots.isEmpty()){
                        for(DocumentSnapshot document : queryDocumentSnapshots){
                            String name = document.getString("nickname");
                            String id = document.getString("email");
                            String uid = document.getId();
                            String profileImageUrl = document.getString("profileImageUrl");
                            MateItem item = new MateItem(name, id, uid, profileImageUrl);

                            searchResultList.add(item);
                        }

                        if(binding.rvSearchList != null) {
                            binding.rvSearchList.setVisibility(View.VISIBLE);
                        }
                        searchAdapter.submitList(searchResultList);

                    } else {
                        if(binding.rvSearchList != null) {
                            binding.rvSearchList.setVisibility(View.GONE);
                        }
                        searchAdapter.submitList(Collections.emptyList());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("MateSearchFragment", "검색 실패: " + e.getMessage());
                });
    }
}