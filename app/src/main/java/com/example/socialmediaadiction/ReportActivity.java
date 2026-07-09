package com.example.socialmediaadiction;

import android.content.Intent;
import android.graphics.fonts.Font;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaadiction.Api.ApiService;
import com.example.socialmediaadiction.Fragment.Constants;
import com.example.socialmediaadiction.Fragment.SessionManager;
import com.example.socialmediaadiction.Model.ReportResponse;
import com.example.socialmediaadiction.Model.UsageModel;
import com.example.socialmediaadiction.R;
import com.example.socialmediaadiction.ReportAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class ReportActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ReportAdapter adapter;
    List<UsageModel> list;
    BottomNavigationView bottomNavigationView;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {

                finish();

//            } else if (id == R.id.nav_compare) {
//
//                startActivity(new Intent(this, ComparisonActivity.class));

            } else if (id == R.id.nav_rewards) {

                startActivity(new Intent(this, RewardsActivity.class));

            } else if (id == R.id.nav_focus) {

                startActivity(new Intent(this, FocusModeActivity.class));

            } else if (id == R.id.nav_insights) {

                startActivity(new Intent(this, InsightsActivity.class));

            }
            else if (id == R.id.nav_logout) {

                SessionManager sessionManager =
                        new SessionManager(this);

                sessionManager.logoutUser();

                Intent intent =
                        new Intent(this, MainActivity.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);

                finish();

            }

            overridePendingTransition(0,0);

            return true;
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        sessionManager = new SessionManager(this);
        list = new ArrayList<>();

        loadReport();

    }

    private void loadReport() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://smartbilling.mycloudspace.in/ecommerce/Android/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);

        String userid = sessionManager.getStringData(Constants.KEY_ID);

        Call<ReportResponse> call = api.getReport(userid);

        call.enqueue(new Callback<ReportResponse>() {
            @Override
            public void onResponse(Call<ReportResponse> call, Response<ReportResponse> response) {

                list = response.body().getData();

                adapter = new ReportAdapter(ReportActivity.this, list);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<ReportResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}