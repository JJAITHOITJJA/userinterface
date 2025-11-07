package com.example.myapplication.data.calendar;

public class BookRecord {
    private String title;
    private String author;
    private String page;
    private String quote;
    private int bookCoverImageRes;

    public BookRecord(String title, String author, String page, String quote, int bookCoverImageRes) {
        this.title = title;
        this.author = author;
        this.page = page;
        this.quote = quote;
        this.bookCoverImageRes = bookCoverImageRes;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getPage() { return page; }
    public String getQuote() { return quote; }
    public int getBookCoverImageRes() { return bookCoverImageRes; }
}