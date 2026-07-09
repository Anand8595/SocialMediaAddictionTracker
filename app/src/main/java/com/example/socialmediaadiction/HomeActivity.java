package com.example.socialmediaadiction;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.socialmediaadiction.Fragment.SessionManager;
import com.example.socialmediaadiction.Model.InsightsWorker;
import com.google.android.material.card.MaterialCardView;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import android.widget.ImageView;
public class HomeActivity extends AppCompatActivity {

    ProgressBar progressUsage;
    TextView tvUsageStatus;

    MaterialCardView cardInsights, cardFocus, cardComparison,
            cardMood, cardRewards, cardAlerts, cardLock,cardChatbot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        scheduleDailyInsights(); // ✅ existing
       // runCatchUpSync();

        progressUsage = findViewById(R.id.progress_usage);
        tvUsageStatus = findViewById(R.id.tv_usage_status);
        TextView tvGreeting = findViewById(R.id.tv_greeting);
        tvGreeting.setText(getGreeting());
        View btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {

            SessionManager sessionManager =
                    new SessionManager(HomeActivity.this);

            sessionManager.logoutUser();

            Intent intent =
                    new Intent(HomeActivity.this, MainActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            finish();

        });
        cardInsights = findViewById(R.id.card_insights);
        cardFocus = findViewById(R.id.card_focus);
        cardComparison = findViewById(R.id.card_comparison);
        cardMood = findViewById(R.id.card_mood);
        cardRewards = findViewById(R.id.card_rewards);
        cardAlerts = findViewById(R.id.card_alerts);
        cardLock = findViewById(R.id.card_lock);
        cardChatbot = findViewById(R.id.card_chatbot);

        setupClicks();

        if (!isUsageAccessGranted()) {
            Toast.makeText(this,
                    "Please enable usage access to track today's usage",
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isUsageAccessGranted()) {
            loadTodayUsage();
        }
    }
    private String getGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return "☀️ Good Morning!";
        } else if (hour >= 12 && hour < 17) {
            return "🌤️ Good Afternoon!";
        } else if (hour >= 17 && hour < 21) {
            return "🌆 Good Evening!";
        } else {
            return "🌙 Good Night!";
        }
    }

    // ================= NEW: Catch-up Sync =================
//    private void runCatchUpSync() {
//        Toast.makeText(this, "Syncing missing data...", Toast.LENGTH_SHORT).show();
//
//        // 👉 Just trigger worker immediately once
//        PeriodicWorkRequest oneTime =
//                new PeriodicWorkRequest.Builder(
//                        InsightsWorker.class,
//                        15, TimeUnit.MINUTES
//                ).build();
//
//        WorkManager.getInstance(this).enqueue(oneTime);
//    }

    // ================= FIXED Worker Schedule =================
    private void scheduleDailyInsights() {

        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(
                        InsightsWorker.class,
                        24,
                        TimeUnit.HOURS
                ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DailyInsights",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );
    }

    // ================= CARD CLICKS =================
    private void setupClicks() {

        cardInsights.setOnClickListener(v ->
                startActivity(new Intent(this, InsightsActivity.class)));

        cardFocus.setOnClickListener(v ->
                startActivity(new Intent(this, ReportActivity.class)));

        cardComparison.setOnClickListener(v ->
                startActivity(new Intent(this, ComparisonActivity.class)));

        cardMood.setOnClickListener(v ->
                startActivity(new Intent(this, MoodTrackerActivity.class)));

        cardRewards.setOnClickListener(v ->
                startActivity(new Intent(this, RewardsActivity.class)));

        cardAlerts.setOnClickListener(v ->
                startActivity(new Intent(this, BuyActivity.class)));

        cardLock.setOnClickListener(v ->
                startActivity(new Intent(this, FocusModeActivity.class)));

        cardChatbot.setOnClickListener(v ->
                startActivity(new Intent(this, ChatBotActivity.class)));
    }

    // ================= PERMISSION =================
    private boolean isUsageAccessGranted() {
        try {
            UsageStatsManager usm =
                    (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long now = System.currentTimeMillis();
            List<UsageStats> stats =
                    usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                            now - 1000 * 60 * 60, now);
            return stats != null && !stats.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // ================= TODAY USAGE =================
//    private void loadTodayUsage() {
//
//        UsageStatsManager usageStatsManager =
//                (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//
//        long startTime = calendar.getTimeInMillis();
//        long endTime = System.currentTimeMillis();
//
//        UsageEvents usageEvents =
//                usageStatsManager.queryEvents(startTime, endTime);
//
//        UsageEvents.Event event = new UsageEvents.Event();
//
//        long totalForegroundTime = 0;
//        long lastForegroundTime = 0;
//
//        while (usageEvents.hasNextEvent()) {
//            usageEvents.getNextEvent(event);
//
//            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
//                lastForegroundTime = event.getTimeStamp();
//            }
//
//            if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND
//                    && lastForegroundTime != 0) {
//                totalForegroundTime +=
//                        (event.getTimeStamp() - lastForegroundTime);
//                lastForegroundTime = 0;
//            }
//        }
//
//        long minutesUsed = totalForegroundTime / (1000 * 60);
//
//        int progress = (int) Math.min(100, (minutesUsed * 100) / 360);
//        progressUsage.setProgress(progress);
//
//        if (minutesUsed < 120) {
//            tvUsageStatus.setText("Excellent control today 🌟 (" + minutesUsed + " min)");
//        } else if (minutesUsed < 240) {
//            tvUsageStatus.setText("You're doing well 👍 (" + minutesUsed + " min)");
//        } else {
//            tvUsageStatus.setText("High usage today ⚠️ (" + minutesUsed + " min)");
//        }
//    }
    private void loadTodayUsage() {

        UsageStatsManager usageStatsManager =
                (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long startTime = calendar.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        UsageEvents usageEvents =
                usageStatsManager.queryEvents(startTime, endTime);

        UsageEvents.Event event = new UsageEvents.Event();

        long totalForegroundTime = 0;
        long lastForegroundTime = 0;

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastForegroundTime = event.getTimeStamp();
            }

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND
                    && lastForegroundTime != 0) {
                totalForegroundTime +=
                        (event.getTimeStamp() - lastForegroundTime);
                lastForegroundTime = 0;
            }
        }

        long minutesUsed = totalForegroundTime / (1000 * 60);

        // ✅ Progress Bar Update
        int progress = (int) Math.min(100, (minutesUsed * 100) / 360);
        progressUsage.setProgress(progress);

        // ✅ Show Time in Hero Card (Big Text)
        long hours = minutesUsed / 60;
        long mins = minutesUsed % 60;
        tvUsageStatus.setText(hours + "h " + mins + "m");
    }
}
