package com.example.socialmediaadiction;

import android.app.AppOpsManager;
import android.app.DatePickerDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediaadiction.Api.ApiService;
import com.example.socialmediaadiction.Fragment.Constants;
import com.example.socialmediaadiction.Fragment.SessionManager;
import com.example.socialmediaadiction.Model.ApiResponse;
import com.example.socialmediaadiction.Model.AppUsageData;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ComparisonActivity extends AppCompatActivity {

    HorizontalBarChart barChart;
    TextView tvTopApp, tvSuggestion;
    Button btnSelectDate;

    // ✅ ICONS
    ImageView iconYoutube, iconInstagram, iconFacebook, iconWhatsapp, iconChrome;

    SessionManager sessionManager;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparison);

        // ✅ INIT VIEWS
        barChart = findViewById(R.id.barChart);
        tvTopApp = findViewById(R.id.tvTopApp);
        tvSuggestion = findViewById(R.id.tvSuggestion);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {

                finish();


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

        // ✅ INIT ICONS
        iconYoutube = findViewById(R.id.iconYoutubeChart);
        iconInstagram = findViewById(R.id.iconInstagramChart);
        iconFacebook = findViewById(R.id.iconFacebookChart);
        iconWhatsapp = findViewById(R.id.iconWhatsappChart);
        iconChrome = findViewById(R.id.iconChromeChart);

        // ✅ SET ICONS
        setIcon(iconYoutube, "com.google.android.youtube");
        setIcon(iconInstagram, "com.instagram.android");
        setIcon(iconFacebook, "com.facebook.katana");
        setIcon(iconWhatsapp, "com.whatsapp");
        setIcon(iconChrome, "com.android.chrome");

        sessionManager = new SessionManager(this);

        if (!hasUsagePermission()) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            Toast.makeText(this, "Please grant Usage Access permission.", Toast.LENGTH_LONG).show();
            return;
        }

        showTodayUsage();

        btnSelectDate.setOnClickListener(v -> showDatePicker());
    }

    // ✅ ICON METHOD
    private void setIcon(ImageView imageView, String packageName) {
        try {
            Drawable icon = getPackageManager().getApplicationIcon(packageName);
            imageView.setImageDrawable(icon);
        } catch (Exception e) {
            imageView.setImageResource(android.R.drawable.sym_def_app_icon);
        }
    }

    private boolean hasUsagePermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void showTodayUsage() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startTime = calendar.getTimeInMillis();

        List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        if (stats == null || stats.isEmpty()) {
            Toast.makeText(this, "No usage data found.", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Long> usageMap = new HashMap<>();

        String[] apps = {
                "com.google.android.youtube",
                "com.instagram.android",
                "com.facebook.katana",
                "com.whatsapp",
                "com.android.chrome"
        };

        for (UsageStats us : stats) {
            for (String app : apps) {
                if (us.getPackageName().equals(app)) {
                    usageMap.put(app, us.getTotalTimeInForeground() / 60000);
                }
            }
        }

        AppUsageData data = new AppUsageData();
        data.date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        data.youtube = String.valueOf(usageMap.getOrDefault(apps[0], 0L));
        data.instagram = String.valueOf(usageMap.getOrDefault(apps[1], 0L));
        data.facebook = String.valueOf(usageMap.getOrDefault(apps[2], 0L));
        data.whatsapp = String.valueOf(usageMap.getOrDefault(apps[3], 0L));
        data.chrome = String.valueOf(usageMap.getOrDefault(apps[4], 0L));

        showChart(data);
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, y, m, d) -> {
                    String date = String.format("%04d-%02d-%02d", y, m + 1, d);
                    fetchDataFromApi(date);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

//    private void fetchDataFromApi(String date) {
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://smartbilling.mycloudspace.in/ecommerce/Android/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        ApiService api = retrofit.create(ApiService.class);
//        String userId = sessionManager.getStringData(Constants.KEY_ID);
//
//        api.getAppUsage(userId, date).enqueue(new Callback<ApiResponse>() {
//            @Override
//            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
//                if (response.body() != null && response.body().data != null && !response.body().data.isEmpty()) {
//                    showChart(response.body().data.get(0));
//                } else {
//                    Toast.makeText(ComparisonActivity.this, "No data", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ApiResponse> call, Throwable t) {
//                Toast.makeText(ComparisonActivity.this, "Error", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void fetchDataFromApi(String date) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://smartbilling.mycloudspace.in/ecommerce/Android/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);
        String userId = sessionManager.getStringData(Constants.KEY_ID);

        api.getAppUsage(userId, date).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (response.body() != null &&
                        response.body().data != null &&
                        !response.body().data.isEmpty()) {

                    // ✅ Normal case (data exists)
                    showChart(response.body().data.get(0));

                } else {

                    // 🔥 NEW: fallback for missing data
                    AppUsageData empty = new AppUsageData();
                    empty.youtube = "0";
                    empty.instagram = "0";
                    empty.facebook = "0";
                    empty.whatsapp = "0";
                    empty.chrome = "0";

                    showChart(empty);

                    tvTopApp.setText("No usage data");
                    tvSuggestion.setText("No activity recorded for this day");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(ComparisonActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChart(AppUsageData u) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, Float.parseFloat(u.youtube)));
        entries.add(new BarEntry(1, Float.parseFloat(u.instagram)));
        entries.add(new BarEntry(2, Float.parseFloat(u.facebook)));
        entries.add(new BarEntry(3, Float.parseFloat(u.whatsapp)));
        entries.add(new BarEntry(4, Float.parseFloat(u.chrome)));

        BarDataSet set = new BarDataSet(entries, "Usage");
        set.setColors(
                Color.RED,
                Color.MAGENTA,
                Color.BLUE,
                Color.GREEN,
                Color.YELLOW
        );

        barChart.setData(new BarData(set));
        barChart.setDescription(new Description());
        barChart.animateY(1200);
        barChart.invalidate();

        // TOP APP
        HashMap<String, Float> map = new HashMap<>();
        map.put("YouTube", Float.parseFloat(u.youtube));
        map.put("Instagram", Float.parseFloat(u.instagram));
        map.put("Facebook", Float.parseFloat(u.facebook));
        map.put("WhatsApp", Float.parseFloat(u.whatsapp));
        map.put("Chrome", Float.parseFloat(u.chrome));

        String top = "";
        float max = 0;

        for (String key : map.keySet()) {
            if (map.get(key) > max) {
                max = map.get(key);
                top = key;
            }
        }

        tvTopApp.setText("🔥 Top App: " + top);
        tvSuggestion.setText(generateTip(top));
    }

    private String generateTip(String app) {
        switch (app) {
            case "YouTube": return "Reduce video time 🎥";
            case "Instagram": return "Avoid endless scrolling 📸";
            case "Facebook": return "Limit feed usage 💬";
            case "WhatsApp": return "Take message breaks 💭";
            case "Chrome": return "Control browsing 🌐";
            default: return "Good usage 👍";
        }
    }
}

//package com.example.socialmediaadiction;
//
//import android.app.AppOpsManager;
//import android.app.DatePickerDialog;
//import android.app.usage.UsageStats;
//import android.app.usage.UsageStatsManager;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.provider.Settings;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.socialmediaadiction.Api.ApiService;
//import com.example.socialmediaadiction.Fragment.Constants;
//import com.example.socialmediaadiction.Fragment.SessionManager;
//import com.example.socialmediaadiction.Model.ApiResponse;
//import com.example.socialmediaadiction.Model.AppUsageData;
//import com.github.mikephil.charting.charts.HorizontalBarChart;
//import com.github.mikephil.charting.components.Description;
//import com.github.mikephil.charting.data.BarData;
//import com.github.mikephil.charting.data.BarDataSet;
//import com.github.mikephil.charting.data.BarEntry;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.List;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//
//public class ComparisonActivity extends AppCompatActivity {
//
//    HorizontalBarChart barChart;
//    TextView tvTopApp, tvSuggestion, tvLegend;
//    Button btnSelectDate;
//    SessionManager sessionManager;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_comparison);
//
//        barChart = findViewById(R.id.barChart);
//        tvTopApp = findViewById(R.id.tvTopApp);
//        tvSuggestion = findViewById(R.id.tvSuggestion);
//        tvLegend = findViewById(R.id.tvLegend);
//        btnSelectDate = findViewById(R.id.btnSelectDate);
//
//        sessionManager = new SessionManager(this);
//
//        tvLegend.setText(
//                "🔴 YouTube\n" +
//                        "🟣 Instagram\n" +
//                        "🔵 Facebook\n" +
//                        "🟢 WhatsApp\n" +
//                        "🟡 Chrome"
//        );
//
//        if (!hasUsagePermission()) {
//            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
//            Toast.makeText(this, "Please grant Usage Access permission.", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        // Show today's usage by default
//        showTodayUsage();
//
//        // Date picker to fetch API data for selected date
//        btnSelectDate.setOnClickListener(v -> showDatePicker());
//    }
//
//    private boolean hasUsagePermission() {
//        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
//        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
//                android.os.Process.myUid(), getPackageName());
//        return mode == AppOpsManager.MODE_ALLOWED;
//    }
//
//    // ---------------- TODAY USAGE ----------------
//    private void showTodayUsage() {
//        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
//        Calendar calendar = Calendar.getInstance();
//        long endTime = calendar.getTimeInMillis();
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        long startTime = calendar.getTimeInMillis();
//
//        List<UsageStats> stats = usageStatsManager.queryUsageStats(
//                UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
//
//        if (stats == null || stats.isEmpty()) {
//            Toast.makeText(this, "No usage data found for today.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        HashMap<String, Long> usageMap = new HashMap<>();
//        String[] targetApps = {
//                "com.google.android.youtube",
//                "com.instagram.android",
//                "com.facebook.katana",
//                "com.whatsapp",
//                "com.android.chrome"
//        };
//
//        for (UsageStats us : stats) {
//            for (String app : targetApps) {
//                if (us.getPackageName().equals(app)) {
//                    usageMap.put(app, us.getTotalTimeInForeground() / 60000); // minutes
//                }
//            }
//        }
//
//        AppUsageData todayData = new AppUsageData();
//        todayData.date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
//        todayData.youtube = String.valueOf(usageMap.getOrDefault("com.google.android.youtube", 0L));
//        todayData.instagram = String.valueOf(usageMap.getOrDefault("com.instagram.android", 0L));
//        todayData.facebook = String.valueOf(usageMap.getOrDefault("com.facebook.katana", 0L));
//        todayData.whatsapp = String.valueOf(usageMap.getOrDefault("com.whatsapp", 0L));
//        todayData.chrome = String.valueOf(usageMap.getOrDefault("com.android.chrome", 0L));
//
//        showChart(todayData);
//    }
//
//    // ---------------- SHOW DATE PICKER ----------------
//    private void showDatePicker() {
//        Calendar c = Calendar.getInstance();
//        int year = c.get(Calendar.YEAR);
//        int month = c.get(Calendar.MONTH);
//        int day = c.get(Calendar.DAY_OF_MONTH);
//
//        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
//                (view, selectedYear, selectedMonth, selectedDay) -> {
//                    String date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
//                    fetchDataFromApi(date);
//                }, year, month, day);
//
//        datePickerDialog.show();
//    }
//
//    // ---------------- FETCH API DATA ----------------
//    private void fetchDataFromApi(String date) {
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://smartbilling.mycloudspace.in/ecommerce/Android/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        ApiService apiService = retrofit.create(ApiService.class);
//        String userId = sessionManager.getStringData(Constants.KEY_ID);
//
//        Call<ApiResponse> call = apiService.getAppUsage(userId, date);
//        call.enqueue(new Callback<ApiResponse>() {
//            @Override
//            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
//                if (response.isSuccessful() && response.body() != null && response.body().data != null && !response.body().data.isEmpty()) {
//                    AppUsageData usage = response.body().data.get(0);
//                    showChart(usage);
//                } else {
//                    Toast.makeText(ComparisonActivity.this, "No data found for " + date, Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ApiResponse> call, Throwable t) {
//                Toast.makeText(ComparisonActivity.this, "API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    // ---------------- SHOW CHART ----------------
//    private void showChart(AppUsageData usage) {
//        ArrayList<BarEntry> entries = new ArrayList<>();
//        ArrayList<Integer> colors = new ArrayList<>();
//
//        entries.add(new BarEntry(0, Long.parseLong(usage.youtube)));
//        entries.add(new BarEntry(1, Long.parseLong(usage.instagram)));
//        entries.add(new BarEntry(2, Long.parseLong(usage.facebook)));
//        entries.add(new BarEntry(3, Long.parseLong(usage.whatsapp)));
//        entries.add(new BarEntry(4, Long.parseLong(usage.chrome)));
//
//        colors.add(Color.parseColor("#FF0000")); // YouTube
//        colors.add(Color.parseColor("#C13584")); // Instagram
//        colors.add(Color.parseColor("#1877F2")); // Facebook
//        colors.add(Color.parseColor("#25D366")); // WhatsApp
//        colors.add(Color.parseColor("#F4B400")); // Chrome
//
//        BarDataSet dataSet = new BarDataSet(entries, "Time Spent (minutes)");
//        dataSet.setColors(colors);
//
//        BarData data = new BarData(dataSet);
//        data.setValueTextSize(12f);
//
//        barChart.setData(data);
//
//        Description desc = new Description();
//        desc.setText("📊 App Usage: " + usage.date);
//        barChart.setDescription(desc);
//        barChart.animateY(1500);
//        barChart.invalidate();
//
//        // Top app & suggestion
//        HashMap<String, Long> map = new HashMap<>();
//        map.put("YouTube", Long.parseLong(usage.youtube));
//        map.put("Instagram", Long.parseLong(usage.instagram));
//        map.put("Facebook", Long.parseLong(usage.facebook));
//        map.put("WhatsApp", Long.parseLong(usage.whatsapp));
//        map.put("Chrome", Long.parseLong(usage.chrome));
//
//        String topApp = "";
//        long max = 0;
//        for (String app : map.keySet()) {
//            if (map.get(app) > max) {
//                max = map.get(app);
//                topApp = app;
//            }
//        }
//
//        tvTopApp.setText("🔥 Biggest Time Drain: " + topApp + " (" + max + " min)");
//        tvSuggestion.setText(generateTip(topApp));
//    }
//
//    private String generateTip(String app) {
//        switch (app) {
//            case "YouTube": return "🎥 Try watching fewer videos or setting a screen time limit.";
//            case "Instagram": return "📸 Avoid long scrolling sessions to stay productive.";
//            case "Facebook": return "💬 Reduce news feed browsing for mental clarity.";
//            case "WhatsApp": return "💭 Take message breaks to improve focus.";
//            case "Chrome": return "🌐 Manage browsing tabs and avoid endless surfing.";
//            default: return "✅ You're using your apps wisely!";
//        }
//    }
//}
