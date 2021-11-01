package com.readrops.app.utils;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.koin.java.KoinJavaComponent;

public final class SharedPreferencesManager {

    public static void writeValue(String key, Object value) {
        SharedPreferences sharedPref = KoinJavaComponent.get(SharedPreferences.class);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (value instanceof Boolean)
            editor.putBoolean(key, (Boolean) value);
        else if (value instanceof String)
            editor.putString(key, (String) value);

        editor.apply();
    }

    public static void writeValue(SharedPrefKey sharedPrefKey, Object value) {
        writeValue(sharedPrefKey.key, value);
    }

    public static int readInt(SharedPrefKey sharedPrefKey) {
        SharedPreferences sharedPreferences = KoinJavaComponent.get(SharedPreferences.class);
        return sharedPreferences.getInt(sharedPrefKey.key, sharedPrefKey.getIntDefaultValue());
    }

    public static boolean readBoolean(SharedPrefKey sharedPrefKey) {
        SharedPreferences sharedPreferences = KoinJavaComponent.get(SharedPreferences.class);
        return sharedPreferences.getBoolean(sharedPrefKey.key, sharedPrefKey.getBooleanDefaultValue());
    }

    public static String readString(String key) {
        SharedPreferences sharedPreferences = KoinJavaComponent.get(SharedPreferences.class);
        return sharedPreferences.getString(key, null);
    }

    public static String readString(SharedPrefKey sharedPrefKey) {
        SharedPreferences sharedPreferences = KoinJavaComponent.get(SharedPreferences.class);
        return sharedPreferences.getString(sharedPrefKey.key, sharedPrefKey.getStringDefaultValue());
    }

    public static void remove(String key) {
        SharedPreferences sharedPreferences = KoinJavaComponent.get(SharedPreferences.class);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove(key);
        editor.apply();
    }

    public enum SharedPrefKey {
        SHOW_READ_ARTICLES("show_read_articles", false),
        ITEMS_TO_PARSE_MAX_NB("items_to_parse_max_nb", "20"),
        OPEN_ITEMS_IN("open_items_in", "0"),
        DARK_THEME("dark_theme", "false"),
        AUTO_SYNCHRO("auto_synchro", "0"),
        HIDE_FEEDS("hide_feeds", false),
        MARK_ITEMS_READ_ON_SCROLL("mark_items_read", false);

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
