package com.example.socialmediaadiction;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import java.util.List;

public class FocusService extends Service {

    private Handler handler = new Handler();
    private Runnable monitorTask;

    private static final String CHANNEL_ID = "focus_service_channel";

    private WindowManager windowManager;
    private TextView overlayView;

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        monitorTask = new Runnable() {
            @Override
            public void run() {

                String topApp = getForegroundApp(getApplicationContext());

                android.util.Log.d("FOCUS", "Detected App: " + topApp);

                if (topApp == null || topApp.isEmpty()) {
                    handler.postDelayed(this, 500);
                    return;
                }

                // 🚫 Ignore your app
                if (topApp.equals(getPackageName())) {
                    removeOverlay();
                    handler.postDelayed(this, 500);
                    return;
                }

                // 🔒 BLOCK
//                if (FocusModeActivity.BLOCKED_APPS.contains(topApp)) {
//                    showOverlay();
//                }
                if (FocusModeActivity.getAllBlockedApps(getApplicationContext()).contains(topApp)) {
                    showOverlay();
                }
                else {
                    removeOverlay();
                }

                handler.postDelayed(this, 500);
            }
        };

        handler.post(monitorTask);
    }

    @Override
    public int onStartCommand(android.content.Intent intent, int flags, int startId) {
        startForegroundServiceProper();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(monitorTask);
        removeOverlay();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(android.content.Intent intent) {
        return null;
    }

    // ✅ FOREGROUND APP DETECTION
    private String getForegroundApp(Context context) {
        UsageStatsManager usm =
                (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        long time = System.currentTimeMillis();

        List<UsageStats> stats =
                usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                        time - 5000,
                        time);

        if (stats == null || stats.isEmpty()) return null;

        UsageStats recentStats = null;

        for (UsageStats usageStats : stats) {
            if (recentStats == null ||
                    usageStats.getLastTimeUsed() > recentStats.getLastTimeUsed()) {
                recentStats = usageStats;
            }
        }

        return recentStats != null ? recentStats.getPackageName() : null;
    }

    // 🔥 SHOW OVERLAY (MAIN BLOCK)
    private void showOverlay() {

        if (overlayView != null) return;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.CENTER;

        overlayView = new TextView(this);
        overlayView.setText("🚫 App Blocked\n\nFocus Mode is ON");
        overlayView.setTextSize(26f);
        overlayView.setGravity(Gravity.CENTER);
        overlayView.setBackgroundColor(0xCC000000);
        overlayView.setTextColor(android.graphics.Color.WHITE);

        windowManager.addView(overlayView, params);
    }

    // 🔥 REMOVE OVERLAY
    private void removeOverlay() {
        if (overlayView != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }
    }

    // ✅ FOREGROUND SERVICE
    private void startForegroundServiceProper() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Focus Mode",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Focus Mode Active")
                .setContentText("Blocking distracting apps...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1, notification);
    }
}

////package com.example.socialmediaadiction;
////
////import android.app.Service;
////import android.app.usage.UsageEvents;
////import android.app.usage.UsageStatsManager;
////import android.content.Context;
////import android.content.Intent;
////import android.os.Handler;
////import android.os.IBinder;
////
////public class FocusService extends Service {
////
////    private Handler handler = new Handler();
////    private Runnable monitorTask;
////
////    @Override
////    public void onCreate() {
////        super.onCreate();
////
////        monitorTask = new Runnable() {
////            @Override
////            public void run() {
////
////                String topApp = getForegroundApp(getApplicationContext());
////
////                // ✅ ADD LOG HERE (IMPORTANT)
////                android.util.Log.d("FOCUS", "Detected App: " + topApp);
////
////                if (FocusModeActivity.BLOCKED_APPS.contains(topApp)) {
////                    Intent intent = new Intent(getApplicationContext(), LockedAppActivity.class);
////                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
////                    startActivity(intent);
////                }
////
////                // ✅ FAST CHECK (IMPORTANT)
////                handler.postDelayed(this, 500);
////            }
////        };
////        handler.post(monitorTask);
////    }
////
////    @Override
////    public int onStartCommand(Intent intent, int flags, int startId) {
////        return START_STICKY;
////    }
////
////    @Override
////    public void onDestroy() {
////        handler.removeCallbacks(monitorTask);
////        super.onDestroy();
////    }
////
////    @Override
////    public IBinder onBind(Intent intent) {
////        return null;
////    }
////
////    private String getForegroundApp(Context context) {
////        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
////        long time = System.currentTimeMillis();
////        UsageEvents usageEvents = usm.queryEvents(time - 2000, time);
////        UsageEvents.Event event = new UsageEvents.Event();
////        String packageName = "";
////        while (usageEvents.hasNextEvent()) {
////            usageEvents.getNextEvent(event);
////            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
////                packageName = event.getPackageName();
////            }
////        }
////        return packageName;
////    }
////}
