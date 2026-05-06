package com.example.smartcalendar.data.models.meeting;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "meetings")
public class Meeting {
    @PrimaryKey
    @SerializedName("id")
    private int id;

    @SerializedName("owner")
    private int owner;

    @SerializedName("name")
    private String name;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("duration")
    private String duration;

    @Embedded
    @SerializedName("location")
    private MeetingLocation location;

    @SerializedName("address")
    private String apiAddress;

    @SerializedName("latitude")
    private Double apiLatitude;

    @SerializedName("longitude")
    private Double apiLongitude;

    @Ignore
    private transient List<MeetingMember> members = new ArrayList<>();
    
    @Ignore
    private transient List<String> chatMessages = new ArrayList<>();

    public void syncApiFields() {
        if (location != null) {
            this.apiAddress = location.getAddress();
            this.apiLatitude = location.getLatitude();
            this.apiLongitude = location.getLongitude();
        }
        if (duration != null && duration.contains("h")) {
            this.duration = "01:00:00";
        }
    }

    public MeetingLocation getLocation() { return location; }
    public void setLocation(MeetingLocation location) {
        this.location = location;
        syncApiFields();
    }

    public List<MeetingMember> getMembers() { return members; }
    public void setMembers(List<MeetingMember> members) { this.members = members; }

    public int getOwner() { return owner; }
    public void setOwner(int owner) { this.owner = owner; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public List<String> getChatMessages() { return chatMessages; }
    public void setChatMessages(List<String> chatMessages) { this.chatMessages = chatMessages; }

    public String getApiAddress() { return apiAddress; }
    public void setApiAddress(String apiAddress) { this.apiAddress = apiAddress; }
    public Double getApiLatitude() { return apiLatitude; }
    public void setApiLatitude(Double apiLatitude) { this.apiLatitude = apiLatitude; }
    public Double getApiLongitude() { return apiLongitude; }
    public void setApiLongitude(Double apiLongitude) { this.apiLongitude = apiLongitude; }
}
