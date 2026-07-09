package com.example.socialmediaadiction.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.socialmediaadiction.AppSelectionActivity;
import com.example.socialmediaadiction.FocusModeActivity;
import com.example.socialmediaadiction.Model.AppModel;
import com.example.socialmediaadiction.R;

import java.util.List;

public class AppAdapter extends BaseAdapter {

    private Context context;
    private List<AppModel> appList;

    public AppAdapter(Context context, List<AppModel> appList) {
        this.context = context;
        this.appList = appList;
    }

    @Override
    public int getCount() {
        return appList.size();
    }

    @Override
    public Object getItem(int position) {
        return appList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.appIcon);
        TextView name = convertView.findViewById(R.id.appName);
        CheckBox checkBox = convertView.findViewById(R.id.appCheckBox);

        AppModel app = appList.get(position);

        icon.setImageDrawable(app.icon);
        name.setText(app.name);

        // 🔥 IMPORTANT: avoid recycling bug
        checkBox.setOnCheckedChangeListener(null);

        // 🔒 Default apps always checked
        boolean isChecked = AppSelectionActivity.selectedAppsStatic.contains(app.packageName)
                || FocusModeActivity.DEFAULT_BLOCKED_APPS.contains(app.packageName);

        checkBox.setChecked(isChecked);

        // 🔥 Handle selection
        checkBox.setOnCheckedChangeListener((buttonView, checked) -> {

            if (FocusModeActivity.DEFAULT_BLOCKED_APPS.contains(app.packageName)) {
                checkBox.setChecked(true);
                return;
            }

            if (checked) {
                AppSelectionActivity.selectedAppsStatic.add(app.packageName);
            } else {
                AppSelectionActivity.selectedAppsStatic.remove(app.packageName);
            }
        });

        return convertView;
    }
}
//package com.example.socialmediaadiction.Adapter;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.CheckBox;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import com.example.socialmediaadiction.AppSelectionActivity;
//import com.example.socialmediaadiction.FocusModeActivity;
//import com.example.socialmediaadiction.Model.AppModel;
//import com.example.socialmediaadiction.R;
//
//import java.util.List;
//
//public class AppAdapter extends BaseAdapter {
//
//    private Context context;
//    private List<AppModel> appList;
//
//    public AppAdapter(Context context, List<AppModel> appList) {
//        this.context = context;
//        this.appList = appList;
//    }
//
//    @Override
//    public int getCount() {
//        return appList.size();
//    }
//
//    @Override
//    public Object getItem(int position) {
//        return appList.get(position);
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//
//        if (convertView == null) {
//            convertView = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false);
//        }
//
//        ImageView icon = convertView.findViewById(R.id.appIcon);
//        TextView name = convertView.findViewById(R.id.appName);
//        CheckBox checkBox = convertView.findViewById(R.id.appCheckBox);
//
//        AppModel app = appList.get(position);
//
//        icon.setImageDrawable(app.icon);
//        name.setText(app.name);
//
//        // ❗ Remove old listener to avoid bugs
//        checkBox.setOnCheckedChangeListener(null);
//
//        // ✅ Set state from saved data
//        checkBox.setChecked(
//                AppSelectionActivity.selectedAppsStatic.contains(app.packageName)
//                        || FocusModeActivity.DEFAULT_BLOCKED_APPS.contains(app.packageName)
//        );
//
//        // ✅ Handle user selection
//        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
//
//            if (FocusModeActivity.DEFAULT_BLOCKED_APPS.contains(app.packageName)) {
//                checkBox.setChecked(true);
//                return;
//            }
//
//            if (isChecked) {
//                AppSelectionActivity.selectedAppsStatic.add(app.packageName);
//            } else {
//                AppSelectionActivity.selectedAppsStatic.remove(app.packageName);
//            }
//        });
//
//        return convertView;
//    }
//}
//
