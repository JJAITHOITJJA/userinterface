package com.example.myapplication.data.group;


import java.util.Date;

public class CommentItem {
    private String id;
    private String nickname;
    private String content;
    private int page;
    private Date createdAt;
    private boolean isReply;
    private String profileImageUrl;

    public CommentItem(String id, String nickname, String content,
                       int page, Date createdAt, boolean isReply, String profileImageUrl) {
        this.id = id;
        this.nickname = nickname;
        this.content = content;
        this.page = page;
        this.createdAt = createdAt;
        this.isReply = isReply;
        this.profileImageUrl = profileImageUrl;
    }

    // getters
    public String getId() { return id; }
    public String getNickname() { return nickname; }
    public String getContent() { return content; }
    public int getPage() { return page; }
    public Date getCreatedAt() { return createdAt; }
    public boolean isReply() { return isReply; }
    public String getProfileImageUrl(){
        return profileImageUrl;
    }
}