package com.example.smartcalendar.ui.meeting;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.smartcalendar.data.models.meeting.MeetingMember;
import com.example.smartcalendar.data.repository.FriendRepository;
import com.example.smartcalendar.data.repository.MeetingRepository;
import com.example.smartcalendar.data.models.meeting.Meeting;
import com.example.smartcalendar.data.models.meeting.MeetingLocation;
import com.example.smartcalendar.data.models.User;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.List;

public class MeetingViewModel extends ViewModel {
    private final FriendRepository friendRepo = FriendRepository.getInstance();

    public LiveData<List<Meeting>> getAllMeetingsLive(Context context) {
        return MeetingRepository.getInstance(context).getAllMeetingsLive();
    }
    public Meeting getMeeting(Context context, int id) {
        MeetingRepository meetingRepo = MeetingRepository.getInstance(context);
        return meetingRepo.getMeetingById(id);
    }
    public LiveData<Meeting> getMeetingLive(Context context, int id) {
        return MeetingRepository.getInstance(context).getMeetingByIdLive(id);
    }

    public List<User> getAvailableFriends() {
        return friendRepo.getFriends();
    }
    public void updateMeeting(Context context, int id, String name, String address) {
        new Thread(() -> {
            MeetingRepository meetingRepo = MeetingRepository.getInstance(context);
            Meeting meeting = meetingRepo.getMeetingById(id);
            if (meeting != null) {
                meeting.setName(name);
                if (meeting.getLocation() == null) {
                    meeting.setLocation(new MeetingLocation());
                }
                meeting.getLocation().setAddress(address);
                meeting.syncApiFields();
                meetingRepo.updateMeeting(context, meeting);
            }
        }).start();
    }

    public void setMeetingMembers(Context context, int meetingId, List<User> selectedUsers) {
        new Thread(() -> {
            MeetingRepository meetingRepo = MeetingRepository.getInstance(context);
            Meeting m = meetingRepo.getMeetingById(meetingId);
            if (m != null) {
                List<MeetingMember> newMemberList = new ArrayList<>();
                for (User user : selectedUsers) {
                    newMemberList.add(new MeetingMember(user.getId(), user.getUsername()));
                }
                m.setMembers(newMemberList);
                meetingRepo.updateMeeting(context, m);
            }
        }).start();
    }

    public void addChatMessage(Context context, int mId, String text) {
        new Thread(() -> {
            MeetingRepository meetingRepo = MeetingRepository.getInstance(context);
            Meeting m = meetingRepo.getMeetingById(mId);
            if (m != null) {
                m.getChatMessages().add(text);
                meetingRepo.updateMeeting(context, m);
            }
        }).start();
    }

    public void updateStartTime(Context context, int id, int hour, int minute) {
        new Thread(() -> {
            MeetingRepository meetingRepo = MeetingRepository.getInstance(context);
            Meeting meeting = meetingRepo.getMeetingById(id);
            if (meeting != null) {
                String currentStart = meeting.getStartTime();
                String datePart = currentStart != null && currentStart.contains("T")
                        ? currentStart.split("T")[0]
                        : "2026-01-01"; // Fallback w razie błędnych danych
                String newStartTime = String.format("%sT%02d:%02d:00", datePart, hour, minute);
                meeting.setStartTime(newStartTime);
                meetingRepo.updateMeeting(context, meeting);
            }
        }).start();
    }

    public void updateDuration(Context context, int id, String durationText) {
        new Thread(() -> {
            MeetingRepository meetingRepo = MeetingRepository.getInstance(context);
            Meeting meeting = meetingRepo.getMeetingById(id);
            if (meeting != null) {
                meeting.setDuration(durationText);
                meetingRepo.updateMeeting(context, meeting);
            }
        }).start();
    }
    public void createNewMeeting(Context context, CalendarDay date, OnMeetingCreatedListener listener) {
        new Thread(() -> {
            String dateString = String.format("%04d-%02d-%02dT12:00:00",
                    date.getYear(), date.getMonth() + 1, date.getDay());

            Meeting newMeeting = new Meeting();
            newMeeting.setName("Nowe spotkanie");
            newMeeting.setDuration("01:00:00");
            newMeeting.setStartTime(dateString);
            newMeeting.setLocation(new MeetingLocation());
            newMeeting.syncApiFields();

            MeetingRepository.getInstance(context).addMeeting(context, newMeeting, listener);
        }).start();
    }

    public interface OnMeetingCreatedListener {
        void onCreated(int actualServerId);
    }
}