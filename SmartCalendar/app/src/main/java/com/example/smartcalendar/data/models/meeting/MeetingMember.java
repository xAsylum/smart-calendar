package com.example.smartcalendar.data.models.meeting;

import com.google.gson.annotations.SerializedName;

public class MeetingMember {
    @SerializedName("user_id")
    private int userId;
    
    @SerializedName("username")
    private String username;

    public MeetingMember() {}

    public MeetingMember(int id, String name) { 
        this.userId = id; 
        this.username = name; 
    }
    
    public String getUsername() { return username; }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
