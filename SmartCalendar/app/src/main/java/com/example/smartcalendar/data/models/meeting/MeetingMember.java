package com.example.smartcalendar.data.models.meeting;

import com.google.gson.annotations.SerializedName;

public class MeetingMember {
    @SerializedName("user_id")
    private final int userId;
    
    @SerializedName("username")
    private final String username;

    public MeetingMember(int id, String name) { 
        this.userId = id; 
        this.username = name; 
    }
    
    public String getUsername() { return username; }

    public int getUserId() {
        return userId;
    }
}
