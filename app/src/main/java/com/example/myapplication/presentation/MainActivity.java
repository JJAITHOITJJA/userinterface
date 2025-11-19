package com.example.myapplication.presentation;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityMainBinding;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_container);

        navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);


        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            int extraPaddingTop = 3;

            v.setPadding(
                    systemBars.left,
                    systemBars.top + dpToPx(v.getContext(), extraPaddingTop),
                    systemBars.right,
                    0
            );

            // 바텀 네비게이션 높이 퍼센트 동적 계산
            float screenHeight = (float) getResources().getDisplayMetrics().heightPixels /
                    getResources().getDisplayMetrics().density;

            float baseBottomNavHeight = 69f; // 기본 높이
            float systemNavHeightDp = navigationBars.bottom /
                    getResources().getDisplayMetrics().density;

            float totalBottomNavHeight = baseBottomNavHeight + systemNavHeightDp;
            float newHeightPercent = totalBottomNavHeight / screenHeight;

            ConstraintLayout.LayoutParams layoutParams =
                    (ConstraintLayout.LayoutParams) binding.bottomNavigationView.getLayoutParams();

            layoutParams.matchConstraintPercentHeight = newHeightPercent;
            binding.bottomNavigationView.setLayoutParams(layoutParams);

            return insets;
        });
    }

    private int dpToPx(Context context, int dp) {
        return Math.round(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dp,
                        context.getResources().getDisplayMetrics()
                )
        );
    }

    public void hideBottom() {
        if (binding != null) {
            binding.bottomNavigationView.setVisibility(View.GONE);
        }
    }

    public void showBottom() {
        if (binding != null) {
            binding.bottomNavigationView.setVisibility(View.VISIBLE);
        }
    }
}