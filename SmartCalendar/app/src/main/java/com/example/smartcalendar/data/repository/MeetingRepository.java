
package com.example.smartcalendar.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.smartcalendar.data.local.AppDatabase;
import com.example.smartcalendar.data.local.MeetingDao;
import com.example.smartcalendar.data.local.TokenManager;
import com.example.smartcalendar.data.models.meeting.Meeting;
import com.example.smartcalendar.data.models.meeting.MeetingListResponse;
import com.example.smartcalendar.data.network.NetworkClient;
import com.example.smartcalendar.ui.meeting.MeetingViewModel;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.LiveData;
import androidx.room.Room;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeetingRepository {
    private static MeetingRepository instance;
    private final MeetingDao meetingDao;

    private MeetingRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.meetingDao = db.meetingDao();
    }

    public static synchronized MeetingRepository getInstance(Context context) {
        if (instance == null) {
            instance = new MeetingRepository(context);
        }
        instance.fetchMeetingsFromServer(context);
        return instance;
    }

    private String getAuthHeader(Context context) {
        String token = TokenManager.getInstance().getToken(context);
        return token != null ? "Bearer " + token : null;
    }

    public void fetchMeetingsFromServer(Context context) {
        String header = getAuthHeader(context);
        if (header == null) return;

        NetworkClient.getApiService().getMeetings(header).enqueue(new Callback<MeetingListResponse>() {
            @Override
            public void onResponse(Call<MeetingListResponse> call, Response<MeetingListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Meeting> meetings = response.body().getMeetings();

                    new Thread(() -> {
                        for (Meeting m : meetings) {
                            meetingDao.insertMeeting(m);
                        }
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<MeetingListResponse> call, Throwable t) {
                Log.e("API", "Błąd synchronizacji z serwerem", t);
            }
        });
    }

    public void addMeeting(Context context, Meeting meeting, MeetingViewModel.OnMeetingCreatedListener listener){
        meetingDao.insertMeeting(meeting);

        String header = getAuthHeader(context);
        if (header == null) return;
        NetworkClient.getApiService().createMeeting(header, meeting).enqueue(new Callback<Meeting>() {
            @Override
            public void onResponse(Call<Meeting> call, Response<Meeting> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Meeting serverMeeting = response.body();

                    new Thread(() -> {
                        meetingDao.insertMeeting(serverMeeting);
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (listener != null) listener.onCreated(serverMeeting.getId());
                        });
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<Meeting> call, Throwable t) {
                Log.e("API", "Błąd tworzenia spotkania", t);
            }
        });
    }

    public void updateMeeting(Context context, Meeting meeting) {
        meeting.syncApiFields();
        meetingDao.insertMeeting(meeting);
        String header = getAuthHeader(context);
        if (header == null) return;

        NetworkClient.getApiService().updateMeeting(header, meeting.getId(), meeting).enqueue(new Callback<Meeting>() {
            @Override
            public void onResponse(Call<Meeting> call, Response<Meeting> response) {
                Log.d("API", "Spotkanie zaktualizowane na serwerze");
                    if (response.isSuccessful() && response.body() != null) {
                        new Thread(() -> meetingDao.insertMeeting(response.body())).start();
                    }
            }

            @Override
            public void onFailure(Call<Meeting> call, Throwable t) {
                Log.e("API", "Błąd aktualizacji na serwerze", t);
            }
        });
    }


    public List<Meeting> getAllMeetings() {
        return meetingDao.getAllMeetings();
    }

    public LiveData<List<Meeting>> getAllMeetingsLive() {
        return meetingDao.getAllMeetingsLive();
    }

    public Meeting getMeetingById(int id) {
        return meetingDao.getMeetingById(id);
    }

    public LiveData<Meeting> getMeetingByIdLive(int id) {
        return meetingDao.getMeetingByIdLive(id);
    }


    public Map<CalendarDay, List<Meeting>> getMeetingsGroupedByDate() {
        Map<CalendarDay, List<Meeting>> map = new HashMap<>();
        for (Meeting m : getAllMeetings()) {
            try {
                if (m.getStartTime() == null || !m.getStartTime().contains("T")) continue;
                String[] dateParts = m.getStartTime().split("T")[0].split("-");
                CalendarDay day = CalendarDay.from(
                        Integer.parseInt(dateParts[0]),
                        Integer.parseInt(dateParts[1]) - 1,
                        Integer.parseInt(dateParts[2])
                );
                if (!map.containsKey(day)) map.put(day, new ArrayList<>());
                map.get(day).add(m);
            } catch (Exception e) { e.printStackTrace(); }
        }
        return map;
    }
}