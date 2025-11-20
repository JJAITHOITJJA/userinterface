package com.example.myapplication.presentation.group;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.databinding.FragmentGroupEditBinding;
import com.example.myapplication.databinding.FragmentGroupInsideBinding;
import com.example.myapplication.presentation.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class GroupEditFragment extends Fragment {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FragmentGroupEditBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth= FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        ((MainActivity) getActivity()).hideBottom();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentGroupEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
