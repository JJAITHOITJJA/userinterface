package com.example.myapplication.presentation.group;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.data.OnItemClickListener;
import com.example.myapplication.data.group.GroupItem;
import com.example.myapplication.databinding.FragmentGroupSearchBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GroupSearchFragment extends Fragment {


    private FragmentGroupSearchBinding binding;
    private FirebaseFirestore db;
    private FirebaseUser auth;
    private NavController navController;

    private boolean isLiterature;
    private boolean isNonLiterature;

    private GroupAdapter adapter;

    public GroupSearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db= FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance().getCurrentUser();

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
        initAdapter();
        navController = NavHostFragment.findNavController(this);


        binding.fabGroupCreate.setOnClickListener(v -> {
            navController.navigate(R.id.action_groupSearchFragment_to_groupCreateFragment);
        });

        binding.btnBackMyGroup.setOnClickListener(v->
                navController.popBackStack()
        );

        binding.etGroupNameSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH||
                        actionId == EditorInfo.IME_ACTION_GO||(keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                        keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                    String keyword = binding.etGroupNameSearch.getText().toString().trim();

                    if(!keyword.isEmpty())
                        searchGroupByOption(keyword, isLiterature, isNonLiterature);
                    return true;
                }
                return false;
            }
        });

        binding.ivSearchIcon.setOnClickListener( v-> {
            String keyword = binding.etGroupNameSearch.getText().toString().trim();
            if(!keyword.isEmpty())
                searchGroupByOption(keyword, isLiterature, isNonLiterature);
        });

        // 체크박스 옵션 정보
        binding.cbSearchLiterature.setOnCheckedChangeListener((buttonState, isChecked) -> {

            isLiterature = isChecked;
        });

        binding.cbSearchNonliterature.setOnCheckedChangeListener((buttonState, isChecked) -> {
            isNonLiterature = isChecked;
        });
    }

    private void initAdapter(){
        adapter = new GroupAdapter(new OnItemClickListener<GroupItem>() {
            @Override
            public void onItemClick(GroupItem item, int position) {
                if (item.getIsLocked()) {
                    showPasswordDialog(item);
                } else {
                    // 2. 잠겨있지 않으면 바로 입장
                    navigateToGroupInside(item.getGroupId());
                }

            }
        });
        binding.rvSearchList.setAdapter(adapter);
        binding.rvSearchList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void showPasswordDialog(GroupItem item){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("비밀번호 입력");
        final EditText input = new EditText(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setHint("그룹 비밀번호를 입력하세요");

        dialog.setView(input);

        dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredPassword = input.getText().toString();

                // 3. 비밀번호 검증 로직
                if (enteredPassword.equals(item.getPassword())) {
                    navigateToGroupInside(item.getGroupId());
                } else {
                    Toast.makeText(getContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    private void navigateToGroupInside(String groupId){
        String myId = auth.getUid();

        db.collection("users").document(myId).update("groupList", FieldValue.arrayUnion(groupId));
        db.collection("group").document(groupId).update("members", FieldValue.arrayUnion(myId));

        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("그룹 입장");
        dialog.setMessage("그룹 입장이 완료되었습니다.");

        dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        dialog.show();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void searchGroupByOption(String keyword, boolean isLiterature, boolean isNonLiterature){
        binding.loadingOverlay.setVisibility(View.VISIBLE);
        binding.rvSearchList.setVisibility(View.GONE);

        List<GroupItem> groupList = new ArrayList<>();

        Query query = db.collection("group");
        if (keyword != null && !keyword.trim().isEmpty()) {
            String trimmedKeyword = keyword.trim();
            String endKeyword = trimmedKeyword + "\uf8ff";

            query = query.whereGreaterThanOrEqualTo("name", trimmedKeyword)
                    .whereLessThan("name", endKeyword);
        }

        if(isLiterature && isNonLiterature){
        }
        else if(isLiterature){
            query = query.whereEqualTo("isLiterature", true);
        }
        else if(isNonLiterature){
            query = query.whereEqualTo("isLiterature", false);
        }

        query.get().addOnCompleteListener( task -> {
            if(task.isSuccessful()){
                for(QueryDocumentSnapshot document: task.getResult()){
                    GroupItem item = document.toObject(GroupItem.class);
                    item.setGroupId(document.getId());
                    groupList.add(item);
                }

                adapter.submitList(groupList);
                binding.loadingOverlay.setVisibility(View.GONE);
                binding.rvSearchList.setVisibility(View.VISIBLE);
            } else {
                // 오류 처리
                binding.loadingOverlay.setVisibility(View.GONE);
            }
        });
    }
}