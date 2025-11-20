package com.example.myapplication.presentation.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.databinding.FragmentSettingsBinding;
import com.example.myapplication.presentation.MainActivity;
import com.example.myapplication.presentation.login.LoginActivity;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";

    private FragmentSettingsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 갤러리에서 이미지 선택 결과 처리
        pickMediaLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        // 선택한 이미지 URI를 Firestore에 저장
                        updateProfileImage(uri);
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).hideBottom();

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 로딩 오버레이 표시
        binding.loadingOverlay.setVisibility(View.VISIBLE);

        // 사용자 프로필 로드
        loadUserProfile();

        // 뒤로가기 클릭 리스너
        binding.ivBackSettings.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack();
        });

        // 사진 수정 클릭 리스너
        binding.tvAccountImageEdit.setOnClickListener(v -> {
            pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        // 로그아웃 클릭 리스너
        binding.clAccountComponent1.setOnClickListener(v -> showLogoutDialog());

        // 회원탈퇴 클릭 리스너
        binding.clAccountComponent3.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("확인", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("회원탈퇴")
                .setMessage("정말 탈퇴하시겠습니까?\n모든 데이터가 삭제됩니다.")
                .setPositiveButton("확인", (dialog, which) -> {
                    performDeleteAccount();
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void performLogout() {
        auth.signOut();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void performDeleteAccount() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.loadingOverlay.setVisibility(View.VISIBLE);

        String userId = currentUser.getUid();

        // Firestore에서 사용자 문서 삭제
        db.collection("users")
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "사용자 문서 삭제 성공");

                    // Firebase Auth에서 사용자 삭제
                    currentUser.delete()
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d(TAG, "Firebase Auth 사용자 삭제 성공");
                                Toast.makeText(getContext(), "회원탈퇴가 완료되었습니다", Toast.LENGTH_SHORT).show();

                                // LoginActivity로 이동
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Firebase Auth 사용자 삭제 실패", e);
                                Toast.makeText(getContext(), "회원탈퇴에 실패했습니다", Toast.LENGTH_SHORT).show();
                                binding.loadingOverlay.setVisibility(View.GONE);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "사용자 문서 삭제 실패", e);
                    Toast.makeText(getContext(), "회원탈퇴에 실패했습니다", Toast.LENGTH_SHORT).show();
                    binding.loadingOverlay.setVisibility(View.GONE);
                });
    }

    private void updateProfileImage(Uri imageUri) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        // 로딩 표시
        binding.loadingOverlay.setVisibility(View.VISIBLE);

        String userId = currentUser.getUid();
        String imageUriString = imageUri.toString();

        // Firestore에 profileImageUrl 업데이트
        db.collection("users")
                .document(userId)
                .update("profileImageUrl", imageUriString)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "프로필 이미지 URL 업데이트 성공");

                    // UI 업데이트
                    com.bumptech.glide.Glide.with(requireContext())
                            .load(imageUri)
                            .placeholder(R.drawable.basic_profile)
                            .error(R.drawable.basic_profile)
                            .circleCrop()
                            .into(binding.ivAccountProfileImg);

                    Toast.makeText(getContext(), "프로필 사진이 변경되었습니다", Toast.LENGTH_SHORT).show();
                    binding.loadingOverlay.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "프로필 이미지 URL 업데이트 실패", e);
                    Toast.makeText(getContext(), "프로필 사진 변경에 실패했습니다", Toast.LENGTH_SHORT).show();
                    binding.loadingOverlay.setVisibility(View.GONE);
                });
    }

    private void loadUserProfile() {
        // 현재 사용자 확인
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "사용자가 로그인되어 있지 않습니다");
            binding.loadingOverlay.setVisibility(View.GONE);
            return;
        }

        String userId = currentUser.getUid();

        // Firestore에서 사용자 정보 가져오기
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // nickname 필드 가져오기
                        String nickname = documentSnapshot.getString("nickname");
                        if (nickname != null && !nickname.isEmpty()) {
                            binding.tvAccountName.setText(nickname);
                        } else {
                            // nickname이 없으면 displayName 또는 이메일 사용
                            String displayName = documentSnapshot.getString("displayName");
                            if (displayName != null && !displayName.isEmpty()) {
                                binding.tvAccountName.setText(displayName);
                            } else if (currentUser.getEmail() != null) {
                                binding.tvAccountName.setText(currentUser.getEmail());
                            }
                        }

                        // profileImageUrl 필드 가져오기
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            com.bumptech.glide.Glide.with(requireContext())
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.basic_profile)
                                    .error(R.drawable.basic_profile)
                                    .circleCrop()
                                    .into(binding.ivAccountProfileImg);
                        } else {
                            // profileImageUrl이 없으면 기본 이미지 사용
                            binding.ivAccountProfileImg.setImageResource(R.drawable.basic_profile);
                        }

                        Log.d(TAG, "사용자 프로필 로드 성공");
                    } else {
                        Log.w(TAG, "사용자 문서가 존재하지 않습니다");
                    }

                    // 로딩 완료 - 오버레이 숨기기
                    binding.loadingOverlay.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "사용자 프로필 로드 실패", e);
                    // 에러 시에도 오버레이 숨기기
                    binding.loadingOverlay.setVisibility(View.GONE);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
