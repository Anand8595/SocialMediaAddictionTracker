package com.example.socialmediaadiction.Gemini;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatApiService {

    @POST("chatbot.php")

    Call<ChatResponse> sendMessage(

            @Body ChatRequest request
    );
}