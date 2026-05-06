package com.example.smartcalendar.data.models.meeting;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MeetingMembersResponse {
    @SerializedName("members")
    private List<MeetingMember> members;
    
    @SerializedName("count")
    private int count;

    public List<MeetingMember> getMembers() { return members; }
    public int getCount() { return count; }
}
