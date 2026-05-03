package com.example.smartcalendar.data.models.friend;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FriendListResponse {
    @SerializedName("friends")
    private List<Friend> friends;

    @SerializedName("count")
    private int count;

    public List<Friend> getFriends() { return friends; }
    public int getCount() { return count; }
}