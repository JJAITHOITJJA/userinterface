package com.example.myapplication.presentation.login;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.myapplication.databinding.FragmentSignUpBinding;

import java.util.HashMap;
import java.util.Map;

public class SignUpFragment extends Fragment {

    private FragmentSignUpBinding binding;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private static final String TAG = "SignUpFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, "Firebase instances initialized.");

        updateSignUpButtonState();

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateSignUpButtonState();
            }
        };

        binding.etEmailInput.addTextChangedListener(textWatcher);
        binding.etPasswordInput.addTextChangedListener(textWatcher);
        binding.etNicknameInput.addTextChangedListener(textWatcher);


        binding.btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.etEmailInput.getText().toString().trim();
                String password = binding.etPasswordInput.getText().toString().trim();
                String nickname = binding.etNicknameInput.getText().toString().trim();

                Log.d(TAG, "Sign Up button clicked. Email: " + email + ", Nickname: " + nickname);

                if (!email.isEmpty() && !password.isEmpty() && !nickname.isEmpty()) {
                    handleSignUp(email, password, nickname);
                } else {
                    Toast.makeText(requireContext(), "이메일, 비밀번호, 닉네임을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Validation failed: Some fields are empty.");
                }
            }
        });
    }

    private void updateSignUpButtonState() {
        String email = binding.etEmailInput.getText().toString().trim();
        String password = binding.etPasswordInput.getText().toString().trim();
        String nickname = binding.etNicknameInput.getText().toString().trim();

        boolean isPasswordValid = password.length() >= 6;
        boolean isAllFieldsFilledAndValid = !email.isEmpty() && !nickname.isEmpty() && isPasswordValid;

        int colorId = isAllFieldsFilledAndValid ? R.color.g1 : R.color.g7;
        int colorValue = ContextCompat.getColor(requireContext(), colorId);

        binding.btnSignup.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorValue));
        binding.btnSignup.setEnabled(isAllFieldsFilledAndValid);

        Log.d(TAG, "Button state updated - Email: " + !email.isEmpty() +
                ", Password: " + isPasswordValid + ", Nickname: " + !nickname.isEmpty() +
                ", Enabled: " + isAllFieldsFilledAndValid);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "Binding destroyed.");
    }

    private void handleSignUp(String email, String password, String nickname) {

        if (password.length() < 6) {
            Toast.makeText(requireContext(), "비밀번호는 최소 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Password validation failed: less than 6 characters.");
            return;
        }

        Log.d(TAG, "Attempting Firebase Auth user creation for: " + email);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                Log.d(TAG, "Auth successful. UID: " + user.getUid());
                                saveUserProfile(user, email, nickname);
                            }
                        } else {
                            Log.e(TAG, "Auth failed: " + task.getException().getLocalizedMessage());
                            Toast.makeText(requireContext(), "회원가입 실패: " + task.getException().getLocalizedMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void saveUserProfile(FirebaseUser user, String email, String nickname) {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("nickname", nickname);
        userProfile.put("email", email);
        userProfile.put("createdAt", FieldValue.serverTimestamp());
        userProfile.put("role", "general");

        Log.d(TAG, "Attempting Firestore save for UID: " + user.getUid());

        db.collection("users").document(user.getUid())
                .set(userProfile)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Firestore save successful. Navigating back.");
                        Toast.makeText(requireContext(), "🎉 " + nickname + "님, 회원가입 완료!", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Firestore save failed: " + e.getMessage());
                        Toast.makeText(requireContext(), "데이터 저장 실패: 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                    }
                });
    }
}