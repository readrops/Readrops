package com.readrops.app.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.DisplayMetrics;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class Utils {

    public static final String HTTP_PREFIX = "http://";

    public static final String HTTPS_PREFIX = "https://";

    public static final int AVERAGE_WORDS_PER_MINUTE = 250;

    public static Bitmap getImageFromUrl(String url) {
        try {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .callTimeout(5, TimeUnit.SECONDS)
                    .build();
            Request request = new Request.Builder().url(url).build();

            Response response = okHttpClient.newCall(request).execute();

            if (response.isSuccessful()) {
                InputStream inputStream = response.body().byteStream();
                return BitmapFactory.decodeStream(inputStream);
            } else
                return null;
        } catch (Exception e) {
            return null; // no way to get the favicon
        }
    }

    public static int getDeviceWidth(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        return displayMetrics.widthPixels;
    }

    public static double readTimeFromString(String value) {
        int nbWords = value.split("\\s+").length;
        double minutes = (double) nbWords / AVERAGE_WORDS_PER_MINUTE;

        return minutes;
    }

    public static String getCssColor(@ColorInt int color) {
        return String.format(Locale.US, "rgba(%d,%d,%d,%.2f)",
                Color.red(color),
                Color.green(color),
                Color.blue(color),
                Color.alpha(color) / 255.0);
    }

    public static boolean isTypeImage(@NonNull String type) {
        return type.equals("image") || type.equals("image/jpeg") || type.equals("image/jpg")
                || type.equals("image/png");
    }

    public static void setDrawableColor(Drawable drawable, @ColorInt int color) {
        drawable.mutate().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
    }

    public static Drawable drawableWithColor(@ColorInt int color) {
        ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
        drawable.setIntrinsicWidth(50);
        drawable.setIntrinsicHeight(50);

        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));

        return drawable;
    }

}
