package com.example.smartcalendar.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.smartcalendar.data.local.AppDatabase;
import com.example.smartcalendar.data.local.MeetingDao;
import com.example.smartcalendar.data.local.TokenManager;
import com.example.smartcalendar.data.models.friend.Friend;
import com.example.smartcalendar.data.models.meeting.Meeting;
import com.example.smartcalendar.data.models.meeting.MeetingListResponse;
import com.example.smartcalendar.data.models.meeting.MeetingMember;
import com.example.smartcalendar.data.models.meeting.MeetingMembersResponse;
import com.example.smartcalendar.data.network.NetworkClient;
import com.example.smartcalendar.ui.meeting.MeetingViewModel;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
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
            instance.fetchMeetingsFromServer(context);
        }
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
                        meetingDao.deleteAllMeetings();
                        for (Meeting m : meetings) {
                            m.syncApiFields(); // Ensure latitude/longitude are synced from location object
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
        String header = getAuthHeader(context);
        if (header == null) return;
        NetworkClient.getApiService().createMeeting(header, meeting).enqueue(new Callback<Meeting>() {
            @Override
            public void onResponse(Call<Meeting> call, Response<Meeting> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Meeting serverMeeting = response.body();
                    serverMeeting.syncApiFields();

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
                        Meeting m = response.body();
                        m.syncApiFields();
                        new Thread(() -> meetingDao.insertMeeting(m)).start();
                    }
            }

            @Override
            public void onFailure(Call<Meeting> call, Throwable t) {
                Log.e("API", "Błąd aktualizacji na serwerze", t);
            }
        });
    }

    public void fetchMeetingMembers(Context context, int meetingId, MembersLoadListener listener) {
        String header = getAuthHeader(context);
        if (header == null) return;

        NetworkClient.getApiService().getMeetingMembers(header, meetingId).enqueue(new Callback<MeetingMembersResponse>() {
            @Override
            public void onResponse(Call<MeetingMembersResponse> call, Response<MeetingMembersResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MeetingMember> newMembers = response.body().getMembers();
                    if (listener != null) listener.onSuccess(newMembers);
                } else {
                    if (listener != null) listener.onFailure("Błąd serwera: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MeetingMembersResponse> call, Throwable t) {
                if (listener != null) listener.onFailure(t.getMessage());
            }
        });
    }

    public void setMeetingMembers(Context context, int meetingId, List<Friend> selectedUsers, Runnable onSuccess) {
        String header = getAuthHeader(context);
        if (header == null) return;

        List<Integer> memberIds = new ArrayList<>();
        for (Friend user : selectedUsers) {
            memberIds.add(user.getId());
        }

        NetworkClient.getApiService().updateMeetingMembers(header, meetingId, memberIds)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d("API", "Członkowie spotkania zaktualizowani.");
                            if (onSuccess != null) onSuccess.run();
                        } else {
                            Log.e("API", "Błąd aktualizacji członków: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("API", "Błąd połączenia", t);
                    }
                });
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

    public interface MembersLoadListener {
        void onSuccess(List<MeetingMember> members);
        void onFailure(String error);
    }
}
