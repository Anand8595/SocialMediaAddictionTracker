package com.example.socialmediaadiction.Api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    public static final String BASE_URL = "https://smartbilling.mycloudspace.in/ecommerce/Android/";
    private static RetrofitClient mInstance;
    private static Retrofit retrofit;

    private RetrofitClient(){
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance(){
        if (mInstance == null ){
            mInstance = new RetrofitClient();
        }
        return mInstance;

    }

    public ApiService getApi(){
        return retrofit.create(ApiService.class);
    }

}






