package com.example.socialmediaadiction.Fragment;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "SharedPref_Name";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;

    // Constructor accepting generic Context
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void putStringData(String keyname, String value) {
        editor.putString(keyname, value);
        editor.commit();  // or editor.apply();
    }

    public String getStringData(String keyName) {
        return pref.getString(keyName, "");
    }

    public void putIntData(String keyname, int value) {
        editor.putInt(keyname, value);
        editor.commit();
    }

    public int getIntData(String keyName) {
        return pref.getInt(keyName, 0);
    }

    public void putBooleanData(String keyname, boolean value) {
        editor.putBoolean(keyname, value);
        editor.commit();
    }

    public boolean getBooleanData(String keyName) {
        return pref.getBoolean(keyName, false);
    }

    public void putLongData(String keyname, long value) {
        editor.putLong(keyname, value);
        editor.commit();
    }

    public long getLongData(String keyName, long defaultValue) {
        return pref.getLong(keyName, defaultValue);
    }

    public void removeData(String keyName) {
        editor.remove(keyName);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean("isLoggedIn", false);
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.apply();
    }

    public void logoutUser() {
        editor.clear();
        editor.apply();
    }

}
