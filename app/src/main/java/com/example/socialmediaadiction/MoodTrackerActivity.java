package com.example.socialmediaadiction;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediaadiction.Api.ApiService;
import com.example.socialmediaadiction.Api.RetrofitClient;
import com.example.socialmediaadiction.Fragment.Constants;
import com.example.socialmediaadiction.Fragment.SessionManager;
import com.example.socialmediaadiction.Model.ApiResponse;
import com.example.socialmediaadiction.Model.AppUsageData;
import com.example.socialmediaadiction.Model.SimpleResponse;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoodTrackerActivity extends AppCompatActivity {

    RadioGroup moodGroup;
    Button btnSaveMood;
    TextView tvSummary;
    LineChart lineChart;
    BottomNavigationView bottomNavigationView;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_tracker);

        moodGroup = findViewById(R.id.moodGroup);
        btnSaveMood = findViewById(R.id.btnSaveMood);
        tvSummary = findViewById(R.id.tvSummary);
        lineChart = findViewById(R.id.lineChart);
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


        if (!hasUsagePermission()) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            Toast.makeText(this, "Please grant Usage Access permission.", Toast.LENGTH_LONG).show();
        }

        btnSaveMood.setOnClickListener(v -> saveMood());

       // showChart();       // OLD (local)
        loadRealData();    // NEW (DB)
    }

    private boolean hasUsagePermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void saveMood() {
        int selectedId = moodGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select your mood", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedMood = findViewById(selectedId);
        String mood = selectedMood.getText().toString();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // LOCAL SAVE (old)
        getSharedPreferences("MoodData", MODE_PRIVATE)
                .edit()
                .putString(today, mood)
                .apply();

        // DB SAVE (new)
        int moodScore = (int) getMoodScore(mood);
        String userId = new SessionManager(this).getStringData(Constants.KEY_ID);

        ApiService api = RetrofitClient.getInstance().getApi();

        api.saveMood(userId, today, moodScore).enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                Toast.makeText(MoodTrackerActivity.this, "Mood saved to DB", Toast.LENGTH_SHORT).show();
                loadRealData(); // refresh graph
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                Toast.makeText(MoodTrackerActivity.this, "DB save failed", Toast.LENGTH_SHORT).show();
            }
        });

        Toast.makeText(this, "Mood saved!", Toast.LENGTH_SHORT).show();
        showChart();
    }

    private void showChart() {
        Map<String, Long> usageMap = getLast7DaysUsage();
        Map<String, String> moodMap = (Map<String, String>) getSharedPreferences("MoodData", MODE_PRIVATE).getAll();

        ArrayList<Entry> entries = new ArrayList<>();

        int i = 0;
        for (String date : usageMap.keySet()) {
            String mood = moodMap.getOrDefault(date, "Neutral");
            float moodValue = getMoodScore(mood);
            entries.add(new Entry(i, moodValue));
            i++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Local Mood");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        lineChart.setData(new LineData(dataSet));
        lineChart.invalidate();
    }

    private Map<String, Long> getLast7DaysUsage() {
        Map<String, Long> usageMap = new LinkedHashMap<>();
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            long end = calendar.getTimeInMillis();
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            long start = calendar.getTimeInMillis();

            List<UsageStats> stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY, start, end);

            long totalTime = 0;
            for (UsageStats us : stats) {
                totalTime += us.getTotalTimeInForeground();
            }

            usageMap.put(String.valueOf(i), totalTime / 60000);
        }
        return usageMap;
    }

    private float getMoodScore(String mood) {
        if (mood == null) return 3f;

        mood = mood.toLowerCase();

        if (mood.contains("happy")) return 5f;
        if (mood.contains("neutral")) return 3f;
        if (mood.contains("sad")) return 1f;

        return 3f;
    }

    // ================= REAL DATA =================
   /* private void loadRealData() {

        String userId = new SessionManager(this).getStringData(Constants.KEY_ID);
        ApiService api = RetrofitClient.getInstance().getApi();

        api.getMoodUsage(userId).enqueue(new Callback<ApiResponse>() {

            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (response.body() != null && response.body().data != null && !response.body().data.isEmpty()) {

                    ArrayList<Entry> moodEntries = new ArrayList<>();
                    ArrayList<Entry> usageEntries = new ArrayList<>();

                    int i = 0;

                    for (AppUsageData item : response.body().data) {

                        // ✅ MOOD (1–5 EXACT FROM DB)
                        float mood = 3f;
                        try {
                            if (item.getMood_score() != null && !item.getMood_score().isEmpty()) {
                                mood = Float.parseFloat(item.getMood_score());
                            }
                        } catch (Exception e) {
                            mood = 3f;
                        }

                        // ✅ USAGE (MIN → HOURS)
                        float usage = 0f;
                        try {
                            if (item.getTotal_minutes() != null && !item.getTotal_minutes().isEmpty()) {
                                usage = Float.parseFloat(item.getTotal_minutes()) / 60f;
                            }
                        } catch (Exception e) {
                            usage = 0f;
                        }

                        moodEntries.add(new Entry(i, mood));
                        usageEntries.add(new Entry(i, usage));

                        i++;
                    }

                    // ✅ MOOD LINE
                    LineDataSet moodSet = new LineDataSet(moodEntries, "Mood");
                    moodSet.setColor(android.graphics.Color.parseColor("#00C853"));
                    moodSet.setCircleColor(android.graphics.Color.parseColor("#00C853"));
                    moodSet.setLineWidth(3f);
                    moodSet.setCircleRadius(5f);

                    // ✅ USAGE LINE
                    LineDataSet usageSet = new LineDataSet(usageEntries, "Usage (hrs)");
                    usageSet.setColor(android.graphics.Color.parseColor("#2962FF"));
                    usageSet.setCircleColor(android.graphics.Color.parseColor("#2962FF"));
                    usageSet.setLineWidth(3f);
                    usageSet.setCircleRadius(5f);

                    LineData lineData = new LineData(moodSet, usageSet);
                    lineChart.setData(lineData);

                    // ✅ FIXED AXIS (MOST IMPORTANT)
                    lineChart.getAxisLeft().setAxisMinimum(0f);
                    lineChart.getAxisLeft().setAxisMaximum(24f); // covers both mood & usage
                    lineChart.getAxisRight().setEnabled(false);

                    // X axis clean
                    lineChart.getXAxis().setGranularity(1f);
                    lineChart.getXAxis().setGranularityEnabled(true);
                    lineChart.getXAxis().setDrawGridLines(false);

                    // Remove description
                    lineChart.getDescription().setEnabled(false);

                    // Animation
                    lineChart.animateY(1000);
                    lineChart.invalidate();

                } else {
                    tvSummary.setText("No data available");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(MoodTrackerActivity.this,
                        "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    private void loadRealData() {

        String userId = new SessionManager(this).getStringData(Constants.KEY_ID);
        ApiService api = RetrofitClient.getInstance().getApi();

        api.getMoodUsage(userId).enqueue(new Callback<ApiResponse>() {

            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (response.body() != null && response.body().data != null && !response.body().data.isEmpty()) {

                    ArrayList<Entry> moodEntries = new ArrayList<>();
                    ArrayList<Entry> usageEntries = new ArrayList<>();

                    float totalUsage = 0f;
                    float totalMood = 0f;

                    int i = 0;

                    for (AppUsageData item : response.body().data) {

                        // ✅ MOOD (1–5)
                        float mood = 3f;
                        try {
                            if (item.getMood_score() != null && !item.getMood_score().isEmpty()) {
                                mood = Float.parseFloat(item.getMood_score());
                            }
                        } catch (Exception e) {
                            mood = 3f;
                        }

                        // ✅ USAGE (minutes → hours)
                        float usage = 0f;
                        try {
                            if (item.getTotal_minutes() != null && !item.getTotal_minutes().isEmpty()) {
                                usage = Float.parseFloat(item.getTotal_minutes()) / 60f;
                            }
                        } catch (Exception e) {
                            usage = 0f;
                        }

                        moodEntries.add(new Entry(i, mood));
                        usageEntries.add(new Entry(i, usage));

                        totalUsage += usage;
                        totalMood += mood;

                        i++;
                    }

                    // ================= GRAPH =================

                    LineDataSet moodSet = new LineDataSet(moodEntries, "Mood");
                    moodSet.setColor(android.graphics.Color.parseColor("#00C853"));
                    moodSet.setCircleColor(android.graphics.Color.parseColor("#00C853"));
                    moodSet.setLineWidth(3f);
                    moodSet.setCircleRadius(5f);

                    LineDataSet usageSet = new LineDataSet(usageEntries, "Usage (hrs)");
                    usageSet.setColor(android.graphics.Color.parseColor("#2962FF"));
                    usageSet.setCircleColor(android.graphics.Color.parseColor("#2962FF"));
                    usageSet.setLineWidth(3f);
                    usageSet.setCircleRadius(5f);

                    LineData lineData = new LineData(moodSet, usageSet);
                    lineChart.setData(lineData);

                    // ✅ AXIS FIX (VERY IMPORTANT)
                    lineChart.getAxisLeft().setAxisMinimum(0f);
                    lineChart.getAxisLeft().setAxisMaximum(24f); // better for mood + usage
                    lineChart.getAxisRight().setEnabled(false);

                    lineChart.getXAxis().setGranularity(1f);
                    lineChart.getXAxis().setGranularityEnabled(true);
                    lineChart.getXAxis().setDrawGridLines(false);

                    lineChart.getDescription().setEnabled(false);

                    lineChart.animateY(1000);
                    lineChart.invalidate();

                    // ================= AI SUMMARY =================

                    float avgUsage = totalUsage / response.body().data.size();
                    float avgMood = totalMood / response.body().data.size();

                    if (avgUsage >= 6) {
                        tvSummary.setText("🚨 Very high screen time! Reduce usage.");
                    }
                    else if (avgUsage > 4 && avgMood <= 2) {
                        tvSummary.setText("📉 High screen time is affecting your mood.");
                    }
                    else if (avgUsage > 4 && avgMood >= 4) {
                        tvSummary.setText("⚠️ High usage but mood still stable.");
                    }
                    else if (avgUsage < 2 && avgMood >= 4) {
                        tvSummary.setText("😊 Low usage improves your mood!");
                    }
                    else {
                        tvSummary.setText("⚖️ Balanced usage and mood.");
                    }

                } else {
                    tvSummary.setText("No data available");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(MoodTrackerActivity.this,
                        "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

//package com.example.socialmediaadiction;
//
//import android.annotation.SuppressLint;
//import android.app.AppOpsManager;
//import android.app.usage.UsageStats;
//import android.app.usage.UsageStatsManager;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.provider.Settings;
//import android.widget.Button;
//import android.widget.RadioButton;
//import android.widget.RadioGroup;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.github.mikephil.charting.charts.LineChart;
//import com.github.mikephil.charting.components.Description;
//import com.github.mikephil.charting.data.Entry;
//import com.github.mikephil.charting.data.LineData;
//import com.github.mikephil.charting.data.LineDataSet;
//import com.github.mikephil.charting.utils.ColorTemplate;
//
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//public class MoodTrackerActivity extends AppCompatActivity {
//
//    RadioGroup moodGroup;
//    Button btnSaveMood;
//    TextView tvSummary;
//    LineChart lineChart;
//
//    @SuppressLint("MissingInflatedId")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_mood_tracker);
//
//        moodGroup = findViewById(R.id.moodGroup);
//        btnSaveMood = findViewById(R.id.btnSaveMood);
//        tvSummary = findViewById(R.id.tvSummary);
//        lineChart = findViewById(R.id.lineChart);
//
//        if (!hasUsagePermission()) {
//            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
//            Toast.makeText(this, "Please grant Usage Access permission.", Toast.LENGTH_LONG).show();
//        }
//
//        btnSaveMood.setOnClickListener(v -> saveMood());
//        showChart();
//    }
//
//    private boolean hasUsagePermission() {
//        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
//        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
//                android.os.Process.myUid(), getPackageName());
//        return mode == AppOpsManager.MODE_ALLOWED;
//    }
//
//    private void saveMood() {
//        int selectedId = moodGroup.getCheckedRadioButtonId();
//        if (selectedId == -1) {
//            Toast.makeText(this, "Please select your mood", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        RadioButton selectedMood = findViewById(selectedId);
//        String mood = selectedMood.getText().toString();
//
//        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//        getSharedPreferences("MoodData", MODE_PRIVATE)
//                .edit()
//                .putString(today, mood)
//                .apply();
//
//        Toast.makeText(this, "Mood saved!", Toast.LENGTH_SHORT).show();
//        showChart();
//    }
//
//    private void showChart() {
//        Map<String, Long> usageMap = getLast7DaysUsage();
//        Map<String, String> moodMap = (Map<String, String>) getSharedPreferences("MoodData", MODE_PRIVATE).getAll();
//
//        ArrayList<Entry> entries = new ArrayList<>();
//        ArrayList<String> labels = new ArrayList<>();
//
//        int i = 0;
//        long totalUsage = 0;
//        int daysCount = 0;
//        for (String date : usageMap.keySet()) {
//            long usageMins = usageMap.get(date);
//            totalUsage += usageMins;
//            daysCount++;
//
//            String mood = moodMap.getOrDefault(date, "Neutral");
//            float moodValue = getMoodScore(mood);
//
//            entries.add(new Entry(i, moodValue));
//            labels.add(date.substring(5)); // show MM-dd
//            i++;
//        }
//
//        LineDataSet dataSet = new LineDataSet(entries, "Mood vs Usage (past 7 days)");
//        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
//        dataSet.setValueTextSize(12f);
//
//        lineChart.setData(new LineData(dataSet));
//        Description desc = new Description();
//        desc.setText("Mood Correlation Chart");
//        lineChart.setDescription(desc);
//        lineChart.animateY(1000);
//        lineChart.invalidate();
//
//        // Calculate average and show AI-style insight
//        long avgUsage = daysCount > 0 ? totalUsage / daysCount : 0;
//        if (avgUsage > 240) {
//            tvSummary.setText("📉 Your mood tends to drop when screen time > 4 hours/day.");
//        } else {
//            tvSummary.setText("😊 Good balance! Keep your screen time moderate.");
//        }
//    }
//
//    private Map<String, Long> getLast7DaysUsage() {
//        Map<String, Long> usageMap = new LinkedHashMap<>();
//        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//        Calendar calendar = Calendar.getInstance();
//
//        for (int i = 0; i < 7; i++) {
//            long end = calendar.getTimeInMillis();
//            calendar.add(Calendar.DAY_OF_MONTH, -1);
//            long start = calendar.getTimeInMillis();
//
//            List<UsageStats> stats = usageStatsManager.queryUsageStats(
//                    UsageStatsManager.INTERVAL_DAILY, start, end);
//
//            long totalTime = 0;
//            for (UsageStats us : stats) {
//                totalTime += us.getTotalTimeInForeground();
//            }
//
//            usageMap.put(sdf.format(new Date(start)), totalTime / 60000); // minutes
//        }
//        return usageMap;
//    }
//
//    private float getMoodScore(String mood) {
//        switch (mood) {
//            case "Happy 😊": return 3f;
//            case "Neutral 😐": return 2f;
//            case "Sad 😞": return 1f;
//            default: return 2f;
//        }
//    }
//}
