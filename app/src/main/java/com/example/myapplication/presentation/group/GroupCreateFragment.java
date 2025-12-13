package com.example.myapplication.presentation.group;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private AddMateAdapter mateAdapter;

    public GroupCreateFragment() {
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initAddMateAdapter();
        setupValidation(); // 유효성 검사 설정

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            int extraPaddingTop = 3;

            v.setPadding(
                    systemBars.left,
                    0,
                    systemBars.right,
                    navigationBars.bottom
            );
            v.post(() -> ((MainActivity) requireActivity()).hideBottom());
            return insets;
        });

        viewModel = new ViewModelProvider(requireActivity()).get(AddMateViewModel.class);

        NavController navController = NavHostFragment.findNavController(this);

        binding.btnGroupCreate.setOnClickListener(v -> {
            createGroup();
        });

        binding.ivBackGroupSearch.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        binding.btnMateSearch.setOnClickListener(v -> {
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

    // 유효성 검사 설정
    private void setupValidation() {
        // maxPeople EditText 검증
        binding.etMaxPeople.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    try {
                        int maxPeople = Integer.parseInt(s.toString());
                        if (maxPeople > 8) {
                            binding.etMaxPeople.setError("최대 인원은 8명까지입니다");
                        } else if (maxPeople < 1) {
                            binding.etMaxPeople.setError("최소 1명 이상이어야 합니다");
                        } else {
                            binding.etMaxPeople.setError(null);
                        }
                    } catch (NumberFormatException e) {
                        binding.etMaxPeople.setError("숫자만 입력해주세요");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // 비밀번호 입력 시 자동으로 체크박스 체크
        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    // 비밀번호가 입력되면 자동으로 체크
                    if (!binding.cbGroupPrivate.isChecked()) {
                        binding.cbGroupPrivate.setChecked(true);
                    }
                } else {
                    // 비밀번호가 비어있으면 체크 해제
                    if (binding.cbGroupPrivate.isChecked()) {
                        binding.cbGroupPrivate.setChecked(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // 체크박스를 수동으로 해제하면 비밀번호 삭제
        binding.cbGroupPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    // 체크 해제 시 비밀번호 삭제
                    binding.etPassword.setText("");
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel.addedMates.getValue() != null) {
            mateAdapter.submitList(viewModel.addedMates.getValue());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initAddMateAdapter() {
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

    private void createGroup() {
        // 입력값 검증
        String groupName = binding.etGroupName.getText().toString().trim();
        if (groupName.isEmpty()) {
            Toast.makeText(getContext(), "그룹 이름을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        String maxPeopleString = binding.etMaxPeople.getText().toString().trim();
        if (maxPeopleString.isEmpty()) {
            Toast.makeText(getContext(), "최대 인원을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer maxPeople;
        try {
            maxPeople = Integer.parseInt(maxPeopleString);
            if (maxPeople > 8) {
                Toast.makeText(getContext(), "최대 인원은 8명까지입니다", Toast.LENGTH_SHORT).show();
                return;
            }
            if (maxPeople < 1) {
                Toast.makeText(getContext(), "최소 1명 이상이어야 합니다", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "올바른 숫자를 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        String groupDescription = binding.etDescription.getText().toString().trim();
        String groupPassword = binding.etPassword.getText().toString().trim();

        boolean isLiterature = binding.cbGroupCategoryLiterature.isChecked();
        boolean isLocked = binding.cbGroupPrivate.isChecked();

        // 비밀번호 체크박스가 체크되어 있는데 비밀번호가 없으면 경고
        if (isLocked && groupPassword.isEmpty()) {
            Toast.makeText(getContext(), "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

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

        if (viewModel.addedMates.getValue() != null) {
            for (AddMateItem item : viewModel.addedMates.getValue()) {
                db.collection("users").document(item.getUId()).update("groupList", FieldValue.arrayUnion(groupId));
                members.add(item.getUId());
            }
        }

        group.put("members", members);
        newGroupRef.set(group)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Firestore save successful. Navigating back.");
                        Toast.makeText(getContext(), "그룹 생성 완료", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Firestore save failed: " + e.getMessage());
                        Toast.makeText(getContext(), "그룹 생성 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}