package com.example.myapplication.presentation.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.R;
import com.example.myapplication.data.search.Book;
import com.example.myapplication.databinding.FragmentBookSearchBinding;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class BookSearchFragment extends Fragment {

    private FragmentBookSearchBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentBookSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpSearchBar();
        setupBackButton();

        performSearch("");
    }

    private void setUpSearchBar() {
        binding.etSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        binding.etSearch.setSingleLine(true);

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String query = s != null ? s.toString().trim() : "";
                performSearch(query);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    binding.ivSearchCancel.setVisibility(
                            s != null && s.length() > 0 ? View.VISIBLE : View.GONE
                    );
            }
        });

        binding.ivSearchCancel.setOnClickListener(v -> {
            if (binding.etSearch.getText() != null) {
                binding.etSearch.getText().clear();
            }
        });

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                String query = binding.etSearch.getText() != null ?
                        binding.etSearch.getText().toString().trim() : "";
                performSearch(query);
                // 키보드 숨기기
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) requireContext()
                                .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });
    }

    private void setupBackButton() {
        binding.ivSearchArrowBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void performSearch(String query) {
        List<Book> searchResults = searchBooks(query);

        if (!searchResults.isEmpty()) {
            showSearchSuccess(searchResults);
        } else {
            showSearchFail();
        }
    }

    private List<Book> searchBooks(String query) {
        List<Book> results = new ArrayList<>();
        results.add(new Book("안녕이라 그랬어", "김애란", "창비", "app/src/main/res/drawable/sayhello.jpeg"));
        results.add(new Book("채식주의자", "한강", "창비", "app/src/main/res/drawable/sayhello.jpeg"));
        results.add(new Book("82년생 김지영", "조남주", "민음사", "app/src/main/res/drawable/sayhello.jpeg"));
        results.add(new Book("달러구트 꿈 백화점", "이미예", "팩토리나인", "app/src/main/res/drawable/sayhello.jpeg"));
        results.add(new Book("트렌드 코리아 2024", "김난도", "미래의창", "app/src/main/res/drawable/sayhello.jpeg"));
        return results;
    }

    private void showSearchSuccess(List<Book> results) {
        binding.viewSearchBg.setVisibility(View.GONE);
        binding.fvSearchSuccess.setVisibility(View.VISIBLE);
        binding.clSearchFail.setVisibility(View.GONE);

        BookSearchSuccessFragment fragment = BookSearchSuccessFragment.newInstance(new ArrayList<>(results));

        fragment.setOnBookSelectedListener(selectedBook -> {
            // NavController를 사용하여 이전 화면으로 돌아가면서 데이터 전달
            Bundle result = new Bundle();
            result.putParcelable("selected_book", selectedBook);

            getParentFragmentManager().setFragmentResult("book_selection", result);

            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fv_search_success, fragment);
        transaction.commit();
    }

    private void showSearchFail() {
        binding.viewSearchBg.setVisibility(View.GONE);
        binding.fvSearchSuccess.setVisibility(View.GONE);
        binding.clSearchFail.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
