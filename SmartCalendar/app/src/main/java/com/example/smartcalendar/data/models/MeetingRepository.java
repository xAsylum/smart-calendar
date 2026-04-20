package com.example.smartcalendar.data.models;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeetingRepository {
    private static MeetingRepository instance;

    private final Map<Integer, Meeting> allMeetings = new HashMap<>();

    private MeetingRepository() {
        generateMockData();
    }

    public static synchronized MeetingRepository getInstance() {
        if (instance == null) {
            instance = new MeetingRepository();
        }
        return instance;
    }

    public Meeting getMeetingById(int id) {
        return allMeetings.get(id);
    }

    public void addMeeting(Meeting meeting) {
        allMeetings.put(meeting.getId(), meeting);
    }

    public List<Meeting> getAllMeetings() {
        return new ArrayList<>(allMeetings.values());
    }

    /**
     * Mapuje spotkania na format czytelny dla kalendarza.
     * Grupuje spotkania według daty (CalendarDay).
     */
    public Map<CalendarDay, List<Meeting>> getMeetingsGroupedByDate() {
        Map<CalendarDay, List<Meeting>> map = new HashMap<>();
        for (Meeting m : allMeetings.values()) {
            try {
                String[] dateParts = m.getStartTime().split("T")[0].split("-");
                CalendarDay day = CalendarDay.from(
                        Integer.parseInt(dateParts[0]),
                        Integer.parseInt(dateParts[1]) - 1,
                        Integer.parseInt(dateParts[2])
                );

                if (!map.containsKey(day)) {
                    map.put(day, new ArrayList<>());
                }
                map.get(day).add(m);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }


    private void generateMockData() {
        Meeting m1 = new Meeting();
        m1.setId(1);
        m1.setStartTime("2026-04-20T10:00:00");
        m1.setDuration("02:00:00");
        m1.setName("Spotkanie projektowe");
        MeetingLocation m1_location = new MeetingLocation();
        m1_location.setAddress("Online");
        m1.setLocation(m1_location);
        m1.getMembers().add(new MeetingMember(101, "Jan Kowalski"));
        m1.getMembers().add(new MeetingMember(102, "Anna Nowak"));
        addMeeting(m1);
    }
}