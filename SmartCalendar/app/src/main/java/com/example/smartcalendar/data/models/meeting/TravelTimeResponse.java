package com.example.smartcalendar.data.models.meeting;

import com.google.gson.annotations.SerializedName;

public class TravelTimeResponse {
    @SerializedName("duration_text")
    private String durationText;

    @SerializedName("duration_seconds")
    private long durationSeconds;

    @SerializedName("distance_text")
    private String distanceText;

    @SerializedName("mode")
    private String mode;

    public String getDurationText() { return durationText; }
    public long getDurationSeconds() { return durationSeconds; }
    public String getDistanceText() { return distanceText; }
    public String getMode() { return mode; }
}
