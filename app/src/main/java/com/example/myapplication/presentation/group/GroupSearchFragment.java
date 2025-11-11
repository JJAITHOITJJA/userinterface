package com.example.myapplication.presentation.group;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.databinding.FragmentGroupSearchBinding;

public class GroupSearchFragment extends Fragment {


    private FragmentGroupSearchBinding binding;
    private NavController navController;

    public GroupSearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentGroupSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    // binding.button.setOnClickListener 같은 작업 여기서 처리할 것
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = NavHostFragment.findNavController(this);


        binding.fabGroupCreate.setOnClickListener(v -> {
            navController.navigate(R.id.action_groupSearchFragment_to_groupCreateFragment);
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}