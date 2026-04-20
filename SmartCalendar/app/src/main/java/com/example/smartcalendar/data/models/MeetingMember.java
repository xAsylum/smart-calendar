package com.example.smartcalendar.data.models;

public class MeetingMember {
    private final int userId;
    private final String username;

    public MeetingMember(int id, String name) { this.userId = id; this.username = name; }
    public String getUsername() { return username; }

    public int getUserId() {
        return userId;
    }
}