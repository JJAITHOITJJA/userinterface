package com.example.myapplication.data.home;

import android.os.Parcel;
import android.os.Parcelable;

public class FeedItem implements Parcelable{
    private String id;
    private String title;
    private String author;
    private int coverImage; // drawable resource id
    private String coverImageUrl; // URL for Glide
    private String date;
    private int rating; // 1-5 stars
    private int startPage;
    private int endPage;
    private String review;
    private String status; // "읽는중" or "완독"
    private String category; // "문학" or "비문학"
    private boolean isPrivate;

    public FeedItem(String id, String title, String author, int coverImage) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.coverImage = coverImage;
    }

    public FeedItem(String id, String title, String author, String coverImageUrl,
                    String date, int rating, int startPage, int endPage,
                    String review, String status, String category, boolean isPrivate) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.coverImageUrl = coverImageUrl;
        this.date = date;
        this.rating = rating;
        this.startPage = startPage;
        this.endPage = endPage;
        this.review = review;
        this.status = status;
        this.category = category;
        this.isPrivate = isPrivate;
    }

    protected FeedItem(Parcel in) {
        id = in.readString();
        title = in.readString();
        author = in.readString();
        coverImage = in.readInt();
        coverImageUrl = in.readString();
        date = in.readString();
        rating = in.readInt();
        startPage = in.readInt();
        endPage = in.readInt();
        review = in.readString();
        status = in.readString();
        category = in.readString();
        isPrivate = in.readByte() != 0;
    }

    public static final Creator<FeedItem> CREATOR = new Creator<FeedItem>() {
        @Override
        public FeedItem createFromParcel(Parcel in) {
            return new FeedItem(in);
        }

        @Override
        public FeedItem[] newArray(int size) {
            return new FeedItem[size];
        }
    };

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getCoverImage() { return coverImage; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public String getDate() { return date; }
    public int getRating() { return rating; }
    public int getStartPage() { return startPage; }
    public int getEndPage() { return endPage; }
    public String getReview() { return review; }
    public String getStatus() { return status; }
    public String getCategory() { return category; }
    public boolean isPrivate() { return isPrivate; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setCoverImage(int coverImage) { this.coverImage = coverImage; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public void setDate(String date) { this.date = date; }
    public void setRating(int rating) { this.rating = rating; }
    public void setStartPage(int startPage) { this.startPage = startPage; }
    public void setEndPage(int endPage) { this.endPage = endPage; }
    public void setReview(String review) { this.review = review; }
    public void setStatus(String status) { this.status = status; }
    public void setCategory(String category) { this.category = category; }
    public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeInt(coverImage);
        dest.writeString(coverImageUrl);
        dest.writeString(date);
        dest.writeInt(rating);
        dest.writeInt(startPage);
        dest.writeInt(endPage);
        dest.writeString(review);
        dest.writeString(status);
        dest.writeString(category);
        dest.writeByte((byte) (isPrivate ? 1 : 0));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FeedItem feedItem = (FeedItem) obj;
        return id.equals(feedItem.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
