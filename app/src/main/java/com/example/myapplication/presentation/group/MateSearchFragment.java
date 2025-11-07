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


import com.example.myapplication.R;
import com.example.myapplication.data.OnItemClickListener;
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
    private MateSearchAdapter searchAdapter;

    private AddMateAdapter addedAdapter;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public MateSearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db= FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initAddedMateListAdapter();
        initSearchListAdapter();

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

        observeAddedMates();

        binding.etOnmateSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE || actionId== EditorInfo.IME_ACTION_SEARCH){
                    String keyword = binding.etOnmateSearch.getText().toString().trim();

                    if(!keyword.isEmpty())
                        searchUser(keyword);
                    return true;
                }
                return false;
            }
        });


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
                viewModel.addMateToSelection(toAddMateItem(item));
            }
        });
        binding.rvSearchList.setAdapter(searchAdapter);
    }

    private AddMateItem toAddMateItem(MateItem item){
        return new AddMateItem(item.getName(), item.getUId(), R.drawable.capibara); // TODO : 나중에 이미지 불러오는 api 만들어지면 그때 수정
    }

    private void initAddedMateListAdapter(){
        addedAdapter = new AddMateAdapter();
        binding.rvAddedMate.setAdapter(addedAdapter);
    }

    private void observeAddedMates() {
        viewModel.addedMates.observe(getViewLifecycleOwner(), new Observer<List<AddMateItem>>() {
            @Override
            public void onChanged(List<AddMateItem> newMates) {
                addedAdapter.submitList(newMates);
                binding.rvAddedMate.scrollToPosition(newMates.size() - 1);
            }
        });
    }

    private void searchUser(String keyword){
        db.collection("users").whereEqualTo("nickname",keyword)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    List<MateItem> searchResultList = new ArrayList<>();
                    binding.tvOnmateSearch.setVisibility(View.VISIBLE);

                    if( !queryDocumentSnapshots.isEmpty()){
                        for(DocumentSnapshot document : queryDocumentSnapshots){
                            String name = document.getString("nickname");
                            String id = document.getString("email");
                            String uid = document.getId();
                            String profileImageUrl = document.getString("profileImageUrl");
                            MateItem item = new MateItem(name, id, uid, profileImageUrl);

                            searchResultList.add(item);
                        }
                        binding.rvSearchList.setVisibility(View.VISIBLE);
                        searchAdapter.submitList(searchResultList);


                    } else{
                        binding.rvSearchList.setVisibility(View.GONE);
                        searchAdapter.submitList(Collections.emptyList());

                    }
                })

                .addOnFailureListener(e -> {
                    Log.w("error", "에휴..검색결과를 불러오는 데 실패하엿노라..");
                });


    }
}
