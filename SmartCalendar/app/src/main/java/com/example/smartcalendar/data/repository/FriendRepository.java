package com.example.smartcalendar.data.repository;

import android.content.Context;
import android.util.Log;
import com.example.smartcalendar.data.local.TokenManager;
import com.example.smartcalendar.data.models.User;
import com.example.smartcalendar.data.models.friend.Friend;
import com.example.smartcalendar.data.models.friend.FriendListResponse;
import com.example.smartcalendar.data.models.friendrequest.FriendRequestSchema;
import com.example.smartcalendar.data.network.NetworkClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendRepository {
    private static FriendRepository instance;

    private FriendRepository() {}

    public static synchronized FriendRepository getInstance() {
        if (instance == null) {
            instance = new FriendRepository();
        }
        return instance;
    }

    private String getAuthHeader(Context context) {
        return "Bearer " + TokenManager.getInstance().getToken(context);
    }

    public void sendRequest(Context context, String username, Runnable onResult) {
        NetworkClient.getApiService().sendFriendRequest(getAuthHeader(context), new FriendRequestSchema(username))
                .enqueue(new SimpleCallback(onResult));
    }

    public void acceptRequest(Context context, int userId, Runnable onResult) {
        NetworkClient.getApiService().acceptFriendRequest(getAuthHeader(context), userId)
                .enqueue(new SimpleCallback(onResult));
    }

    public interface FriendsLoadListener {
        void onSuccess(List<Friend> friends);
        void onFailure(String errorMessage);
    }

    public void getFriends(Context context, FriendsLoadListener listener) {
        NetworkClient.getApiService().getFriends(getAuthHeader(context))
                .enqueue(new Callback<FriendListResponse>() {
                    @Override
                    public void onResponse(Call<FriendListResponse> call, Response<FriendListResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (listener != null) {
                                listener.onSuccess(response.body().getFriends());
                            }
                        } else {
                            if (listener != null) listener.onFailure("Błąd serwera: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<FriendListResponse> call, Throwable t) {
                        if (listener != null) listener.onFailure(t.getMessage());
                        Log.e("API", "Błąd sieci przy pobieraniu znajomych", t);
                    }
                });
    }
    public void rejectRequest(Context context, int userId, Runnable onResult) {
        NetworkClient.getApiService().rejectFriendRequest(getAuthHeader(context), userId)
                .enqueue(new SimpleCallback(onResult));
    }

    public void cancelRequest(Context context, int userId, Runnable onResult) {
        NetworkClient.getApiService().cancelFriendRequest(getAuthHeader(context), userId)
                .enqueue(new SimpleCallback(onResult));
    }

    private static class SimpleCallback implements Callback<Void> {
        private final Runnable onResult;
        public SimpleCallback(Runnable onResult) { this.onResult = onResult; }
        @Override
        public void onResponse(Call<Void> call, Response<Void> response) { if (onResult != null) onResult.run(); }
        @Override
        public void onFailure(Call<Void> call, Throwable t) { Log.e("API", "Friend action failed", t); }
    }

    public void getAvailableFriends(Context context, FriendRepository.FriendsLoadListener listener) {
        FriendRepository.getInstance().getFriends(context, listener);
    }

}