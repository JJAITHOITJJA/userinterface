package com.example.myapplication.data.onmate;

public class MateItem {
    private final String name;
    private final String id;
    private final int thumbnailResId;

    public MateItem(String name, String id, int thumbnailResId) {
        this.name = name;
        this.id = id;
        this.thumbnailResId = thumbnailResId;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
    public int getThumbnailResId() {
        return thumbnailResId;
    }
}