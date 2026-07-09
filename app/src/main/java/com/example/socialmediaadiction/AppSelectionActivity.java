package com.example.socialmediaadiction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediaadiction.Adapter.AppAdapter;
import com.example.socialmediaadiction.Model.AppModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppSelectionActivity extends AppCompatActivity {

    private ListView listView;
    private Button btnSave;

    private PackageManager pm;

    private List<AppModel> appList = new ArrayList<>();

    // 🔥 SINGLE SOURCE OF TRUTH
    public static Set<String> selectedAppsStatic = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

        listView = findViewById(R.id.listViewApps);
        btnSave = findViewById(R.id.btnSaveApps);

        pm = getPackageManager();

        loadApps();

        // 🔥 LOAD SAVED DATA
        SharedPreferences prefs = getSharedPreferences("FocusPrefs", MODE_PRIVATE);
        String saved = prefs.getString("blocked_apps", "");

        selectedAppsStatic.clear();

        if (!saved.isEmpty()) {
            String[] apps = saved.split(",");
            for (String app : apps) {
                if (!app.isEmpty()) {
                    selectedAppsStatic.add(app);
                }
            }
        }

        // 🔥 SET ADAPTER
        AppAdapter adapter = new AppAdapter(this, appList);
        listView.setAdapter(adapter);

        // 💾 SAVE BUTTON
        btnSave.setOnClickListener(v -> {

            StringBuilder sb = new StringBuilder();

            for (String app : selectedAppsStatic) {
                sb.append(app).append(",");
            }

            getSharedPreferences("FocusPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("blocked_apps", sb.toString())
                    .apply();

            Toast.makeText(this, "Apps saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void loadApps() {

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);

        for (ResolveInfo app : apps) {

            String appName = app.loadLabel(pm).toString();
            String packageName = app.activityInfo.packageName;
            Drawable icon = app.loadIcon(pm);

            appList.add(new AppModel(appName, packageName, icon));
        }
    }
}
//package com.example.socialmediaadiction;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.content.pm.ResolveInfo;
//import android.graphics.drawable.Drawable;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.ListView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.socialmediaadiction.Adapter.AppAdapter;
//import com.example.socialmediaadiction.Model.AppModel;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//public class AppSelectionActivity extends AppCompatActivity {
//
//    private ListView listView;
//    private Button btnSave;
//
//    private PackageManager pm;
//
//    private List<AppModel> appList = new ArrayList<>();
//    private Set<String> selectedApps = new HashSet<>();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_app_selection);
//
//        listView = findViewById(R.id.listViewApps);
//        btnSave = findViewById(R.id.btnSaveApps);
//
//        pm = getPackageManager();
//
//        loadApps();
//
//        AppAdapter adapter = new AppAdapter(this, appList);
//        listView.setAdapter(adapter);
//        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//
//        // 🔥 LOAD SAVED APPS
//        SharedPreferences prefs = getSharedPreferences("FocusPrefs", MODE_PRIVATE);
//        Set<String> savedApps = prefs.getStringSet("blocked_apps", new HashSet<>());
//
//        selectedApps = new HashSet<>(savedApps);
//
//        // 🔒 Restore checked state
//        for (int i = 0; i < appList.size(); i++) {
//            String pkg = appList.get(i).packageName;
//
//            if (FocusModeActivity.DEFAULT_BLOCKED_APPS.contains(pkg)
//                    || selectedApps.contains(pkg)) {
//                listView.setItemChecked(i, true);
//            }
//        }
//
//        // 🔥 Handle selection
//        listView.setOnItemClickListener((parent, view, position, id) -> {
//
//            String pkg = appList.get(position).packageName;
//
//            // 🔒 Prevent unselect default apps
//            if (FocusModeActivity.DEFAULT_BLOCKED_APPS.contains(pkg)) {
//                listView.setItemChecked(position, true);
//                Toast.makeText(this, "Default apps cannot be removed", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if (listView.isItemChecked(position)) {
//                selectedApps.add(pkg);
//            } else {
//                selectedApps.remove(pkg);
//            }
//        });
//
//        // 💾 SAVE
//        btnSave.setOnClickListener(v -> {
//
//            SharedPreferences.Editor editor = getSharedPreferences("FocusPrefs", MODE_PRIVATE).edit();
//            //editor.putStringSet("blocked_apps", selectedApps);
//            editor.putStringSet("blocked_apps", new HashSet<>(selectedApps));
//            editor.apply();
//
//            Toast.makeText(this, "Apps saved successfully", Toast.LENGTH_SHORT).show();
//            finish();
//        });
//    }
//
//    private void loadApps() {
//
//        Intent intent = new Intent(Intent.ACTION_MAIN, null);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//
//        List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
//
//        for (ResolveInfo app : apps) {
//
//            String appName = app.loadLabel(pm).toString();
//            String packageName = app.activityInfo.packageName;
//            Drawable icon = app.loadIcon(pm);
//
//            appList.add(new AppModel(appName, packageName, icon));
//        }
//    }
//}
////
//////package com.example.socialmediaadiction;
//////
//////import android.content.Intent;
//////import android.content.pm.ApplicationInfo;
//////import android.content.pm.PackageManager;
//////import android.content.pm.ResolveInfo;
//////import android.os.Bundle;
//////import android.widget.ArrayAdapter;
//////import android.widget.Button;
//////import android.widget.ListView;
//////import android.widget.Toast;
//////
//////import androidx.appcompat.app.AppCompatActivity;
//////
//////import java.util.ArrayList;
//////import java.util.HashSet;
//////import java.util.List;
//////import java.util.Set;
//////
//////public class AppSelectionActivity extends AppCompatActivity {
//////
//////    private ListView listView;
//////    private Button btnSave;
//////
//////    private PackageManager pm;
//////
//////    private List<String> appNames = new ArrayList<>();
//////    private List<String> packageNames = new ArrayList<>();
//////
//////    private Set<String> selectedApps = new HashSet<>();
//////
//////    @Override
//////    protected void onCreate(Bundle savedInstanceState) {
//////        super.onCreate(savedInstanceState);
//////        setContentView(R.layout.activity_app_selection);
//////
//////        listView = findViewById(R.id.listViewApps);
//////        btnSave = findViewById(R.id.btnSaveApps);
//////
//////        pm = getPackageManager();
//////
//////        loadApps();
//////
//////        ArrayAdapter<String> adapter = new ArrayAdapter<>(
//////                this,
//////                android.R.layout.simple_list_item_multiple_choice,
//////                appNames
//////        );
//////
//////        listView.setAdapter(adapter);
//////        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//////
//////        // 🔒 Preselect default apps
//////        for (int i = 0; i < packageNames.size(); i++) {
//////            String pkg = packageNames.get(i);
//////
//////            if (FocusModeActivity.DEFAULT_BLOCKED_APPS.contains(pkg)) {
//////                listView.setItemChecked(i, true);
//////            }
//////        }
//////
//////        // 🔥 Handle selection
//////        listView.setOnItemClickListener((parent, view, position, id) -> {
//////
//////            String pkg = packageNames.get(position);
//////
//////            // 🔒 Prevent unselecting default apps
//////            if (FocusModeActivity.DEFAULT_BLOCKED_APPS.contains(pkg)) {
//////                listView.setItemChecked(position, true);
//////                Toast.makeText(this, "Default apps cannot be removed", Toast.LENGTH_SHORT).show();
//////                return;
//////            }
//////
//////            if (listView.isItemChecked(position)) {
//////                selectedApps.add(pkg);
//////            } else {
//////                selectedApps.remove(pkg);
//////            }
//////        });
//////
//////        // 💾 Save selection
//////        btnSave.setOnClickListener(v -> {
//////            FocusModeActivity.userSelectedApps = selectedApps;
//////            Toast.makeText(this, "Apps saved successfully", Toast.LENGTH_SHORT).show();
//////            finish();
//////        });
//////    }
//////
//////    // 🔥 LOAD ALL INSTALLED APPS (USER APPS)
//////    private void loadApps() {
//////
//////        Intent intent = new Intent(Intent.ACTION_MAIN, null);
//////        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//////
//////        List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
//////
//////        for (ResolveInfo app : apps) {
//////
//////            String appName = app.loadLabel(pm).toString();
//////            String packageName = app.activityInfo.packageName;
//////
//////            appNames.add(appName);
//////            packageNames.add(packageName);
//////        }
//////    }
//////
//////}