package com.example.myapplication.presentation.group;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;

import com.example.myapplication.R;
import com.example.myapplication.data.onmate.AddMateItem;
import com.example.myapplication.databinding.FragmentGroupCreateBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class GroupCreateFragment extends Fragment {
    private FragmentGroupCreateBinding binding;

    private FirebaseAuth auth ;
    private FirebaseFirestore db;
    private AddMateAdapter mateAdapter;


    public GroupCreateFragment()  {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentGroupCreateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    // binding.button.setOnClickListener 같은 작업 여기서 처리할 것
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController navController = new NavController(getContext());

//        binding.btnGroupCreate.setOnClickListener( view ->{
//
//        });

        binding.btnMateSearch.setOnClickListener(v->{
            navController.navigate(R.id.action_groupCreateFragment_to_mateSearchFragment);
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initAddMateAdapter(){
        AddMateAdapter adapter = new AddMateAdapter();
        adapter.setDeleteMode(false);
        binding.rvAddedMate.setAdapter(adapter);


    }


}
