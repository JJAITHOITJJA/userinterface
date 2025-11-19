package com.example.myapplication.presentation.search;

import com.example.myapplication.data.search.Book;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class AladinResponse {

    @SerializedName("title")
    private String title;

    @SerializedName("author")
    private String author;

    @SerializedName("publisher")
    private String publisher;

    @SerializedName("cover")
    private String cover;

    @SerializedName("isbn13")
    private String isbn13;

    public String getTitle(){
        return title;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getCover() {
        return cover;
    }

    public String getIsbn() {
        return isbn13;
    }

    public String getAuthor() {
        return author;
    }

    // AladinResponse를 Book 객체로 변환하는 메서드
    public Book toBook() {
        return new Book(
                this.title != null ? this.title : "",
                this.author != null ? this.author : "",
                this.publisher != null ? this.publisher : "",
                this.cover != null ? this.cover : "",
                this.isbn13 != null ? this.isbn13 : "",
                "" // description은 Aladin API에서 제공하지 않음
        );
    }

    // AladinResponse 리스트를 Book 리스트로 변환하는 정적 메서드
    public static List<Book> toBookList(List<AladinResponse> aladinResponses) {
        List<Book> books = new ArrayList<>();
        if (aladinResponses != null) {
            for (AladinResponse response : aladinResponses) {
                books.add(response.toBook());
            }
        }
        return books;
    }

    public class AladinResponse2 {
        @SerializedName("totalResults")
        private int totalResults;

        @SerializedName("itemsPerPage")
        private int itemsPerPage;

        @SerializedName("item")
        private List<AladinResponse> books;

        public int getTotalResults() {
            return totalResults;
        }

        public int getItemsPerPage() {
            return itemsPerPage;
        }

        public List<AladinResponse> getBooks() {
            return books;
        }
    }
}
