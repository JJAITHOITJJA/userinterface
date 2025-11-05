package com.example.myapplication.data.group;

import java.util.List;
import java.util.ArrayList;

public class GroupItem {

    private String thumbnailUrl;
    private String name;
    private boolean isLocked;
    private boolean isLiterature;
    private String startDate;
    private String description;
    private String password;
    private int peopleNumber;
    private List<String> members;
    private List<String> discussionList;

    public GroupItem() {
        this.members = new ArrayList<>();
        this.discussionList = new ArrayList<>();
    }

    public GroupItem(String thumbnailUrl, String name, boolean isLocked, String startDate, String description, boolean isLiterature) {
        this.thumbnailUrl = thumbnailUrl;
        this.name = name;
        this.isLocked = isLocked;
        this.startDate = startDate;
        this.description = description;
        this.isLiterature = isLiterature;
        this.members = new ArrayList<>();
        this.discussionList = new ArrayList<>();
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public boolean getIsLiterature() {
        return isLiterature;
    }

    public void setIsLiterature(boolean isLiterature) {
        this.isLiterature = isLiterature;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPeopleNumber() {
        return peopleNumber;
    }

    public void setPeopleNumber(int peopleNumber) {
        this.peopleNumber = peopleNumber;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public List<String> getDiscussionList() {
        return discussionList;
    }

    public void setDiscussionList(List<String> discussionList) {
        this.discussionList = discussionList;
    }
}