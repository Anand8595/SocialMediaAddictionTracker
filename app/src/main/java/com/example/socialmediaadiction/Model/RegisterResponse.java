package com.example.socialmediaadiction.Model;

public class RegisterResponse {
    private String status;
    private String message;
    private String image_url; // 👈 must match exactly with PHP key

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getImage_url() {
        return image_url;
    }
}
