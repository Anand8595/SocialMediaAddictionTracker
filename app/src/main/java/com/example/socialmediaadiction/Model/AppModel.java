package com.example.socialmediaadiction.Model;
import android.graphics.drawable.Drawable;

public class AppModel {
    public String name;
    public String packageName;
    public Drawable icon;

    public AppModel(String name, String packageName, Drawable icon) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
    }
}