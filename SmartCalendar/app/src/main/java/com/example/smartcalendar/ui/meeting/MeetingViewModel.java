package com.example.smartcalendar.ui.meeting;

import androidx.lifecycle.ViewModel;

import com.example.smartcalendar.data.models.MeetingMember;
import com.example.smartcalendar.data.models.MeetingRepository;
import com.example.smartcalendar.data.models.Meeting;
import com.example.smartcalendar.data.models.MeetingLocation;
import com.example.smartcalendar.data.models.User;
import com.example.smartcalendar.ui.friends.FriendRepository;

import java.util.ArrayList;
import java.util.List;

public class MeetingViewModel extends ViewModel {
    private MeetingRepository meetingRepo = MeetingRepository.getInstance();
    private FriendRepository friendRepo = FriendRepository.getInstance();

    public Meeting getMeeting(int id) {
        return meetingRepo.getMeetingById(id);
    }

    public void updateMeeting(int id, String name, String address) {
        Meeting meeting = meetingRepo.getMeetingById(id);
        if (meeting != null) {
            meeting.setName(name);
            if (meeting.getLocation() == null) {
                meeting.setLocation(new MeetingLocation());
            }
            meeting.getLocation().setAddress(address);
        }
    }


    public List<User> getAvailableFriends() {
        return friendRepo.getFriends();
    }

    public void setMeetingMembers(int meetingId, List<User> selectedUsers) {
        Meeting m = meetingRepo.getMeetingById(meetingId);
        if (m != null) {
            List<MeetingMember> newMemberList = new ArrayList<>();
            for (User user : selectedUsers) {
                newMemberList.add(new MeetingMember(user.getId(), user.getUsername()));
            }
            m.setMembers(newMemberList);
        }
    }
}