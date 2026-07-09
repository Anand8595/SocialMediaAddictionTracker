package com.example.socialmediaadiction.Model;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.socialmediaadiction.Api.ApiService;
import com.example.socialmediaadiction.Api.RetrofitClient;
import com.example.socialmediaadiction.Fragment.Constants;
import com.example.socialmediaadiction.Fragment.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InsightsWorker extends Worker {

    private static final String TAG = "InsightsWorker";
    private SessionManager sessionManager;

    public InsightsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        sessionManager = new SessionManager(context.getApplicationContext());
    }

    @NonNull
    @Override
    public Result doWork() {

        try {

            UsageStatsManager usm =
                    (UsageStatsManager) getApplicationContext()
                            .getSystemService(Context.USAGE_STATS_SERVICE);

            // ✅ GET LAST SYNC TIME (prevents duplicate)
//            long lastSync = sessionManager.getLongData("last_sync_time", 0);
//
//            long endTime = System.currentTimeMillis();
//            long startTime;
//
//            if (lastSync == 0) {
//                startTime = endTime - (15 * 60 * 1000); // first run
//            } else {
//                startTime = lastSync;
//            }

            Calendar cal = Calendar.getInstance();

            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            long startTime = cal.getTimeInMillis();
            long endTime = System.currentTimeMillis();

            UsageEvents events = usm.queryEvents(startTime, endTime);
            UsageEvents.Event event = new UsageEvents.Event();

            String currentApp = null;
            long lastTime = 0;

            long youtubeSec = 0, instagramSec = 0, facebookSec = 0, whatsappSec = 0, chromeSec = 0;

            while (events.hasNextEvent()) {
                events.getNextEvent(event);

                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {

                    if (currentApp != null) {
                        long duration = (event.getTimeStamp() - lastTime) / 1000; // ✅ seconds

                        if (duration > 0) {
                            switch (currentApp) {
                                case "com.google.android.youtube": youtubeSec += duration; break;
                                case "com.instagram.android": instagramSec += duration; break;
                                case "com.facebook.katana":
                                case "com.facebook.lite": facebookSec += duration; break;
                                case "com.whatsapp": whatsappSec += duration; break;
                                case "com.android.chrome": chromeSec += duration; break;
                            }
                        }
                    }

                    currentApp = event.getPackageName();
                    lastTime = event.getTimeStamp();
                }
            }

            // ✅ handle last app still open
            if (currentApp != null) {
                long duration = (endTime - lastTime) / 1000;

                if (duration > 0) {
                    switch (currentApp) {
                        case "com.google.android.youtube": youtubeSec += duration; break;
                        case "com.instagram.android": instagramSec += duration; break;
                        case "com.facebook.katana":
                        case "com.facebook.lite": facebookSec += duration; break;
                        case "com.whatsapp": whatsappSec += duration; break;
                        case "com.android.chrome": chromeSec += duration; break;
                    }
                }
            }

            // ✅ convert to minutes
            long youtube = youtubeSec / 60;
            long instagram = instagramSec / 60;
            long facebook = facebookSec / 60;
            long whatsapp = whatsappSec / 60;
            long chrome = chromeSec / 60;

            Log.d(TAG, "Usage -> IG=" + instagram + " YT=" + youtube);

            // ❌ skip if no meaningful usage
            if (youtube < 1 && instagram < 1 && facebook < 1 &&
                    whatsapp < 1 && chrome < 1) {

                sessionManager.putLongData("last_sync_time", endTime);
                return Result.success();
            }

            // ✅ SAVE SYNC TIME
            sessionManager.putLongData("last_sync_time", endTime);

            // ✅ SEND DATA
            String userId = sessionManager.getStringData(Constants.KEY_ID);
            if (userId == null || userId.isEmpty()) userId = "1";

            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date());

            sendUsageToServer(
                    userId,
                    date,
                    (int) youtube,
                    (int) instagram,
                    (int) facebook,
                    (int) whatsapp,
                    (int) chrome
            );

        } catch (Exception e) {
            Log.e(TAG, "Error: ", e);
            return Result.failure();
        }

        return Result.success();
    }

    // ================= API =================
    private void sendUsageToServer(String userId, String date,
                                   int yt, int ig, int fb, int wa, int ch) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RetrofitClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);

        Call<SimpleResponse> call = api.saveDailyUsage(
                userId, date, yt, ig, fb, wa, ch
        );

        call.enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call,
                                   Response<SimpleResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Inserted: " + response.body().getMessage());
                } else {
                    Log.e(TAG, "Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                Log.e(TAG, "Network error: ", t);
            }
        });
    }
}

//package com.example.socialmediaadiction.Model;
//
//import android.app.usage.UsageEvents;
//import android.app.usage.UsageStats;
//import android.app.usage.UsageStatsManager;
//import android.content.Context;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//import androidx.work.Worker;
//import androidx.work.WorkerParameters;
//
//import com.example.socialmediaadiction.Api.ApiService;
//import com.example.socialmediaadiction.Api.RetrofitClient;
//import com.example.socialmediaadiction.Fragment.Constants;
//import com.example.socialmediaadiction.Fragment.SessionManager;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//
//public class InsightsWorker extends Worker {
//
//    private static final String TAG = "InsightsWorker";
//    private SessionManager sessionManager;
//
//    public InsightsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
//        super(context, params);
//        sessionManager = new SessionManager(context.getApplicationContext());
//    }
//
//    @NonNull
//    @Override
//    public Result doWork() {
//
//        try {
//
//            UsageStatsManager usm =
//                    (UsageStatsManager) getApplicationContext()
//                            .getSystemService(Context.USAGE_STATS_SERVICE);
//
//            long endTime = System.currentTimeMillis();
//            long startTime = endTime - (15 * 60 * 1000); // last 15 min
//
//            UsageEvents events = usm.queryEvents(startTime, endTime);
//            UsageEvents.Event event = new UsageEvents.Event();
//
//            String currentApp = null;
//            long lastTime = 0;
//
//            long youtube = 0, instagram = 0, facebook = 0, whatsapp = 0, chrome = 0;
//
//            while (events.hasNextEvent()) {
//                events.getNextEvent(event);
//
//                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
//
//                    if (currentApp != null) {
//                        long duration = (event.getTimeStamp() - lastTime) / 60000;
//
//                        if (duration > 0) {
//                            switch (currentApp) {
//                                case "com.google.android.youtube": youtube += duration; break;
//                                case "com.instagram.android": instagram += duration; break;
//                                case "com.facebook.katana":
//                                case "com.facebook.lite": facebook += duration; break;
//                                case "com.whatsapp": whatsapp += duration; break;
//                                case "com.android.chrome": chrome += duration; break;
//                            }
//                        }
//                    }
//
//                    currentApp = event.getPackageName();
//                    lastTime = event.getTimeStamp();
//                }
//            }
//
//            // handle last open app
//            if (currentApp != null) {
//                long duration = (endTime - lastTime) / 60000;
//
//                if (duration > 0) {
//                    switch (currentApp) {
//                        case "com.google.android.youtube": youtube += duration; break;
//                        case "com.instagram.android": instagram += duration; break;
//                        case "com.facebook.katana":
//                        case "com.facebook.lite": facebook += duration; break;
//                        case "com.whatsapp": whatsapp += duration; break;
//                        case "com.android.chrome": chrome += duration; break;
//                    }
//                }
//            }
//
//            // ❌ if no usage → don't send
//            if (youtube == 0 && instagram == 0 && facebook == 0 &&
//                    whatsapp == 0 && chrome == 0) {
//                return Result.success();
//            }
//
//            String userId = sessionManager.getStringData(Constants.KEY_ID);
//            if (userId == null || userId.isEmpty()) userId = "1";
//
//            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//                    .format(new Date());
//
//            sendUsageToServer(
//                    userId,
//                    date,
//                    (int) youtube,
//                    (int) instagram,
//                    (int) facebook,
//                    (int) whatsapp,
//                    (int) chrome
//            );
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return Result.failure();
//        }
//
//        return Result.success();
//    }
//
//    // ================= API CALL =================
//    private void sendUsageToServer(String userId, String date,
//                                   int yt, int ig, int fb, int wa, int ch) {
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(RetrofitClient.BASE_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        ApiService api = retrofit.create(ApiService.class);
//
//        Call<SimpleResponse> call = api.saveDailyUsage(
//                userId, date, yt, ig, fb, wa, ch
//        );
//
//        call.enqueue(new Callback<SimpleResponse>() {
//            @Override
//            public void onResponse(Call<SimpleResponse> call,
//                                   Response<SimpleResponse> response) {
//
//                if (response.isSuccessful() && response.body() != null) {
//                    Log.d(TAG, "Inserted: " + response.body().getMessage());
//                } else {
//                    Log.e(TAG, "Server error: " + response.code());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<SimpleResponse> call, Throwable t) {
//                Log.e(TAG, "Network error: ", t);
//            }
//        });
//    }
//}
//
////package com.example.socialmediaadiction.Model;
////
////import android.app.usage.UsageStats;
////import android.app.usage.UsageStatsManager;
////import android.content.Context;
////import android.util.Log;
////
////import androidx.annotation.NonNull;
////import androidx.work.Worker;
////import androidx.work.WorkerParameters;
////
////import com.example.socialmediaadiction.Api.ApiService;
////import com.example.socialmediaadiction.Api.RetrofitClient;
////import com.example.socialmediaadiction.Fragment.Constants;
////import com.example.socialmediaadiction.Fragment.SessionManager;
////import com.example.socialmediaadiction.Model.SimpleResponse;
////
////import java.text.SimpleDateFormat;
////import java.util.Calendar;
////import java.util.Date;
////import java.util.List;
////import java.util.Locale;
////
////import retrofit2.Call;
////import retrofit2.Callback;
////import retrofit2.Response;
////import retrofit2.Retrofit;
////import retrofit2.converter.gson.GsonConverterFactory;
////
////public class InsightsWorker extends Worker {
////
////    private static final String TAG = "InsightsWorker";
////    private SessionManager sessionManager;
////
////    public InsightsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
////        super(context, params);
////        // Initialize SessionManager safely with application context
////        sessionManager = new SessionManager(context.getApplicationContext());
////    }
////
////    @NonNull
////    @Override
////    public Result doWork() {
////        try {
////            // ====== GET USAGE STATS ======
////            UsageStatsManager usm = (UsageStatsManager) getApplicationContext()
////                    .getSystemService(Context.USAGE_STATS_SERVICE);
////
////            Calendar cal = Calendar.getInstance();
////            cal.set(Calendar.HOUR_OF_DAY, 0);
////            cal.set(Calendar.MINUTE, 0);
////            cal.set(Calendar.SECOND, 0);
////
////            List<UsageStats> stats = usm.queryUsageStats(
////                    UsageStatsManager.INTERVAL_DAILY,
////                    cal.getTimeInMillis(),
////                    System.currentTimeMillis()
////            );
////
////            long youtube = 0, instagram = 0, facebook = 0, whatsapp = 0, chrome = 0;
////
////            for (UsageStats u : stats) {
////                long minutes = u.getTotalTimeInForeground() / 60000;
////                String pkg = u.getPackageName();
////
////                if (pkg.contains("youtube")) youtube += minutes;
////                else if (pkg.contains("instagram")) instagram += minutes;
////                else if (pkg.contains("facebook")) facebook += minutes;
////                else if (pkg.contains("whatsapp")) whatsapp += minutes;
////                else if (pkg.contains("chrome")) chrome += minutes;
////            }
////
////            Log.d(TAG, "YT=" + youtube + " IG=" + instagram + " FB=" + facebook +
////                    " WA=" + whatsapp + " CH=" + chrome);
////
////            // ====== GET USER ID FROM SESSION ======
////            String userId = sessionManager.getStringData(Constants.KEY_ID);
////            if (userId == null || userId.isEmpty()) {
////                userId = "1"; // fallback default user ID
////            }
////
////            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
////
////            // ====== SEND DATA TO SERVER ======
////            sendUsageToServer(userId, date, youtube, instagram, facebook, whatsapp, chrome);
////
////        } catch (Exception e) {
////            Log.e(TAG, "Error in InsightsWorker: ", e);
////            return Result.failure();
////        }
////
////        return Result.success();
////    }
////
////    private void sendUsageToServer(String userId, String date,
////                                   long yt, long ig, long fb, long wa, long ch) {
////
////        Retrofit retrofit = new Retrofit.Builder()
////                .baseUrl(RetrofitClient.BASE_URL) // Make sure this ends with "/"
////                .addConverterFactory(GsonConverterFactory.create())
////                .build();
////
////        ApiService api = retrofit.create(ApiService.class);
////
////        Call<SimpleResponse> call = api.saveDailyUsage(
////                userId,
////                date,
////                (int) yt,
////                (int) ig,
////                (int) fb,
////                (int) wa,
////                (int) ch
////        );
////
////        call.enqueue(new Callback<SimpleResponse>() {
////            @Override
////            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
////                if (response.isSuccessful() && response.body() != null) {
////                    Log.d(TAG, "Inserted: " + response.body().getMessage());
////                } else {
////                    Log.e(TAG, "Server error: " + response.code() + " " + response.message());
////                }
////            }
////
////            @Override
////            public void onFailure(Call<SimpleResponse> call, Throwable t) {
////                Log.e(TAG, "Network error: ", t);
////            }
////        });
////    }
////}
