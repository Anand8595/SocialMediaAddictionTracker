package com.example.socialmediaadiction;

import android.annotation.SuppressLint;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GamificationActivity extends AppCompatActivity {

    private TextView tvTodayUsage, tvPoints, tvBadge, tvMessage;
    private SharedPreferences prefs;
    private ListView listRewards;

    private static final int DAILY_LIMIT_MIN = 180;
    private static final String PREF_NAME = "usage_game_data";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamification);

        tvTodayUsage = findViewById(R.id.tvTodayUsage);
        tvPoints = findViewById(R.id.tvPoints);
        tvBadge = findViewById(R.id.tvBadge);
        tvMessage = findViewById(R.id.tvMessage);
        listRewards = findViewById(R.id.listRewards);

        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        float todayUsage = getTodayScreenTimeMinutes();
        tvTodayUsage.setText("📱 Today’s Screen Time: " + (int) todayUsage + " min");

        updatePoints(todayUsage);
        showGamificationStatus();

        fetchRewards();
    }

    private float getTodayScreenTimeMinutes() {

        UsageStatsManager usageStatsManager =
                (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

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

        return totalForegroundTime / (1000f * 60f); // minutes
    }

    private void updatePoints(float todayUsage) {
        int currentPoints = prefs.getInt("points", 0);
        int currentStreak = prefs.getInt("streak", 0);

        long lastUpdate = prefs.getLong("lastUpdate", 0);
        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.DAY_OF_YEAR);
        cal.setTimeInMillis(lastUpdate);
        int lastDay = cal.get(Calendar.DAY_OF_YEAR);

        if (today != lastDay) {
            if (todayUsage <= DAILY_LIMIT_MIN) {
                currentPoints += 1;
                currentStreak += 1;
                tvMessage.setText("🏆 Great! You stayed under your limit today!");
            } else {
                currentPoints -= 1;
                currentStreak = 0;
                tvMessage.setText("⚠️ You exceeded your daily limit. Try again tomorrow!");
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("points", currentPoints);
            editor.putInt("streak", currentStreak);
            editor.putLong("lastUpdate", System.currentTimeMillis());
            editor.apply();
        }
    }

    private void showGamificationStatus() {
        int points = prefs.getInt("points", 0);
        int streak = prefs.getInt("streak", 0);

        tvPoints.setText("⭐ Points: " + points);
        tvBadge.setText(getBadge(points, streak));
    }

    private String getBadge(int points, int streak) {
        if (points >= 30) return "🏅 Legendary Focus Master!";
        if (points >= 15) return "🌿 Weekend Detox Hero!";
        if (points >= 7) return "🔥 1 Week Streak Champion!";
        if (streak >= 3) return "💪 Focus Apprentice!";
        if (points > 0) return "🎯 Good Start!";
        if (points < 0) return "😣 Try to reduce screen time!";
        return "⚡ Keep Going!";
    }

    // ✅ FINAL FIXED METHOD
    private void fetchRewards() {

        String url = "https://smartbilling.mycloudspace.in/ecommerce/Android/get_rewards.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);

                        if (obj.getString("status").equals("success")) {

                            int totalPoints = obj.getInt("total_points");
                            JSONArray array = obj.getJSONArray("data");

                            List<String> list = new ArrayList<>();

                            for (int i = 0; i < array.length(); i++) {

                                JSONObject item = array.getJSONObject(i);
                                int points = item.getInt("points");

                                // ✅ SAFE DATE FETCH
                                String rawDate = item.optString("reward_date", "");

                                String formattedDate;

                                // ✅ HANDLE NULL / OLD DATA
                                if (rawDate == null || rawDate.equals("") || rawDate.equals("null")) {
                                    formattedDate = "Old Data";
                                } else {
                                    try {
                                        SimpleDateFormat input =
                                                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                                        SimpleDateFormat output =
                                                new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());

                                        java.util.Date d = input.parse(rawDate);
                                        formattedDate = output.format(d);

                                    } catch (Exception e) {
                                        formattedDate = rawDate;
                                    }
                                }

                                String text = formattedDate + " → " +
                                        (points > 0 ? "✅ +" + points : "❌ " + points);

                                list.add(text);
                            }

                            tvPoints.setText("⭐ Total Rewards: " + totalPoints);

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    this,
                                    android.R.layout.simple_list_item_1,
                                    list
                            );

                            listRewards.setAdapter(adapter);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error loading rewards", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", "5"); // make dynamic later
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}

//package com.example.socialmediaadiction;
//
//import android.annotation.SuppressLint;
//import android.app.usage.UsageStats;
//import android.app.usage.UsageStatsManager;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.widget.ArrayAdapter;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.toolbox.StringRequest;
//import com.android.volley.toolbox.Volley;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//
//public class GamificationActivity extends AppCompatActivity {
//
//    private TextView tvTodayUsage, tvPoints, tvBadge, tvMessage;
//    private SharedPreferences prefs;
//    private ListView listRewards;
//
//    private static final int DAILY_LIMIT_MIN = 180;
//    private static final String PREF_NAME = "usage_game_data";
//
//    @SuppressLint("MissingInflatedId")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_gamification);
//
//        tvTodayUsage = findViewById(R.id.tvTodayUsage);
//        tvPoints = findViewById(R.id.tvPoints);
//        tvBadge = findViewById(R.id.tvBadge);
//        tvMessage = findViewById(R.id.tvMessage);
//        listRewards = findViewById(R.id.listRewards);
//
//        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
//
//        float todayUsage = getTodayScreenTimeMinutes();
//        tvTodayUsage.setText("📱 Today’s Screen Time: " + (int) todayUsage + " min");
//
//        updatePoints(todayUsage);
//        showGamificationStatus();
//
//        fetchRewards();
//    }
//
//    private float getTodayScreenTimeMinutes() {
//        UsageStatsManager usageStatsManager =
//                (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
//
//        Calendar calendar = Calendar.getInstance();
//        long endTime = calendar.getTimeInMillis();
//        calendar.add(Calendar.DAY_OF_MONTH, -1);
//        long startTime = calendar.getTimeInMillis();
//
//        List<UsageStats> stats = usageStatsManager.queryUsageStats(
//                UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
//
//        if (stats == null || stats.isEmpty()) {
//            Toast.makeText(this, "No usage data found!", Toast.LENGTH_SHORT).show();
//            return 0;
//        }
//
//        long totalForeground = 0;
//        for (UsageStats us : stats) {
//            totalForeground += us.getTotalTimeInForeground();
//        }
//
//        return totalForeground / 60000f;
//    }
//
//    private void updatePoints(float todayUsage) {
//        int currentPoints = prefs.getInt("points", 0);
//        int currentStreak = prefs.getInt("streak", 0);
//
//        long lastUpdate = prefs.getLong("lastUpdate", 0);
//        Calendar cal = Calendar.getInstance();
//        int today = cal.get(Calendar.DAY_OF_YEAR);
//        cal.setTimeInMillis(lastUpdate);
//        int lastDay = cal.get(Calendar.DAY_OF_YEAR);
//
//        if (today != lastDay) {
//            if (todayUsage <= DAILY_LIMIT_MIN) {
//                currentPoints += 1;
//                currentStreak += 1;
//                tvMessage.setText("🏆 Great! You stayed under your limit today!");
//            } else {
//                currentPoints -= 1;
//                currentStreak = 0;
//                tvMessage.setText("⚠️ You exceeded your daily limit. Try again tomorrow!");
//            }
//
//            SharedPreferences.Editor editor = prefs.edit();
//            editor.putInt("points", currentPoints);
//            editor.putInt("streak", currentStreak);
//            editor.putLong("lastUpdate", System.currentTimeMillis());
//            editor.apply();
//        }
//    }
//
//    private void showGamificationStatus() {
//        int points = prefs.getInt("points", 0);
//        int streak = prefs.getInt("streak", 0);
//
//        tvPoints.setText("⭐ Points: " + points);
//        tvBadge.setText(getBadge(points, streak));
//    }
//
//    private String getBadge(int points, int streak) {
//        if (points >= 30) return "🏅 Legendary Focus Master!";
//        if (points >= 15) return "🌿 Weekend Detox Hero!";
//        if (points >= 7) return "🔥 1 Week Streak Champion!";
//        if (streak >= 3) return "💪 Focus Apprentice!";
//        if (points > 0) return "🎯 Good Start!";
//        if (points < 0) return "😣 Try to reduce screen time!";
//        return "⚡ Keep Going!";
//    }
//
//    // ✅ UPDATED FETCH (ONLY CHANGE AREA)
//    private void fetchRewards() {
//
//        String url = "https://smartbilling.mycloudspace.in/ecommerce/Android/get_rewards.php";
//
//        StringRequest request = new StringRequest(Request.Method.POST, url,
//                response -> {
//                    try {
//                        JSONObject obj = new JSONObject(response);
//
//                        if (obj.getString("status").equals("success")) {
//
//                            int totalPoints = obj.getInt("total_points");
//                            JSONArray array = obj.getJSONArray("data");
//
//                            List<String> list = new ArrayList<>();
//
//                            for (int i = 0; i < array.length(); i++) {
//                                JSONObject item = array.getJSONObject(i);
//
//                                int points = item.getInt("points");
//
//                                // ✅ USE reward_date
//                                String rawDate = item.getString("reward_date");
//
//                                // ✅ FORMAT DATE
//                                String formattedDate = rawDate;
//                                try {
//                                    SimpleDateFormat input =
//                                            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//
//                                    SimpleDateFormat output =
//                                            new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
//
//                                    java.util.Date d = input.parse(rawDate);
//                                    formattedDate = output.format(d);
//
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//
//                                String text = formattedDate + " → " +
//                                        (points > 0 ? "✅ +" + points : "❌ " + points);
//
//                                list.add(text);
//                            }
//
//                            tvPoints.setText("⭐ Total Rewards: " + totalPoints);
//
//                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
//                                    this,
//                                    android.R.layout.simple_list_item_1,
//                                    list
//                            );
//
//                            listRewards.setAdapter(adapter);
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                },
//                error -> Toast.makeText(this, "Error loading rewards", Toast.LENGTH_SHORT).show()
//        ) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<>();
//                params.put("user_id", "5"); // later dynamic
//                return params;
//            }
//        };
//
//        RequestQueue queue = Volley.newRequestQueue(this);
//        queue.add(request);
//    }
//}
////package com.example.socialmediaadiction;
////
////import android.annotation.SuppressLint;
////import android.app.usage.UsageStats;
////import android.app.usage.UsageStatsManager;
////import android.content.Context;
////import android.content.SharedPreferences;
////import android.os.Bundle;
////import android.widget.ArrayAdapter;
////import android.widget.ListView;
////import android.widget.TextView;
////import android.widget.Toast;
////
////import androidx.appcompat.app.AppCompatActivity;
////
////import com.android.volley.Request;
////import com.android.volley.RequestQueue;
////import com.android.volley.toolbox.StringRequest;
////import com.android.volley.toolbox.Volley;
////
////import org.json.JSONArray;
////import org.json.JSONObject;
////
////import java.util.ArrayList;
////import java.util.Calendar;
////import java.util.HashMap;
////import java.util.List;
////import java.util.Map;
////
////public class GamificationActivity extends AppCompatActivity {
////
////    private TextView tvTodayUsage, tvPoints, tvBadge, tvMessage;
////    private SharedPreferences prefs;
////
////    // ✅ NEW
////    private ListView listRewards;
////
////    private static final int DAILY_LIMIT_MIN = 180; // 3 hours
////    private static final String PREF_NAME = "usage_game_data";
////
////    @SuppressLint("MissingInflatedId")
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_gamification);
////
////        tvTodayUsage = findViewById(R.id.tvTodayUsage);
////        tvPoints = findViewById(R.id.tvPoints);
////        tvBadge = findViewById(R.id.tvBadge);
////        tvMessage = findViewById(R.id.tvMessage);
////
////        // ✅ NEW
////        listRewards = findViewById(R.id.listRewards);
////
////        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
////
////        float todayUsage = getTodayScreenTimeMinutes();
////        tvTodayUsage.setText("📱 Today’s Screen Time: " + (int) todayUsage + " min");
////
////        updatePoints(todayUsage);
////        showGamificationStatus();
////
////        // ✅ NEW: Fetch rewards from API
////        fetchRewards();
////    }
////
////    private float getTodayScreenTimeMinutes() {
////        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
////        Calendar calendar = Calendar.getInstance();
////        long endTime = calendar.getTimeInMillis();
////        calendar.add(Calendar.DAY_OF_MONTH, -1);
////        long startTime = calendar.getTimeInMillis();
////
////        List<UsageStats> stats = usageStatsManager.queryUsageStats(
////                UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
////
////        if (stats == null || stats.isEmpty()) {
////            Toast.makeText(this, "No usage data found!", Toast.LENGTH_SHORT).show();
////            return 0;
////        }
////
////        long totalForeground = 0;
////        for (UsageStats us : stats) {
////            totalForeground += us.getTotalTimeInForeground();
////        }
////
////        return totalForeground / 60000f;
////    }
////
////    private void updatePoints(float todayUsage) {
////        int currentPoints = prefs.getInt("points", 0);
////        int currentStreak = prefs.getInt("streak", 0);
////
////        long lastUpdate = prefs.getLong("lastUpdate", 0);
////        Calendar cal = Calendar.getInstance();
////        int today = cal.get(Calendar.DAY_OF_YEAR);
////        cal.setTimeInMillis(lastUpdate);
////        int lastDay = cal.get(Calendar.DAY_OF_YEAR);
////
////        if (today != lastDay) {
////            if (todayUsage <= DAILY_LIMIT_MIN) {
////                currentPoints += 1;
////                currentStreak += 1;
////                tvMessage.setText("🏆 Great! You stayed under your limit today!");
////            } else {
////                currentPoints -= 1;
////                currentStreak = 0;
////                tvMessage.setText("⚠️ You exceeded your daily limit. Try again tomorrow!");
////            }
////
////            SharedPreferences.Editor editor = prefs.edit();
////            editor.putInt("points", currentPoints);
////            editor.putInt("streak", currentStreak);
////            editor.putLong("lastUpdate", System.currentTimeMillis());
////            editor.apply();
////        }
////    }
////
////    private void showGamificationStatus() {
////        int points = prefs.getInt("points", 0);
////        int streak = prefs.getInt("streak", 0);
////
////        tvPoints.setText("⭐ Points: " + points);
////        tvBadge.setText(getBadge(points, streak));
////    }
////
////    private String getBadge(int points, int streak) {
////        if (points >= 30) return "🏅 Legendary Focus Master!";
////        if (points >= 15) return "🌿 Weekend Detox Hero!";
////        if (points >= 7) return "🔥 1 Week Streak Champion!";
////        if (streak >= 3) return "💪 Focus Apprentice!";
////        if (points > 0) return "🎯 Good Start!";
////        if (points < 0) return "😣 Try to reduce screen time!";
////        return "⚡ Keep Going!";
////    }
////
////    // ✅ NEW METHOD (API CALL)
////    private void fetchRewards() {
////
////        String url = "https://smartbilling.mycloudspace.in/ecommerce/Android/get_rewards.php";
////
////        StringRequest request = new StringRequest(Request.Method.POST, url,
////                response -> {
////                    try {
////                        JSONObject obj = new JSONObject(response);
////
////                        if (obj.getString("status").equals("success")) {
////
////                            int totalPoints = obj.getInt("total_points");
////                            JSONArray array = obj.getJSONArray("data");
////
////                            List<String> list = new ArrayList<>();
////
////                            for (int i = 0; i < array.length(); i++) {
////                                JSONObject item = array.getJSONObject(i);
////
////                                int points = item.getInt("points");
////                                String date = item.getString("created_at");
////
////                                String text = date + " → " +
////                                        (points > 0 ? "✅ +" + points : "❌ " + points);
////
////                                list.add(text);
////                            }
////
////                            // 🔥 Update UI
////                            tvPoints.setText("⭐ Total Rewards: " + totalPoints);
////
////                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
////                                    this,
////                                    android.R.layout.simple_list_item_1,
////                                    list
////                            );
////
////                            listRewards.setAdapter(adapter);
////                        }
////
////                    } catch (Exception e) {
////                        e.printStackTrace();
////                    }
////                },
////                error -> Toast.makeText(this, "Error loading rewards", Toast.LENGTH_SHORT).show()
////        ) {
////            @Override
////            protected Map<String, String> getParams() {
////                Map<String, String> params = new HashMap<>();
////                params.put("user_id", "5"); // later dynamic
////                return params;
////            }
////        };
////
////        RequestQueue queue = Volley.newRequestQueue(this);
////        queue.add(request);
////    }
////}
////
//////package com.example.socialmediaadiction;
//////
//////import android.annotation.SuppressLint;
//////import android.app.usage.UsageStats;
//////import android.app.usage.UsageStatsManager;
//////import android.content.Context;
//////import android.content.SharedPreferences;
//////import android.os.Bundle;
//////import android.widget.TextView;
//////import android.widget.Toast;
//////
//////import androidx.appcompat.app.AppCompatActivity;
//////
//////import java.util.Calendar;
//////import java.util.List;
//////
//////public class GamificationActivity extends AppCompatActivity {
//////
//////    private TextView tvTodayUsage, tvPoints, tvBadge, tvMessage;
//////    private SharedPreferences prefs;
//////
//////    private static final int DAILY_LIMIT_MIN = 180; // 3 hours
//////    private static final String PREF_NAME = "usage_game_data";
//////
//////    @SuppressLint("MissingInflatedId")
//////    @Override
//////    protected void onCreate(Bundle savedInstanceState) {
//////        super.onCreate(savedInstanceState);
//////        setContentView(R.layout.activity_gamification);
//////
//////        tvTodayUsage = findViewById(R.id.tvTodayUsage);
//////        tvPoints = findViewById(R.id.tvPoints);
//////        tvBadge = findViewById(R.id.tvBadge);
//////        tvMessage = findViewById(R.id.tvMessage);
//////
//////        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
//////
//////        float todayUsage = getTodayScreenTimeMinutes();
//////        tvTodayUsage.setText("📱 Today’s Screen Time: " + (int) todayUsage + " min");
//////
//////        updatePoints(todayUsage);
//////        showGamificationStatus();
//////    }
//////
//////    private float getTodayScreenTimeMinutes() {
//////        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
//////        Calendar calendar = Calendar.getInstance();
//////        long endTime = calendar.getTimeInMillis();
//////        calendar.add(Calendar.DAY_OF_MONTH, -1);
//////        long startTime = calendar.getTimeInMillis();
//////
//////        List<UsageStats> stats = usageStatsManager.queryUsageStats(
//////                UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
//////
//////        if (stats == null || stats.isEmpty()) {
//////            Toast.makeText(this, "No usage data found!", Toast.LENGTH_SHORT).show();
//////            return 0;
//////        }
//////
//////        long totalForeground = 0;
//////        for (UsageStats us : stats) {
//////            totalForeground += us.getTotalTimeInForeground();
//////        }
//////
//////        return totalForeground / 60000f; // Convert to minutes
//////    }
//////
//////    private void updatePoints(float todayUsage) {
//////        int currentPoints = prefs.getInt("points", 0);
//////        int currentStreak = prefs.getInt("streak", 0);
//////
//////        // Reset streak if new day
//////        long lastUpdate = prefs.getLong("lastUpdate", 0);
//////        Calendar cal = Calendar.getInstance();
//////        int today = cal.get(Calendar.DAY_OF_YEAR);
//////        cal.setTimeInMillis(lastUpdate);
//////        int lastDay = cal.get(Calendar.DAY_OF_YEAR);
//////
//////        if (today != lastDay) {
//////            // Earn or lose point
//////            if (todayUsage <= DAILY_LIMIT_MIN) {
//////                currentPoints += 1;
//////                currentStreak += 1;
//////                tvMessage.setText("🏆 Great! You stayed under your limit today!");
//////            } else {
//////                currentPoints -= 1;
//////                currentStreak = 0;
//////                tvMessage.setText("⚠️ You exceeded your daily limit. Try again tomorrow!");
//////            }
//////
//////            // Save updated data
//////            SharedPreferences.Editor editor = prefs.edit();
//////            editor.putInt("points", currentPoints);
//////            editor.putInt("streak", currentStreak);
//////            editor.putLong("lastUpdate", System.currentTimeMillis());
//////            editor.apply();
//////        }
//////    }
//////
//////    private void showGamificationStatus() {
//////        int points = prefs.getInt("points", 0);
//////        int streak = prefs.getInt("streak", 0);
//////
//////        tvPoints.setText("⭐ Points: " + points);
//////        tvBadge.setText(getBadge(points, streak));
//////    }
//////
//////    private String getBadge(int points, int streak) {
//////        if (points >= 30) return "🏅 Legendary Focus Master!";
//////        if (points >= 15) return "🌿 Weekend Detox Hero!";
//////        if (points >= 7) return "🔥 1 Week Streak Champion!";
//////        if (streak >= 3) return "💪 Focus Apprentice!";
//////        if (points > 0) return "🎯 Good Start!";
//////        if (points < 0) return "😣 Try to reduce screen time!";
//////        return "⚡ Keep Going!";
//////    }
//////}
