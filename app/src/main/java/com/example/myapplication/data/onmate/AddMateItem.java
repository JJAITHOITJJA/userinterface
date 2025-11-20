package com.example.myapplication.data.onmate;

public class AddMateItem {
    private final String name;
    private final String uid;
    private final String profileImageUrl;

    public AddMateItem(String name, String uid, String profileImageUrl) {
        this.name = name;
        this.uid = uid;
        this.profileImageUrl = profileImageUrl;
    }

    public String getName() {
        return name;
    }

    public String getUId() {
        return uid;
    }
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
