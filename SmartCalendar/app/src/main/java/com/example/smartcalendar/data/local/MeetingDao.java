package com.example.smartcalendar.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.smartcalendar.data.models.meeting.Meeting;

import java.util.List;

@Dao
public interface MeetingDao {
    @Query("SELECT * FROM meetings")
    List<Meeting> getAllMeetings();
    @Query("SELECT * FROM meetings WHERE id = :id LIMIT 1")
    Meeting getMeetingById(int id);
    @Query("SELECT * FROM meetings WHERE id = :id LIMIT 1")
    LiveData<Meeting> getMeetingByIdLive(int id);
    @Query("SELECT * FROM meetings")
    LiveData<List<Meeting>> getAllMeetingsLive();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMeeting(Meeting meeting);

    @Delete
    void delete(Meeting meeting);

    @Query("DELETE FROM meetings")
    void deleteAllMeetings();
}