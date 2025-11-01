package com.example.myapplication.data.group;

public class GroupItem {
    private final int thumbnailResId;
    private final String name;
    private final boolean isLocked;
    private final String startDate;
    private final String description;
    private final String tagRes; // 문학 인지 비문학인지 string 으로 내려주기

    // 생성자
    public GroupItem(int thumbnailResId, String name, boolean isLocked, String startDate, String description, String tagRes) {
        this.thumbnailResId = thumbnailResId;
        this.name = name;
        this.isLocked = isLocked;
        this.startDate = startDate;
        this.description = description;
        this.tagRes = tagRes;
    }

    // Getter 메서드
    public int getThumbnailResId() {
        return thumbnailResId;
    }

    public String getName() {
        return name;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getDescription() {
        return description;
    }

    public String getTagResId() {
        return tagRes;
    }
}