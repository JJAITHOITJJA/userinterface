package com.example.myapplication.presentation.group.discussion;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentDiscussionCreateBinding;
import com.example.myapplication.presentation.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DiscussionCreateFragment extends Fragment {
    private FirebaseAuth auth ;
    private FirebaseFirestore db;
    private FragmentDiscussionCreateBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDiscussionCreateBinding.inflate(inflater, container, false);
        ((MainActivity) getActivity()).hideBottom();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity) getActivity()).showBottom();
        binding = null;
    }

    private void createDiscussion(){
        String bookName = binding.tvBookSelectTitle.getText().toString();
        String author = binding.tvBookSelectAuthor.getText().toString();
        String topic = binding.etTopic.getText().toString();
        String bookCover = getArguments().getString("bookCover");

        Map<String, Object> discussionData = new HashMap<>();
        discussionData.put("bookName", bookName);
        discussionData.put("author", author);
        discussionData.put("topic", topic);
        discussionData.put("bookCover", bookCover);

        db.collection("discussion").add(discussionData);
    }

}