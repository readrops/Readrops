package com.readrops.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class SharedPreferencesManager {

    private static final String PREFS = "com.readrops.app.uniquepreferences";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void writeValue(Context context, SharedPrefKey key, Object value) {
        writeValue(context, key.toString(), value);
    }

    public static void writeValue(Context context, String key, Object value) {
        SharedPreferences sharedPref = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (value instanceof Boolean)
            editor.putBoolean(key, (Boolean) value);
        else if (value instanceof String)
            editor.putString(key, (String) value);

        editor.apply();
    }

    public static int readInt(Context context, SharedPrefKey key) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getInt(key.toString(), 0);
    }

    public static boolean readBoolean(Context context, SharedPrefKey key) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getBoolean(key.toString(), false);
    }

    public static String readString(Context context, SharedPrefKey key) {
        return readString(context, key.toString());
    }

    public static String readString(Context context, String key) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getString(key.toString(), null);
    }

    public enum SharedPrefKey {
        SHOW_READ_ARTICLES
    }
}
