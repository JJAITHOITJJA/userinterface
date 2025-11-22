package com.example.myapplication.presentation.group;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.data.onmate.AddMateItem;
import com.example.myapplication.databinding.FragmentGroupCreateBinding;
import com.example.myapplication.presentation.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GroupCreateFragment extends Fragment {
    private FragmentGroupCreateBinding binding;
    private AddMateViewModel viewModel;

    private FirebaseAuth auth ;
    private FirebaseFirestore db;
    private AddMateAdapter mateAdapter;


    public GroupCreateFragment()  {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
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

        initAddMateAdapter();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            int extraPaddingTop = 3;

            // 하단 패딩을 navigationBars.bottom으로 설정하여 네비게이션 바 위로 올림
            v.setPadding(
                    systemBars.left,
                    0,
                    systemBars.right,
                    navigationBars.bottom  // 시스템 네비게이션 바 높이만큼 패딩
            );
            v.post(() -> ((MainActivity) requireActivity()).hideBottom());
            return insets;
        });

        viewModel = new ViewModelProvider(requireActivity()).get(
                AddMateViewModel.class);


        NavController navController = NavHostFragment.findNavController(this);

        binding.btnGroupCreate.setOnClickListener( v ->{
            createGroup();
        });

        binding.btnMateSearch.setOnClickListener(v->{
            navController.navigate(R.id.action_groupCreateFragment_to_mateSearchFragment);
        });

        binding.cbGroupCategoryLiterature.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.cbGroupCategoryNonliterature.setChecked(false);
                } else {
                    if (!binding.cbGroupCategoryNonliterature.isChecked()) {
                        binding.cbGroupCategoryLiterature.setChecked(true);
                    }
                }
            }
        });

        binding.cbGroupCategoryNonliterature.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.cbGroupCategoryLiterature.setChecked(false);
                } else {
                    if (!binding.cbGroupCategoryLiterature.isChecked()) {
                        binding.cbGroupCategoryNonliterature.setChecked(true);
                    }
                }
            }
        });


    }

    @Override
    public void onResume(){
        super.onResume();
        if(viewModel.addedMates.getValue()!= null){
            mateAdapter.submitList(viewModel.addedMates.getValue());
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void initAddMateAdapter(){
        mateAdapter = new AddMateAdapter();
        mateAdapter.setDeleteMode(false);
        binding.rvAddedMate.setAdapter(mateAdapter);
        binding.rvAddedMate.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mateAdapter.submitList(Collections.emptyList());

    }
    private String getCurrentDateAsString() {
        Date currentDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDateString = formatter.format(currentDate);
        return formattedDateString;
    }

    private void createGroup(){
        String groupName = binding.etGroupName.getText().toString();
        String groupDescription = binding.etDescription.getText().toString();
        String groupPassword = binding.etPassword.getText().toString();

        String maxPeopleString = binding.etMaxPeople.getText().toString();
        Integer maxPeople = Integer.parseInt(maxPeopleString);

        boolean isLiterature = binding.cbGroupCategoryLiterature.isChecked();
        boolean isLocked = binding.cbGroupPrivate.isChecked();

        Map<String, Object> group = new HashMap<>();
        group.put("name", groupName);
        group.put("description", groupDescription);
        group.put("password", groupPassword);
        group.put("peopleNumber", maxPeople);
        group.put("isLiterature", isLiterature);
        group.put("isLocked", isLocked);
        group.put("startDate", getCurrentDateAsString());

        group.put("discussionList", new ArrayList<>());

        List<String> members = new ArrayList<>();




        DocumentReference newGroupRef = db.collection("group").document();
        String groupId = newGroupRef.getId();

        for(AddMateItem item : viewModel.addedMates.getValue()){
            db.collection("users").document(item.getUId()).update("groupList", FieldValue.arrayUnion(groupId));
            members.add(item.getUId());
        }

        group.put("members", members);
        newGroupRef.set(group)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Firestore save successful. Navigating back.");
                        getParentFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Firestore save failed: " + e.getMessage());
                    }
                });
        Toast.makeText(getContext(), "그룹 생성 완료", Toast.LENGTH_SHORT).show();

    }


}
