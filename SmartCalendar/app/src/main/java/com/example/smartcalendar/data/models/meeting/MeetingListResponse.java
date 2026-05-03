package com.example.smartcalendar.data.models.meeting;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MeetingListResponse {
    @SerializedName("meetings")
    private List<Meeting> meetings;

    @SerializedName("count")
    private int count;

    public List<Meeting> getMeetings() { return meetings; }
    public int getCount() { return count; }
}
