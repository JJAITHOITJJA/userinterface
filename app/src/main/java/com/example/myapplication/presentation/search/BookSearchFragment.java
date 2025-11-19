package com.example.myapplication.presentation.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.R;
import com.example.myapplication.data.search.Book;
import com.example.myapplication.databinding.FragmentBookSearchBinding;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookSearchFragment extends Fragment {

    private FragmentBookSearchBinding binding;
    private static final String TAG = "BookSearchFragment";
    private static final String ALADIN_API_KEY = "ttbseongju14161409001";

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

        // 초기 상태 설정
        showInitialState();

        // 키보드 자동 표시
        showKeyboard();
    }

    private void showInitialState() {
        binding.viewSearchBg.setVisibility(View.VISIBLE);
        binding.fvSearchSuccess.setVisibility(View.GONE);
        binding.clSearchFail.setVisibility(View.GONE);
    }

    private void showKeyboard() {
        if (binding.etSearch != null) {
            binding.etSearch.requestFocus();
            binding.etSearch.postDelayed(() -> {
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) requireContext()
                                .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(binding.etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            }, 200);
        }
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
        // 검색어가 비어있으면 검색 실패 화면 표시
        if (query == null || query.trim().isEmpty()) {
            showSearchFail();
            return;
        }

        // API 호출
        searchBooksFromAPI(query.trim());
    }

    private void searchBooksFromAPI(String query) {
        // Retrofit 서비스 인스턴스 생성
        RetrofitService service = RetrofitClient_aladin.getClient().create(RetrofitService.class);

        // API 호출
        Call<AladinResponse.AladinResponse2> call = service.getSearchBook(
                ALADIN_API_KEY,      // ttbkey
                query,                // Query (검색어)
                "Keyword",              // QueryType (제목 검색)
                10,                   // MaxResults (최대 결과 수)
                1,                    // start (시작 페이지)
                "Book",               // SearchTarget (도서 검색)
                "JS",                 // output (JSON 형식)
                "20131101"            // Version
        );

        call.enqueue(new Callback<AladinResponse.AladinResponse2>() {
            @Override
            public void onResponse(Call<AladinResponse.AladinResponse2> call,
                                 Response<AladinResponse.AladinResponse2> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AladinResponse.AladinResponse2 aladinResponse = response.body();
                    List<AladinResponse> aladinBooks = aladinResponse.getBooks();

                    if (aladinBooks != null && !aladinBooks.isEmpty()) {
                        // AladinResponse를 Book 리스트로 변환
                        List<Book> bookList = AladinResponse.toBookList(aladinBooks);
                        showSearchSuccess(bookList);
                    } else {
                        showSearchFail();
                    }
                } else {
                    Log.e(TAG, "API 응답 실패: " + response.code());
                    showSearchFail();
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "검색 결과를 불러오는데 실패했습니다.",
                                     Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AladinResponse.AladinResponse2> call, Throwable t) {
                Log.e(TAG, "API 호출 실패", t);
                showSearchFail();
                if (getContext() != null) {
                    Toast.makeText(getContext(), "네트워크 오류가 발생했습니다.",
                                 Toast.LENGTH_SHORT).show();
                }
            }
        });
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
