package com.example.socialmediaadiction;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediaadiction.Fragment.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class FocusModeActivity extends AppCompatActivity {

    private Button btnStart, btnStop;
    private Button btn15, btn30, btn60, btnSelectApps;
    private EditText etCustomTime;

    private TextView tvTimer, tvStatus, tvDefaultTime;

    private CountDownTimer countDownTimer;
    private boolean isRunning = false;

    private int selectedMinutes = 15;
    BottomNavigationView bottomNavigationView;

    public static final Set<String> DEFAULT_BLOCKED_APPS = new HashSet<>(Arrays.asList(
            "com.instagram.android",
            "com.google.android.youtube",
            "com.facebook.katana",
            "com.whatsapp"
    ));

    public static Set<String> getAllBlockedApps(Context context) {
        Set<String> all = new HashSet<>(DEFAULT_BLOCKED_APPS);

        SharedPreferences prefs = context.getSharedPreferences("FocusPrefs", Context.MODE_PRIVATE);
        String saved = prefs.getString("blocked_apps", "");

        if (!saved.isEmpty()) {
            String[] apps = saved.split(",");
            for (String app : apps) {
                if (!app.isEmpty()) {
                    all.add(app);
                }
            }
        }
        return all;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_mode);

        btnStart = findViewById(R.id.btnStartLock);
        btnStop = findViewById(R.id.btnUnlock);

        btn15 = findViewById(R.id.btn15);
        btn30 = findViewById(R.id.btn30);
        btn60 = findViewById(R.id.btn60);
        btnSelectApps = findViewById(R.id.btnSelectApps);

        etCustomTime = findViewById(R.id.etCustomTime);

        tvTimer = findViewById(R.id.tvTimer);
        tvStatus = findViewById(R.id.tvStatus);
        tvDefaultTime = findViewById(R.id.tvDefaultTime);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {

                startActivity(new Intent(this, HomeActivity.class));

            } else if (id == R.id.nav_reports) {

                startActivity(new Intent(this, ReportActivity.class));

            } else if (id == R.id.nav_insights) {

                startActivity(new Intent(this, InsightsActivity.class));

            } else if (id == R.id.nav_rewards) {

                startActivity(new Intent(this, RewardsActivity.class));

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
        tvDefaultTime.setText("Default focus time: 15 min");

        // ✅ DEFAULT SELECTED
        updateButtonUI(15);

        btn15.setOnClickListener(v -> {
            selectedMinutes = 15;
            etCustomTime.setText("");
            tvDefaultTime.setText("Focus time: 15 min");
            updateButtonUI(15);
        });

        btn30.setOnClickListener(v -> {
            selectedMinutes = 30;
            etCustomTime.setText("");
            tvDefaultTime.setText("Focus time: 30 min");
            updateButtonUI(30);
        });

        btn60.setOnClickListener(v -> {
            selectedMinutes = 60;
            etCustomTime.setText("");
            tvDefaultTime.setText("Focus time: 60 min");
            updateButtonUI(60);
        });

        btnSelectApps.setOnClickListener(v ->
                startActivity(new Intent(this, AppSelectionActivity.class)));

        btnStart.setOnClickListener(v -> {

            String input = etCustomTime.getText().toString().trim();

            if (!TextUtils.isEmpty(input)) {
                try {
                    int customTime = Integer.parseInt(input);

                    if (customTime <= 0) {
                        Toast.makeText(this, "Enter valid time", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    selectedMinutes = customTime;
                    tvDefaultTime.setText("Focus time: " + selectedMinutes + " min");

                } catch (Exception e) {
                    Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (!hasUsageAccess(this)) {
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                return;
            }

            if (!Settings.canDrawOverlays(this)) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                return;
            }

            startFocusLock(selectedMinutes);
        });

        btnStop.setOnClickListener(v -> stopFocusLock());

        restoreTimer();
    }

    // ✅ FIXED UI METHOD (NO TINT BREAK)
    private void updateButtonUI(int selected) {

        btn15.setSelected(false);
        btn30.setSelected(false);
        btn60.setSelected(false);

        if (selected == 15) {
            btn15.setSelected(true);
        } else if (selected == 30) {
            btn30.setSelected(true);
        } else if (selected == 60) {
            btn60.setSelected(true);
        }
    }

    private void startFocusLock(int minutes) {

        if (isRunning) return;

        isRunning = true;

        tvStatus.setText("🔒 Focus Mode Active! (" + minutes + " min)");

        btnStart.setEnabled(false);
        btnStop.setEnabled(true);

        // ❌ REMOVED disable (keeps UI clean)
        btn15.setAlpha(0.5f);
        btn30.setAlpha(0.5f);
        btn60.setAlpha(0.5f);

        etCustomTime.setEnabled(false);
        btnSelectApps.setEnabled(false);

        long endTime = System.currentTimeMillis() + (minutes * 60 * 1000);

        SharedPreferences prefs = getSharedPreferences("FocusPrefs", MODE_PRIVATE);
        prefs.edit()
                .putLong("endTime", endTime)
                .putBoolean("isRunning", true)
                .apply();

        Intent intent = new Intent(this, FocusService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        resumeTimer(minutes * 60L * 1000L);
    }

    private void resumeTimer(long millis) {

        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(millis, 1000) {
            public void onTick(long millisUntilFinished) {
                tvTimer.setText(formatTime(millisUntilFinished));
            }

            public void onFinish() {
                stopFocusLock();
            }
        }.start();
    }

    private void restoreTimer() {

        SharedPreferences prefs = getSharedPreferences("FocusPrefs", MODE_PRIVATE);

        boolean wasRunning = prefs.getBoolean("isRunning", false);
        long endTime = prefs.getLong("endTime", 0);

        if (wasRunning) {

            long remaining = endTime - System.currentTimeMillis();

            if (remaining > 0) {
                isRunning = true;
                tvStatus.setText("🔒 Focus Mode Active!");

                btnStart.setEnabled(false);
                btnStop.setEnabled(true);

                resumeTimer(remaining);
            } else {
                stopFocusLock();
            }
        }
    }

    private void stopFocusLock() {

        if (!isRunning) return;

        isRunning = false;

        if (countDownTimer != null) countDownTimer.cancel();

        stopService(new Intent(this, FocusService.class));

        tvStatus.setText("✅ Focus Mode Ended");
        tvTimer.setText("");

        btnStart.setEnabled(true);
        btnStop.setEnabled(true);

        // ✅ restore alpha
        btn15.setAlpha(1f);
        btn30.setAlpha(1f);
        btn60.setAlpha(1f);

        etCustomTime.setEnabled(true);
        btnSelectApps.setEnabled(true);

        SharedPreferences prefs = getSharedPreferences("FocusPrefs", MODE_PRIVATE);

        prefs.edit()
                .remove("endTime")
                .remove("isRunning")
                .apply();
    }

    private boolean hasUsageAccess(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.getPackageName()
        );
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private String formatTime(long millis) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }
}
