package com.example.socialmediaadiction;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.socialmediaadiction.Fragment.Constants;
import com.example.socialmediaadiction.Fragment.SessionManager;
import com.example.socialmediaadiction.Model.RewardModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class RewardsActivity extends AppCompatActivity {

    TextView tvUsageValue, tvPoints, tvTotalPoints;
    ListView listRewards;
    SessionManager sessionManager;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        sessionManager = new SessionManager(this);
        String USER_ID = sessionManager.getStringData(Constants.KEY_ID);

        tvUsageValue = findViewById(R.id.tvUsageValue);
        tvPoints = findViewById(R.id.tvPoints);

        // ✅ NEW
        tvTotalPoints = findViewById(R.id.tvTotalPoints);
        listRewards = findViewById(R.id.listRewards);
        BottomNavigationView bottomNavigation =
                findViewById(R.id.bottomNavigation);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {

                startActivity(new Intent(this, HomeActivity.class));

            } else if (id == R.id.nav_reports) {

                startActivity(new Intent(this, ReportActivity.class));

            } else if (id == R.id.nav_compare) {

                startActivity(new Intent(this, ComparisonActivity.class));

            } else if (id == R.id.nav_focus) {

                startActivity(new Intent(this, FocusModeActivity.class));

            } else if (id == R.id.nav_logout) {

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

        if (!hasUsagePermission(this)) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }

        long usage = getTodayUsage(this);

        long hours = usage / 60;
        long minutes = usage % 60;

        tvUsageValue.setText(hours + "h " + minutes + "m");

        int points = (usage > 150) ? 0 : 2;
        tvPoints.setText(points + " Points");

        // ❌ REMOVED navigation to GamificationActivity

        if (!isWorkerScheduled()) {
            scheduleWorker(USER_ID);
            markWorkerScheduled();
        }

        // ✅ NEW → fetch reward history
        fetchRewards(USER_ID);
    }

    private boolean hasUsagePermission(Context context) {
        UsageStatsManager usm =
                (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        long time = System.currentTimeMillis();

        List<UsageStats> stats =
                usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000, time);

        return stats != null && !stats.isEmpty();
    }

    public static long getTodayUsage(Context context) {

        UsageStatsManager usageStatsManager =
                (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long startTime = calendar.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        android.app.usage.UsageEvents usageEvents =
                usageStatsManager.queryEvents(startTime, endTime);

        android.app.usage.UsageEvents.Event event =
                new android.app.usage.UsageEvents.Event();

        long totalForegroundTime = 0;
        long lastForegroundTime = 0;

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);

            if (event.getEventType() ==
                    android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {

                lastForegroundTime = event.getTimeStamp();
            }

            if (event.getEventType() ==
                    android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND
                    && lastForegroundTime != 0) {

                totalForegroundTime +=
                        (event.getTimeStamp() - lastForegroundTime);

                lastForegroundTime = 0;
            }
        }

        return totalForegroundTime / (1000 * 60); // minutes
    }

    // ✅ FIXED API
    interface ApiService {
        @FormUrlEncoded
        @POST("save_points.php")
        retrofit2.Call<ResponseBody> savePoints(
                @Field("user_id") String id,
                @Field("points") String points,
                @Field("date") String date
        );
    }
    private void fetchRewards(String userId)
    {
        String url = "https://smartbilling.mycloudspace.in/ecommerce/Android/get_rewards.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {

                        JSONObject obj = new JSONObject(response);

                        if (obj.getString("status").equals("success")) {

                            int totalPoints = obj.getInt("total_points");
                            JSONArray array = obj.getJSONArray("data");

                            tvTotalPoints.setText("⭐ Total Points: " + totalPoints);

                            // ✅ USE MODEL LIST (NOT STRING)
                            List<RewardModel> list = new ArrayList<>();

                            for (int i = 0; i < array.length(); i++) {

                                JSONObject item = array.getJSONObject(i);

                                int points = item.getInt("points");
                                String date = item.optString("reward_date", "Old");

                                list.add(new RewardModel(date, points));
                            }

                            // ✅ USE CUSTOM ADAPTER
                            RewardAdapter adapter = new RewardAdapter(this, list);
                            listRewards.setAdapter(adapter);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this,
                        "Error loading rewards", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", userId);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);

    }


    // 🔥 FETCH REWARD HISTORY
//    private void fetchRewards(String userId) {
//
//        String url = "https://smartbilling.mycloudspace.in/ecommerce/Android/get_rewards.php";
//
//        StringRequest request = new StringRequest(Request.Method.POST, url,
//                response -> {
//                    try {
//
//                        JSONObject obj = new JSONObject(response);
//
//                        if (obj.getString("status").equals("success")) {
//
//                            int totalPoints = obj.getInt("total_points");
//                            JSONArray array = obj.getJSONArray("data");
//
//                            tvTotalPoints.setText("⭐ Total Points: " + totalPoints);
//
//                            List<String> list = new ArrayList<>();
//
//                            for (int i = 0; i < array.length(); i++) {
//
//                                JSONObject item = array.getJSONObject(i);
//
//                                int points = item.getInt("points");
//                                String date = item.optString("reward_date", "Old");
//
//                                String text = date + " → " +
//                                        (points > 0 ? "✅ +" + points : "❌ " + points);
//
//                                list.add(text);
//                            }
//
//                            ArrayAdapter<String> adapter =
//                                    new ArrayAdapter<>(this,
//                                            android.R.layout.simple_list_item_1, list);
//
//                            listRewards.setAdapter(adapter);
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                },
//                error -> Toast.makeText(this,
//                        "Error loading rewards", Toast.LENGTH_SHORT).show()
//        ) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<>();
//                params.put("user_id", userId);
//                return params;
//            }
//        };
//
//        RequestQueue queue = Volley.newRequestQueue(this);
//        queue.add(request);
//    }

    public static class RewardWorker extends Worker {

        public RewardWorker(@NonNull Context context,
                            @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {

            Context context = getApplicationContext();

            SessionManager sessionManager = new SessionManager(context);
            String userId = sessionManager.getStringData(Constants.KEY_ID);

            long usage = getTodayUsage(context);
            int points = (usage > 150) ? 0 : 2;

            if (userId != null) {

                if (isInternetAvailable(context)) {

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("https://smartbilling.mycloudspace.in/ecommerce/Android/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    ApiService api = retrofit.create(ApiService.class);

                    syncOfflineData(context, userId, api);

                    try {
                        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(new java.util.Date());

                        api.savePoints(userId, String.valueOf(points), date).execute();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    saveOfflineData(context, points);
                }
            }

            return Result.success();
        }

        private boolean isInternetAvailable(Context context) {
            android.net.ConnectivityManager cm =
                    (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }

        private void saveOfflineData(Context context, int points) {

            try {
                android.content.SharedPreferences prefs =
                        context.getSharedPreferences("offline_data", MODE_PRIVATE);

                String json = prefs.getString("data", "[]");

                JSONArray array = new JSONArray(json);

                JSONObject obj = new JSONObject();
                obj.put("date", System.currentTimeMillis());
                obj.put("points", points);

                array.put(obj);

                prefs.edit().putString("data", array.toString()).apply();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void syncOfflineData(Context context, String userId, ApiService api) {

            try {
                android.content.SharedPreferences prefs =
                        context.getSharedPreferences("offline_data", MODE_PRIVATE);

                String json = prefs.getString("data", "[]");

                JSONArray array = new JSONArray(json);

                if (array.length() == 0) return;

                for (int i = 0; i < array.length(); i++) {

                    JSONObject obj = array.getJSONObject(i);

                    int points = obj.getInt("points");
                    long time = obj.getLong("date");

                    String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(new java.util.Date(time));

                    api.savePoints(userId, String.valueOf(points), date).execute();
                }

                prefs.edit().putString("data", "[]").apply();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void scheduleWorker(String userId) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 0);

        long delay = calendar.getTimeInMillis() - System.currentTimeMillis();

        if (delay < 0) {
            delay += 24 * 60 * 60 * 1000;
        }

        Data data = new Data.Builder()
                .putString("user_id", userId)
                .build();

        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(RewardWorker.class, 1, TimeUnit.DAYS)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(data)
                        .build();

        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
                        "reward_work",
                        ExistingPeriodicWorkPolicy.UPDATE,
                        request);
    }

    private boolean isWorkerScheduled() {
        return getSharedPreferences("worker_pref", MODE_PRIVATE)
                .getBoolean("scheduled", false);
    }

    private void markWorkerScheduled() {
        getSharedPreferences("worker_pref", MODE_PRIVATE)
                .edit()
                .putBoolean("scheduled", true)
                .apply();
    }
}

//package com.example.socialmediaadiction;
//
//import android.app.usage.UsageStats;
//import android.app.usage.UsageStatsManager;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.provider.Settings;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.work.Data;
//import androidx.work.ExistingPeriodicWorkPolicy;
//import androidx.work.PeriodicWorkRequest;
//import androidx.work.WorkManager;
//import androidx.work.Worker;
//import androidx.work.WorkerParameters;
//
//import com.example.socialmediaadiction.Fragment.Constants;
//import com.example.socialmediaadiction.Fragment.SessionManager;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.List;
//import java.util.Locale;
//import java.util.concurrent.TimeUnit;
//
//import okhttp3.ResponseBody;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//import retrofit2.http.Field;
//import retrofit2.http.FormUrlEncoded;
//import retrofit2.http.POST;
//
//public class RewardsActivity extends AppCompatActivity {
//
//    TextView tvUsageValue, tvPoints;
//    SessionManager sessionManager;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_rewards);
//
//        sessionManager = new SessionManager(this);
//        String USER_ID = sessionManager.getStringData(Constants.KEY_ID);
//
//        tvUsageValue = findViewById(R.id.tvUsageValue);
//        tvPoints = findViewById(R.id.tvPoints);
//
//        if (!hasUsagePermission(this)) {
//            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
//        }
//
//        long usage = getTodayUsage(this);
//
//        long hours = usage / 60;
//        long minutes = usage % 60;
//
//        tvUsageValue.setText(hours + "h " + minutes + "m");
//
//        int points = (usage > 150) ? 0 : 2;
//        tvPoints.setText(points + " Points");
//
//        new Handler().postDelayed(() -> {
//            startActivity(new Intent(RewardsActivity.this, GamificationActivity.class));
//        }, 1500);
//
//        if (!isWorkerScheduled()) {
//            scheduleWorker(USER_ID);
//            markWorkerScheduled();
//        }
//    }
//
//    private boolean hasUsagePermission(Context context) {
//        UsageStatsManager usm =
//                (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
//
//        long time = System.currentTimeMillis();
//
//        List<UsageStats> stats =
//                usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000, time);
//
//        return stats != null && !stats.isEmpty();
//    }
//
//    public static long getTodayUsage(Context context) {
//
//        UsageStatsManager usageStatsManager =
//                (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//
//        long startTime = calendar.getTimeInMillis();
//        long endTime = System.currentTimeMillis();
//
//        android.app.usage.UsageEvents usageEvents =
//                usageStatsManager.queryEvents(startTime, endTime);
//
//        android.app.usage.UsageEvents.Event event =
//                new android.app.usage.UsageEvents.Event();
//
//        long totalForegroundTime = 0;
//        long lastForegroundTime = 0;
//
//        while (usageEvents.hasNextEvent()) {
//            usageEvents.getNextEvent(event);
//
//            if (event.getEventType() ==
//                    android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {
//
//                lastForegroundTime = event.getTimeStamp();
//            }
//
//            if (event.getEventType() ==
//                    android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND
//                    && lastForegroundTime != 0) {
//
//                totalForegroundTime +=
//                        (event.getTimeStamp() - lastForegroundTime);
//
//                lastForegroundTime = 0;
//            }
//        }
//
//        return totalForegroundTime / (1000 * 60); // minutes
//    }
//
//    interface ApiService {
//        @FormUrlEncoded
//        @POST("save_points.php")
//        retrofit2.Call<ResponseBody> savePoints(
//                @Field("id") String id,
//                @Field("credit_points") String points,
//                @Field("date") String date
//        );
//    }
//
//    public static class RewardWorker extends Worker {
//
//        public RewardWorker(@NonNull Context context,
//                            @NonNull WorkerParameters workerParams) {
//            super(context, workerParams);
//        }
//
//        @NonNull
//        @Override
//        public Result doWork() {
//
//            Context context = getApplicationContext();
//
//            SessionManager sessionManager = new SessionManager(context);
//            String userId = sessionManager.getStringData(Constants.KEY_ID);
//
//            long usage = getTodayUsage(context);
//            int points = (usage > 150) ? 0 : 2;
//
//            if (userId != null) {
//
//                if (isInternetAvailable(context)) {
//
//                    Retrofit retrofit = new Retrofit.Builder()
//                            .baseUrl("https://smartbilling.mycloudspace.in/ecommerce/Android/")
//                            .addConverterFactory(GsonConverterFactory.create())
//                            .build();
//
//                    ApiService api = retrofit.create(ApiService.class);
//
//                    // Sync old data
//                    syncOfflineData(context, userId, api);
//
//                    // Send today
//                    try {
//                        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//                                .format(new java.util.Date());
//
//                        api.savePoints(userId, String.valueOf(points), date).execute();
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                } else {
//                    saveOfflineData(context, points);
//                }
//            }
//
//            return Result.success();
//        }
//
//        private boolean isInternetAvailable(Context context) {
//            android.net.ConnectivityManager cm =
//                    (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//            android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//            return activeNetwork != null && activeNetwork.isConnected();
//        }
//
//        private void saveOfflineData(Context context, int points) {
//
//            try {
//                android.content.SharedPreferences prefs =
//                        context.getSharedPreferences("offline_data", MODE_PRIVATE);
//
//                String json = prefs.getString("data", "[]");
//
//                JSONArray array = new JSONArray(json);
//
//                JSONObject obj = new JSONObject();
//                obj.put("date", System.currentTimeMillis());
//                obj.put("points", points);
//
//                array.put(obj);
//
//                prefs.edit().putString("data", array.toString()).apply();
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        private void syncOfflineData(Context context, String userId, ApiService api) {
//
//            try {
//                android.content.SharedPreferences prefs =
//                        context.getSharedPreferences("offline_data", MODE_PRIVATE);
//
//                String json = prefs.getString("data", "[]");
//
//                JSONArray array = new JSONArray(json);
//
//                if (array.length() == 0) return;
//
//                for (int i = 0; i < array.length(); i++) {
//
//                    JSONObject obj = array.getJSONObject(i);
//
//                    int points = obj.getInt("points");
//                    long time = obj.getLong("date");
//
//                    String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//                            .format(new java.util.Date(time));
//
//                    api.savePoints(userId, String.valueOf(points), date).execute();
//                }
//
//                prefs.edit().putString("data", "[]").apply();
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void scheduleWorker(String userId) {
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 23);
//        calendar.set(Calendar.MINUTE, 59);
//        calendar.set(Calendar.SECOND, 0);
//
//        long delay = calendar.getTimeInMillis() - System.currentTimeMillis();
//
//        if (delay < 0) {
//            delay += 24 * 60 * 60 * 1000;
//        }
//
//        Data data = new Data.Builder()
//                .putString("user_id", userId)
//                .build();
//
//        PeriodicWorkRequest request =
//                new PeriodicWorkRequest.Builder(RewardWorker.class, 1, TimeUnit.DAYS)
//                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
//                        .setInputData(data)
//                        .build();
//
//        WorkManager.getInstance(this)
//                .enqueueUniquePeriodicWork(
//                        "reward_work",
//                        ExistingPeriodicWorkPolicy.UPDATE,
//                        request);
//    }
//
//    private boolean isWorkerScheduled() {
//        return getSharedPreferences("worker_pref", MODE_PRIVATE)
//                .getBoolean("scheduled", false);
//    }
//
//    private void markWorkerScheduled() {
//        getSharedPreferences("worker_pref", MODE_PRIVATE)
//                .edit()
//                .putBoolean("scheduled", true)
//                .apply();
//    }
//}
//
////    package com.example.socialmediaadiction;
////
////    import android.app.usage.UsageStats;
////    import android.app.usage.UsageStatsManager;
////    import android.content.Context;
////    import android.content.Intent;
////    import android.os.Bundle;
////    import android.os.Handler;
////    import android.provider.Settings;
////    import android.util.Log;
////    import android.widget.TextView;
////
////    import androidx.annotation.NonNull;
////    import androidx.appcompat.app.AppCompatActivity;
////    import androidx.work.Data;
////    import androidx.work.ExistingPeriodicWorkPolicy;
////    import androidx.work.PeriodicWorkRequest;
////    import androidx.work.WorkManager;
////    import androidx.work.Worker;
////    import androidx.work.WorkerParameters;
////
////    import com.example.socialmediaadiction.Fragment.Constants;
////    import com.example.socialmediaadiction.Fragment.SessionManager;
////
////    import java.util.Calendar;
////    import java.util.List;
////    import java.util.concurrent.TimeUnit;
////
////    import okhttp3.ResponseBody;
////    import retrofit2.Retrofit;
////    import retrofit2.converter.gson.GsonConverterFactory;
////    import retrofit2.http.Field;
////    import retrofit2.http.FormUrlEncoded;
////    import retrofit2.http.POST;
////
////    public class RewardsActivity extends AppCompatActivity {
////
////        TextView tvUsageValue, tvPoints;
////        SessionManager sessionManager;
////
////        @Override
////        protected void onCreate(Bundle savedInstanceState) {
////            super.onCreate(savedInstanceState);
////            setContentView(R.layout.activity_rewards);
////
////            sessionManager = new SessionManager(this);
////            String USER_ID = sessionManager.getStringData(Constants.KEY_ID);
////
////            tvUsageValue = findViewById(R.id.tvUsageValue);
////            tvPoints = findViewById(R.id.tvPoints);
////
////            // Permission
////            if (!hasUsagePermission(this)) {
////                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
////            }
////
////            long usage = getTodayUsage(this);
////
////            long hours = usage / 60;
////            long minutes = usage % 60;
////
////            tvUsageValue.setText(hours + "h " + minutes + "m");
////
////            int points = (usage > 150) ? 0 : 2;
////
////            tvPoints.setText(points + " Points");
////
////            // ✅ SEND DATA TO API
////            sendPointsNow(USER_ID, points);
////
////            // ✅ OPEN GAMIFICATION AFTER DELAY
////            new Handler().postDelayed(() -> {
////                startActivity(new Intent(RewardsActivity.this, GamificationActivity.class));
////            }, 1500);
////
////            // Schedule Worker (kept as it is)
////            scheduleWorker(USER_ID);
////        }
////
////        // -------- SEND POINTS API --------
////        private void sendPointsNow(String userId, int points) {
////
////            Retrofit retrofit = new Retrofit.Builder()
////                    .baseUrl("https://smartbilling.mycloudspace.in/ecommerce/Android/")
////                    .addConverterFactory(GsonConverterFactory.create())
////                    .build();
////
////            ApiService api = retrofit.create(ApiService.class);
////
////            new Thread(() -> {
////                try {
////                    retrofit2.Response<ResponseBody> response =
////                            api.savePoints(userId, String.valueOf(points)).execute();
////
////                    if (response.isSuccessful()) {
////                        Log.d("API_SUCCESS", "Points sent: " + points);
////                    } else {
////                        Log.e("API_ERROR", "Code: " + response.code());
////                    }
////
////                } catch (Exception e) {
////                    Log.e("API_EXCEPTION", e.getMessage());
////                }
////            }).start();
////        }
////
////        // -------- PERMISSION --------
////        private boolean hasUsagePermission(Context context) {
////            UsageStatsManager usm =
////                    (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
////
////            long time = System.currentTimeMillis();
////
////            List<UsageStats> stats =
////                    usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000, time);
////
////            return stats != null && !stats.isEmpty();
////        }
////
////        // -------- GET USAGE --------
////        public static long getTodayUsage(Context context) {
////
////            UsageStatsManager usm =
////                    (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
////
////            long end = System.currentTimeMillis();
////
////            Calendar calendar = Calendar.getInstance();
////            calendar.set(Calendar.HOUR_OF_DAY, 0);
////            calendar.set(Calendar.MINUTE, 0);
////            calendar.set(Calendar.SECOND, 0);
////
////            long start = calendar.getTimeInMillis();
////
////            List<UsageStats> stats =
////                    usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end);
////
////            long totalTime = 0;
////
////            if (stats != null) {
////                for (UsageStats usageStats : stats) {
////                    totalTime += usageStats.getTotalTimeInForeground();
////                }
////            }
////
////            return totalTime / (1000 * 60);
////        }
////
////        // -------- API --------
////        interface ApiService {
////            @FormUrlEncoded
////            @POST("save_points.php")
////            retrofit2.Call<ResponseBody> savePoints(
////                    @Field("id") String id,
////                    @Field("credit_points") String points
////            );
////        }
////
////        // -------- WORKER --------
////        public static class RewardWorker extends Worker {
////
////            public RewardWorker(@NonNull Context context,
////                                @NonNull WorkerParameters workerParams) {
////                super(context, workerParams);
////            }
////
////            @NonNull
////            @Override
////            public Result doWork() {
////
////                String userId = getInputData().getString("user_id");
////                Context context = getApplicationContext();
////
////                long usage = getTodayUsage(context);
////
////                int points = (usage > 150) ? 0 : 2;
////
////                if (userId != null) {
////
////                    Retrofit retrofit = new Retrofit.Builder()
////                            .baseUrl("https://smartbilling.mycloudspace.in/ecommerce/Android/")
////                            .addConverterFactory(GsonConverterFactory.create())
////                            .build();
////
////                    ApiService api = retrofit.create(ApiService.class);
////
////                    try {
////                        retrofit2.Response<ResponseBody> response =
////                                api.savePoints(userId, String.valueOf(points)).execute();
////
////                        if (response.isSuccessful()) {
////                            Log.d("API_SUCCESS", "Points sent: " + points);
////                        } else {
////                            Log.e("API_ERROR", "Code: " + response.code());
////                        }
////
////                    } catch (Exception e) {
////                        Log.e("API_EXCEPTION", e.getMessage());
////                    }
////                }
////
////                return Result.success();
////            }
////        }
////
////        // -------- SCHEDULE --------
////        private void scheduleWorker(String userId) {
////
////            Calendar calendar = Calendar.getInstance();
////            calendar.set(Calendar.HOUR_OF_DAY, 23);
////            calendar.set(Calendar.MINUTE, 59);
////            calendar.set(Calendar.SECOND, 0);
////
////            long delay = calendar.getTimeInMillis() - System.currentTimeMillis();
////
////            if (delay < 0) {
////                delay += 24 * 60 * 60 * 1000;
////            }
////
////            Data data = new Data.Builder()
////                    .putString("user_id", userId)
////                    .build();
////
////            PeriodicWorkRequest request =
////                    new PeriodicWorkRequest.Builder(RewardWorker.class, 1, TimeUnit.DAYS)
////                            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
////                            .setInputData(data)
////                            .build();
////
////            WorkManager.getInstance(this)
////                    .enqueueUniquePeriodicWork(
////                            "reward_work",
////                            ExistingPeriodicWorkPolicy.UPDATE,
////                            request);
////        }
////    }
