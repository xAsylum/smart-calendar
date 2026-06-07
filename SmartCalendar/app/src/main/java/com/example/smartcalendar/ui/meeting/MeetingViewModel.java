package com.example.smartcalendar.ui.meeting;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.smartcalendar.data.models.friend.Friend;
import com.example.smartcalendar.data.models.meeting.MeetingMember;
import com.example.smartcalendar.data.repository.FriendRepository;
import com.example.smartcalendar.data.repository.MeetingRepository;
import com.example.smartcalendar.data.models.meeting.Meeting;
import com.example.smartcalendar.data.models.meeting.MeetingLocation;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.List;

public class MeetingViewModel extends ViewModel {

    private final MutableLiveData<List<MeetingMember>> meetingMembers = new MutableLiveData<>();

    public LiveData<List<Meeting>> getAllMeetingsLive(Context context) {
        return MeetingRepository.getInstance(context).getAllMeetingsLive();
    }

    public void refreshMeetings(Context context) {
        MeetingRepository.getInstance(context).fetchMeetingsFromServer(context);
    }

    public LiveData<Meeting> getMeetingLive(Context context, int id) {
        return MeetingRepository.getInstance(context).getMeetingByIdLive(id);
    }

    public LiveData<List<MeetingMember>> getMeetingMembersLive() {
        return meetingMembers;
    }

    public void loadMeetingMembers(Context context, int meetingId) {
        MeetingRepository.getInstance(context).fetchMeetingMembers(context, meetingId, new MeetingRepository.MembersLoadListener() {
            @Override
            public void onSuccess(List<MeetingMember> members) {
                meetingMembers.postValue(members);
            }

            @Override
            public void onFailure(String error) {
            }
        });
    }

    public void updateMeeting(Context context, int id, String name, String address, Double latitude, Double longitude) {
        new Thread(() -> {
            MeetingRepository meetingRepo = MeetingRepository.getInstance(context);
            Meeting meeting = meetingRepo.getMeetingById(id);
            if (meeting != null) {
                meeting.setName(name);
                if (meeting.getLocation() == null) {
                    meeting.setLocation(new MeetingLocation());
                }
                meeting.getLocation().setAddress(address);
                meeting.getLocation().setLatitude(latitude);
                meeting.getLocation().setLongitude(longitude);
                meeting.syncApiFields();
                meetingRepo.updateMeeting(context, meeting);
            }
        }).start();
    }

    public void setMeetingMembers(Context context, int meetingId, List<Friend> selectedUsers) {
        MeetingRepository.getInstance(context).setMeetingMembers(context, meetingId, selectedUsers, () -> {
            loadMeetingMembers(context, meetingId);
        });
    }

    public void updateStartTime(Context context, int id, int hour, int minute) {
        new Thread(() -> {
            MeetingRepository meetingRepo = MeetingRepository.getInstance(context);
            Meeting meeting = meetingRepo.getMeetingById(id);
            if (meeting != null) {
                String currentStart = meeting.getStartTime();
                String datePart = currentStart != null && currentStart.contains("T")
                        ? currentStart.split("T")[0]
                        : "2026-01-01";
                String newStartTime = String.format("%sT%02d:%02d:00", datePart, hour, minute);
                meeting.setStartTime(newStartTime);
                meetingRepo.updateMeeting(context, meeting);
            }
        }).start();
    }

    public void getAvailableFriends(Context context, FriendRepository.FriendsLoadListener listener) {
        FriendRepository.getInstance().getFriends(context, listener);
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
