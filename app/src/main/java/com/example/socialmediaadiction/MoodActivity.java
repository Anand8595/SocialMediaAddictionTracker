package com.example.socialmediaadiction;

import android.annotation.SuppressLint;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MoodActivity extends AppCompatActivity {

    SeekBar seekMood;
    TextView tvMoodScore, tvMoodText, tvInsight;
    Button btnSaveMood;

    int moodScore = 3; // default

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood);

        seekMood = findViewById(R.id.seekMood);
        tvMoodScore = findViewById(R.id.tvMoodScore);
        tvMoodText = findViewById(R.id.tvMoodText);
        btnSaveMood = findViewById(R.id.btnSaveMood);
        tvInsight = findViewById(R.id.tvInsight);

        // Default mood = 3
        seekMood.setProgress(2);
        tvMoodText.setText(getMoodText(3));

        seekMood.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                moodScore = progress + 1;
                tvMoodScore.setText("Mood Score: " + moodScore);
                tvMoodText.setText(getMoodText(moodScore));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnSaveMood.setOnClickListener(v -> {

            long screenTimeMinutes = getTodayScreenTime();

            String insight = getCorrelationInsight(moodScore, screenTimeMinutes);

            tvInsight.setText(insight);

            Toast.makeText(
                    MoodActivity.this,
                    "Mood saved successfully",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    // ------------------ MOOD TEXT ------------------
    private String getMoodText(int mood) {
        switch (mood) {
            case 1: return "Very Bad";
            case 2: return "Bad";
            case 3: return "Neutral";
            case 4: return "Good";
            case 5: return "Very Good";
            default: return "";
        }
    }

    // ------------------ SCREEN TIME ------------------
    private long getTodayScreenTime() {

        UsageStatsManager usm =
                (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        long endTime = System.currentTimeMillis();
        long startTime = endTime - (24 * 60 * 60 * 1000);

        List<UsageStats> stats =
                usm.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        startTime,
                        endTime
                );

        long totalTime = 0;
        if (stats != null) {
            for (UsageStats s : stats) {
                totalTime += s.getTotalTimeInForeground();
            }
        }

        return totalTime / (1000 * 60); // minutes
    }

    // ------------------ CORRELATION LOGIC ------------------
    private String getCorrelationInsight(int mood, long screenTimeMinutes) {

        long hours = screenTimeMinutes / 60;

        if (hours >= 4 && mood <= 2) {
            return "High screen time (" + hours +
                    " hrs) is associated with lower mood.";
        }

        if (hours < 2 && mood >= 4) {
            return "Lower screen time (" + hours +
                    " hrs) is linked to better mood.";
        }

        if (hours >= 4 && mood >= 4) {
            return "Despite high screen time, your mood is stable.";
        }

        return "Your mood and screen time show no strong correlation today.";
    }
}
