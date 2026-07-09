package com.example.socialmediaadiction.Model;

import com.google.gson.annotations.SerializedName;

public class AppUsageData {

    @SerializedName("id")
    public String id;

    @SerializedName("user_id")
    public String user_id;

    @SerializedName("date")
    public String date;

    @SerializedName("youtube")
    public String youtube;

    @SerializedName("instagram")
    public String instagram;

    @SerializedName("facebook")
    public String facebook;

    @SerializedName("whatsapp")
    public String whatsapp;

    @SerializedName("chrome")
    public String chrome;

    @SerializedName("total_minutes")
    public String total_minutes;
    public String getTotal_minutes() {
        return total_minutes;
    }

    @SerializedName("created_at")
    public String created_at;
    @SerializedName("mood_score")
    public String mood_score;
    public String getMood_score() {
        return mood_score;   // ✅ now this will work
    }
}
