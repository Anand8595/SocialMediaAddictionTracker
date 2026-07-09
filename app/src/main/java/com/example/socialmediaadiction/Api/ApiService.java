package com.example.socialmediaadiction.Api;


import com.example.socialmediaadiction.Model.ApiResponse;
import com.example.socialmediaadiction.Model.LoginResponse;
import com.example.socialmediaadiction.Model.RegisterResponse;
import com.example.socialmediaadiction.Model.ReportResponse;
import com.example.socialmediaadiction.Model.SimpleResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @FormUrlEncoded
    @POST("register.php")
    Call<RegisterResponse> useradd(
            @Field("name") String name,
            @Field("email") String email,
            @Field("password") String password,
            @Field("phone") String phone,
            @Field("address") String address,
            @Field("city") String city,
            @Field("date_of_birth") String dob,
            @Field("gender") String gender
    );

    @FormUrlEncoded
    @POST("login.php")
    Call<LoginResponse> userlogin(
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("save_insights.php")
    Call<SimpleResponse> saveDailyUsage(
            @Field("user_id") String userId,
            @Field("date") String date,
            @Field("youtube") int youtube,
            @Field("instagram") int instagram,
            @Field("facebook") int facebook,
            @Field("whatsapp") int whatsapp,
            @Field("chrome") int chrome
    );

    @FormUrlEncoded
    @POST("view_insights.php")
    Call<ApiResponse> getAppUsage(
            @Field("user_id") String userId,
            @Field("date") String date
    );
    @FormUrlEncoded
    @POST("get_usage_report.php")
    Call<ReportResponse> getReport(
            @Field("user_id") String user_id
    );
    //mood_save.php
    @FormUrlEncoded
    @POST("save_mood.php")
    Call<SimpleResponse> saveMood(
            @Field("user_id") String userId,
            @Field("date") String date,
            @Field("mood_score") int moodScore
    );
    @FormUrlEncoded
    @POST("get_mood_usage.php")
    Call<ApiResponse> getMoodUsage(
            @Field("user_id") String userId
    );
}
