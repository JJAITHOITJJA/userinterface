package com.example.myapplication.data.record;

import android.os.Parcel;
import android.os.Parcelable;

public class BookRecord implements Parcelable {

    private String bookId;
    private String title;
    private String author;
    private String coverImageUrl;
    private String category;

    public BookRecord(String bookId, String title, String author, String coverImageUrl, String category) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.coverImageUrl = coverImageUrl;
        this.category = category;
    }

    protected BookRecord(Parcel in) {
        bookId = in.readString();
        title = in.readString();
        author = in.readString();
        coverImageUrl = in.readString();
        category = in.readString();
    }

    public static final Creator<BookRecord> CREATOR = new Creator<BookRecord>() {
        @Override
        public BookRecord createFromParcel(Parcel in) {
            return new BookRecord(in);
        }

        @Override
        public BookRecord[] newArray(int size) {
            return new BookRecord[size];
        }
    };

    // Getters
    public String getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public String getCategory() {
        return category;
    }

    // Setters
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bookId);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(coverImageUrl);
        dest.writeString(category);
    }
}
