package com.example.socialmediaadiction;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class LockedAppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔥 Show over other apps
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        // 🔒 Disable back button
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // Do nothing
                    }
                });

        // 🔒 Disable outside touch
        setFinishOnTouchOutside(false);

        TextView tv = new TextView(this);
        tv.setText("🚫 App Blocked!\n\nFocus Mode is ON.\nStay disciplined 💪");
        tv.setTextSize(24f);
        tv.setPadding(60, 200, 60, 200);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        setContentView(tv);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 🔥 Keep reopening to prevent escape
        Intent intent = new Intent(this, LockedAppActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}

//package com.example.socialmediaadiction;
//
//import android.content.Intent;
//import android.os.Build;
//import android.os.Bundle;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.TextView;
//
//import androidx.activity.OnBackPressedCallback;
//import androidx.appcompat.app.AppCompatActivity;
//
//public class LockedAppActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // 🔥 Make it appear over other apps
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//            setShowWhenLocked(true);
//            setTurnScreenOn(true);
//        }
//
//        getWindow().addFlags(
//                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
//                        WindowManager.LayoutParams.FLAG_FULLSCREEN |
//                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
//                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//        );
//
//        // 🔒 Disable back button
//        getOnBackPressedDispatcher().addCallback(this,
//                new OnBackPressedCallback(true) {
//                    @Override
//                    public void handleOnBackPressed() {
//                        // Do nothing
//                    }
//                });
//
//        // 🔒 Disable touch outside
//        setFinishOnTouchOutside(false);
//
//        TextView tv = new TextView(this);
//        tv.setText("🚫 App Blocked!\n\nFocus Mode is ON.\nStay disciplined 💪");
//        tv.setTextSize(24f);
//        tv.setPadding(60, 200, 60, 200);
//        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//
//        setContentView(tv);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        // 🔥 IMPORTANT: bring it back if user tries to escape
//        if (FocusModeActivity.BLOCKED_APPS != null) {
//            Intent intent = new Intent(this, LockedAppActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            startActivity(intent);
//        }
//    }
//}
