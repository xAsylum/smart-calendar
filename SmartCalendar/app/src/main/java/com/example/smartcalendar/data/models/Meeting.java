package com.example.smartcalendar.data.models;

import java.util.ArrayList;
import java.util.List;

public class Meeting {
    private int id;
    private int owner;
    private String name;
    private String start_time;
    private String duration;

    private MeetingLocation location;
    private List<MeetingMember> members = new ArrayList<>();
    private List<String> chatMessages = new ArrayList<>();

    public MeetingLocation getLocation() { return location; }
    public void setLocation(MeetingLocation location) { this.location = location; }
    public List<MeetingMember> getMembers() { return members; }
    public void setMembers(List<MeetingMember> members) {this.members = members; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStartTime() { return start_time; }
    public void setStartTime(String start_time) { this.start_time = start_time; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public List<String> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(List<String> chatMessages) {
        this.chatMessages = chatMessages;
    }
}