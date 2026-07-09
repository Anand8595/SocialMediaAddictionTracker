package com.example.socialmediaadiction;

import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import java.util.List;

import com.example.socialmediaadiction.Fragment.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
public class InsightsActivity extends AppCompatActivity {

    ProgressBar youtubeBar, instaBar, facebookBar, whatsappBar, chromeBar;
    TextView tvYoutube, tvInsta, tvFacebook, tvWhatsapp, tvChrome, tvSuggestion, tvTotal;

    ImageView iconYoutube, iconInsta, iconFacebook, iconWhatsapp, iconChrome;

    long youtube = 0, insta = 0, facebook = 0, whatsapp = 0, chrome = 0;
    long totalSeconds = 0;
    BottomNavigationView bottomNavigation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insights);

        youtubeBar = findViewById(R.id.progress_youtube);
        instaBar = findViewById(R.id.progress_insta);
        facebookBar = findViewById(R.id.progress_facebook);
        whatsappBar = findViewById(R.id.progress_whatsapp);
        chromeBar = findViewById(R.id.progress_chrome);

        tvYoutube = findViewById(R.id.tv_youtube);
        tvInsta = findViewById(R.id.tv_insta);
        tvFacebook = findViewById(R.id.tv_facebook);
        tvWhatsapp = findViewById(R.id.tv_whatsapp);
        tvChrome = findViewById(R.id.tv_chrome);
        tvSuggestion = findViewById(R.id.tv_suggestion);
        tvTotal = findViewById(R.id.tv_total);

        iconYoutube = findViewById(R.id.icon_youtube);
        iconInsta = findViewById(R.id.icon_insta);
        iconFacebook = findViewById(R.id.icon_facebook);
        iconWhatsapp = findViewById(R.id.icon_whatsapp);
        iconChrome = findViewById(R.id.icon_chrome);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        setAppIcon(iconYoutube, "com.google.android.youtube");
        setAppIcon(iconInsta, "com.instagram.android");
        setAppIcon(iconFacebook, "com.facebook.katana");
        setAppIcon(iconWhatsapp, "com.whatsapp");
        setAppIcon(iconChrome, "com.android.chrome");

        if (!hasUsagePermission()) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            Toast.makeText(this, "Enable Usage Access", Toast.LENGTH_LONG).show();
            return;
        }

        loadUsageHybrid();

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {

                startActivity(new Intent(this, HomeActivity.class));

            } else if (id == R.id.nav_reports) {

                startActivity(new Intent(this, ReportActivity.class));

//            } else if (id == R.id.nav_compare) {
//
//                startActivity(new Intent(this, ComparisonActivity.class));

            } else if (id == R.id.nav_rewards) {

                startActivity(new Intent(this, RewardsActivity.class));

            } else if (id == R.id.nav_focus) {

                startActivity(new Intent(this, FocusModeActivity.class));

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
    }

    private boolean hasUsagePermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                getPackageName()
        );
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    // 🔥 HYBRID (for app breakdown only)
    private void loadUsageHybrid() {

        youtube = insta = facebook = whatsapp = chrome = 0;
        totalSeconds = 0;

        loadUsingEvents();

        if (totalSeconds < 300) {
            youtube = insta = facebook = whatsapp = chrome = 0;
            totalSeconds = 0;
            loadUsingStatsFallback();

            Toast.makeText(this,
                    "Using approximate data for this device",
                    Toast.LENGTH_SHORT).show();
        }

        updateUI();
    }

    private void loadUsingEvents() {

        UsageStatsManager usm =
                (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        long startTime = cal.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        UsageEvents events = usm.queryEvents(startTime, endTime);
        UsageEvents.Event event = new UsageEvents.Event();

        String currentApp = null;
        long lastTime = 0;

        while (events.hasNextEvent()) {
            events.getNextEvent(event);

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {

                if (currentApp != null) {
                    long duration = (event.getTimeStamp() - lastTime) / 1000;

                    if (duration > 0) {
                        addUsage(currentApp, duration);
                    }
                }

                currentApp = event.getPackageName();
                lastTime = event.getTimeStamp();
            }
        }

        if (currentApp != null) {
            long duration = (endTime - lastTime) / 1000;

            if (duration > 0) {
                addUsage(currentApp, duration);
            }
        }
    }

//    private void loadUsingStatsFallback() {
//        UsageStatsManager usm =
//                (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
//
//        long end = System.currentTimeMillis();
//        long start = end - (24 * 60 * 60 * 1000);
//
//        List<UsageStats> stats =
//                usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end);
//
//        if (stats == null) return;
//
//        for (UsageStats u : stats) {
//
//            // ✅ FIX: convert to MINUTES (NOT seconds)
//            long minutes = u.getTotalTimeInForeground() / 60000;
//
//            String pkg = u.getPackageName();
//
//            totalSeconds += minutes; // (you can rename later to totalMinutes)
//
//            switch (pkg) {
//                case "com.google.android.youtube":
//                    youtube += minutes;
//                    break;
//
//                case "com.instagram.android":
//                    insta += minutes;
//                    break;
//
//                case "com.facebook.katana":
//                case "com.facebook.lite":
//                    facebook += minutes;
//                    break;
//
//                case "com.whatsapp":
//                    whatsapp += minutes;
//                    break;
//
//                case "com.android.chrome":
//                    chrome += minutes;
//                    break;
//            }
//        }
//    }
private void loadUsingStatsFallback() {

    UsageStatsManager usm =
            (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

    long end = System.currentTimeMillis();
    long start = end - (24 * 60 * 60 * 1000);

    List<UsageStats> stats =
            usm.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    start,
                    end
            );

    if (stats == null) return;

    for (UsageStats u : stats) {

        long sec = u.getTotalTimeInForeground() / 1000;

        String pkg = u.getPackageName();

        totalSeconds += sec;

        switch (pkg) {

            case "com.google.android.youtube":
                youtube += sec;
                break;

            case "com.instagram.android":
                insta += sec;
                break;

            case "com.facebook.katana":
            case "com.facebook.lite":
                facebook += sec;
                break;

            case "com.whatsapp":
                whatsapp += sec;
                break;

            case "com.android.chrome":
                chrome += sec;
                break;
        }
    }
}

    // ✅ FIXED (NO FILTER ON TOTAL)
//    private void addUsage(String app, long durationSec) {
//
//        // ❌ skip system noise
//        if (app.startsWith("com.android")) return;
//        if (app.contains("launcher")) return;
//        if (app.contains("systemui")) return;
//
//        // ❌ skip invalid sessions
//        if (durationSec < 2 || durationSec > 3600) return;
//
//        totalSeconds += durationSec;
//
//        switch (app) {
//            case "com.google.android.youtube": youtube += durationSec; break;
//            case "com.instagram.android": insta += durationSec; break;
//            case "com.facebook.katana":
//            case "com.facebook.lite": facebook += durationSec; break;
//            case "com.whatsapp": whatsapp += durationSec; break;
//            case "com.android.chrome": chrome += durationSec; break;
//        }
//    }
    private void addUsage(String app, long durationSec) {

        // skip only launcher/system apps
        if (app.contains("launcher")) return;
        if (app.contains("systemui")) return;

        // skip invalid sessions
        if (durationSec < 2 || durationSec > 3600) return;

        totalSeconds += durationSec;

        switch (app) {

            case "com.google.android.youtube":
                youtube += durationSec;
                break;

            case "com.instagram.android":
                insta += durationSec;
                break;

            case "com.facebook.katana":
            case "com.facebook.lite":
                facebook += durationSec;
                break;

            case "com.whatsapp":
                whatsapp += durationSec;
                break;

            case "com.android.chrome":
                chrome += durationSec;
                break;
        }
    }

    private void updateUI() {

        long ytMin = youtube / 60;
        long igMin = insta / 60;
        long fbMin = facebook / 60;
        long waMin = whatsapp / 60;
        long chMin = chrome / 60;

        // ✅ CORRECT TOTAL (same as Home)
        long totalMin = getTotalScreenTimeToday();

        long max = Math.max(Math.max(Math.max(ytMin, igMin), fbMin),
                Math.max(waMin, chMin));
        if (max == 0) max = 1;

        youtubeBar.setMax((int) max);
        instaBar.setMax((int) max);
        facebookBar.setMax((int) max);
        whatsappBar.setMax((int) max);
        chromeBar.setMax((int) max);

        youtubeBar.setProgress((int) ytMin);
        instaBar.setProgress((int) igMin);
        facebookBar.setProgress((int) fbMin);
        whatsappBar.setProgress((int) waMin);
        chromeBar.setProgress((int) chMin);

        tvYoutube.setText("YouTube • " + formatTime(ytMin));
        tvInsta.setText("Instagram • " + formatTime(igMin));
        tvFacebook.setText("Facebook • " + formatTime(fbMin));
        tvWhatsapp.setText("WhatsApp • " + formatTime(waMin));
        tvChrome.setText("Chrome • " + formatTime(chMin));

        tvTotal.setText(formatTime(totalMin));

        tvSuggestion.setText(generateAISummary(totalMin,
                Math.max(Math.max(Math.max(ytMin, igMin), fbMin),
                        Math.max(waMin, chMin))));
    }

    private long getTotalScreenTimeToday() {

        UsageStatsManager usageStatsManager =
                (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long startTime = calendar.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        UsageEvents usageEvents =
                usageStatsManager.queryEvents(startTime, endTime);

        UsageEvents.Event event = new UsageEvents.Event();

        long totalTime = 0;
        long lastTime = 0;

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastTime = event.getTimeStamp();
            }

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND
                    && lastTime != 0) {

                totalTime += (event.getTimeStamp() - lastTime);
                lastTime = 0;
            }
        }

        return totalTime / (1000 * 60);
    }

    private String generateAISummary(long total, long maxApp) {

        int score = 0;

        if (total > 120) score += 20;
        if (total > 180) score += 30;
        if (total > 300) score += 20;

        double dominance = (double) maxApp / Math.max(total, 1);
        if (dominance > 0.6) score += 30;

        if (score < 20) return "✅ Excellent control!";
        else if (score < 50) return "👍 Good usage. Stay balanced.";
        else if (score < 80) return "⚠️ High usage. Try reducing screen time.";
        else return "🧠 Addiction risk! Consider taking a break.";
    }

    private String formatTime(long minutes) {
        long h = minutes / 60;
        long m = minutes % 60;
        return h > 0 ? h + "h " + m + "m" : m + "m";
    }

    private void setAppIcon(ImageView imageView, String packageName) {
        try {
            Drawable icon = getPackageManager().getApplicationIcon(packageName);
            imageView.setImageDrawable(icon);
        } catch (Exception e) {
            imageView.setImageResource(android.R.drawable.sym_def_app_icon);
        }
    }
}


