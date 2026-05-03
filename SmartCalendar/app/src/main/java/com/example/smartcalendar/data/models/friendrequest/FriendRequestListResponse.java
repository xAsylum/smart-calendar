package com.example.smartcalendar.data.models.friendrequest;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FriendRequestListResponse {
    @SerializedName("requests")
    private List<FriendRequest> requests;
    public List<FriendRequest> getRequests() { return requests; }
}

