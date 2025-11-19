package com.example.myapplication.presentation.search;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitService {

    @GET("itemSearch.aspx")
    Call<AladinResponse.AladinResponse2> getSearchBook(
            @Query("TTBkey") String ttbkey,
            @Query("Query") String Query,
            @Query("QueryType") String queryType,
            @Query("MaxResults") int maxResults,
            @Query("Start") int start,
            @Query("SearchTarget") String searchTarget,
            @Query("Output") String output,
            @Query("Version") String version
    );
}
