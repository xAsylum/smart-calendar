package com.example.smartcalendar.data.models.friendrequest;

import com.google.gson.annotations.SerializedName;

public class FriendRequest {
    @SerializedName("id")
    private int id;

    @SerializedName("username")
    private String username;

    public int getId() { return id; }
    public String getUsername() { return username; }
}