package com.example.smartcalendar.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private String savedToken = null;
    private int savedUserId = -1;
    private static TokenManager instance = null;

    public static TokenManager getInstance() {
        if (instance == null) {
            instance = new TokenManager();
        }
        return instance;
    }

    public void saveToken(Context context, String token) {
        savedToken = token;
        SharedPreferences prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("auth_token", token).apply();
    }

    public String getToken(Context context) {
        if (savedToken != null) return savedToken;
        SharedPreferences prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        savedToken = prefs.getString("auth_token", null);
        return savedToken;
    }

    public void saveUserId(Context context, int userId) {
        savedUserId = userId;
        SharedPreferences prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        prefs.edit().putInt("user_id", userId).apply();
    }

    public int getUserId(Context context) {
        if (savedUserId != -1) return savedUserId;
        SharedPreferences prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        savedUserId = prefs.getInt("user_id", -1);
        return savedUserId;
    }

    public void deleteToken(Context context) {
        savedToken = null;
        savedUserId = -1;
        SharedPreferences prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        prefs.edit().remove("auth_token").remove("user_id").apply();
    }
}
