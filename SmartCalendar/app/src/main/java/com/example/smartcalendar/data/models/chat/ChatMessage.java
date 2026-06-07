package com.example.smartcalendar.data.models.chat;

import com.google.gson.annotations.SerializedName;

public class ChatMessage {
    @SerializedName("id")
    private int id;
    
    @SerializedName("sender_id")
    private int senderId;
    
    @SerializedName("sender_name")
    private String senderName;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("meeting_id")
    private int meetingId;

    public ChatMessage() {}

    public ChatMessage(String senderName, String message) {
        this.senderName = senderName;
        this.message = message;
    }

    public int getId() { return id; }
    public String getSenderName() { return senderName; }
    public String getMessage() { return message; }
    public int getSenderId() { return senderId; }
    public int getMeetingId() { return meetingId; }
}
