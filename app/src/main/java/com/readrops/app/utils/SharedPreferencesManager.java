package com.readrops.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

public final class SharedPreferencesManager {

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
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

    public static void writeValue(Context context, SharedPrefKey sharedPrefKey, Object value) {
        writeValue(context, sharedPrefKey.key, value);
    }

    public static int readInt(Context context, SharedPrefKey sharedPrefKey) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getInt(sharedPrefKey.key, sharedPrefKey.getIntDefaultValue());
    }

    public static boolean readBoolean(Context context, SharedPrefKey sharedPrefKey) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getBoolean(sharedPrefKey.key, sharedPrefKey.getBooleanDefaultValue());
    }

    public static String readString(Context context, String key) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getString(key, null);
    }

    public static String readString(Context context, SharedPrefKey sharedPrefKey) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getString(sharedPrefKey.key, sharedPrefKey.getStringDefaultValue());
    }

    public enum SharedPrefKey {
        SHOW_READ_ARTICLES("show_read_articles", false),
        ITEMS_TO_PARSE_MAX_NB("items_to_parse_max_nb", "20"),
        OPEN_ITEMS_IN("open_items_in", "0"),
        DARK_THEME("dark_theme", "false");

        @NonNull
        private String key;
        @NonNull
        private Object defaultValue;

        public boolean getBooleanDefaultValue() {
            return Boolean.valueOf(defaultValue.toString());
        }

        public String getStringDefaultValue() {
            return (String) defaultValue;
        }

        public int getIntDefaultValue() {
            return Integer.parseInt(defaultValue.toString());
        }

        SharedPrefKey(@NonNull String key, @NonNull Object defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }
    }
}
