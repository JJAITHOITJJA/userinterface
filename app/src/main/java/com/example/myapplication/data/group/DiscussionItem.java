package com.example.myapplication.data.group;

public class DiscussionItem implements Comparable<DiscussionItem> {
    private String discussionId;
    private String bookName;
    private String author;
    private String bookImageUrl;

    private String topic;
    private String startDate;

    public DiscussionItem(String discussionId, String bookName,
                          String author,
                          String bookImageUrl, String topic, String startDate){
        this.discussionId = discussionId;
        this.bookName= bookName;
        this.author= author;
        this.bookImageUrl= bookImageUrl;
        this.topic = topic;
        this.startDate = startDate;
    }

    public void setId(String id){
        this.discussionId = id;
    }

    public String getDiscussionId(){
        return discussionId;
    }

    public void setBookName(String bookName){
        this.bookName = bookName;
    }


    public String getBookName(){
        return bookName;
    }

    public void setAuthor(String author){
        this.author = author;
    }

    public String getAuthor(){
        return author;
    }

    public void setBookImageUrl(String bookImageUrl){
        this.bookImageUrl = bookImageUrl;
    }

    public String getBookImageUrl(){
        return bookImageUrl;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic(){
        return topic;
    }

    public void setStartDate(String startDate){
        this.startDate = startDate;
    }

    public String getStartDate(){
        return startDate;
    }

    @Override
    public int compareTo(DiscussionItem other) {
        return other.startDate.compareTo(startDate);
    }

}
