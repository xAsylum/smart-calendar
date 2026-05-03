package com.example.smartcalendar.data.models.friendrequest;

import com.google.gson.annotations.SerializedName;

public class FriendRequestSchema {
    @SerializedName("username")
    private String username;

    public FriendRequestSchema(String username) {
        this.username = username;
    }
}