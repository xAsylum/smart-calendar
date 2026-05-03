package com.example.smartcalendar.data.local;

import androidx.room.TypeConverter;
import com.example.smartcalendar.data.models.meeting.MeetingMember;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class Converters {
    private static Gson gson = new Gson();

    @TypeConverter
    public static String fromMemberList(List<MeetingMember> members) {
        return gson.toJson(members);
    }

    @TypeConverter
    public static List<MeetingMember> toMemberList(String data) {
        Type listType = new TypeToken<List<MeetingMember>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String fromStringList(List<String> messages) {
        return gson.toJson(messages);
    }

    @TypeConverter
    public static List<String> toStringList(String data) {
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(data, listType);
    }
}