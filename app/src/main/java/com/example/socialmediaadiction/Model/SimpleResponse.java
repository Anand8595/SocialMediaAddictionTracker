package com.example.socialmediaadiction.Model;

import com.google.gson.annotations.SerializedName;

public class SimpleResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
}