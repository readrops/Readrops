package com.readrops.app.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;
import com.readrops.api.utils.HttpManager;

import org.jsoup.Jsoup;

import java.io.InputStream;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class Utils {

    public static final String HTTP_PREFIX = "http://";

    public static final String HTTPS_PREFIX = "https://";

    private static final int AVERAGE_WORDS_PER_MINUTE = 250;

    public static Bitmap getImageFromUrl(String url) {
        try {
            OkHttpClient okHttpClient = HttpManager.getInstance().getOkHttpClient();
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

    public static double readTimeFromString(String value) {
        int nbWords = value.split("\\s+").length;

        return (double) nbWords / AVERAGE_WORDS_PER_MINUTE;
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


    public static void showSnackBarWithAction(View root, String message, String action, View.OnClickListener listener) {
        Snackbar snackbar = Snackbar.make(root, message, Snackbar.LENGTH_LONG);
        snackbar.setAction(action, listener);

        snackbar.show();
    }

    public static void showSnackbar(View root, String message) {
        Snackbar snackbar = Snackbar.make(root, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    /**
     * Remove html tags and trim the text
     *
     * @param text string to clean
     * @return cleaned text
     */
    public static String cleanText(String text) {
        return Jsoup.parse(text).text().trim();
    }

    public static Bitmap getBitmapFromDrawable(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;

    }

    public static boolean isColorTooBright(@ColorInt int color) {
        return getColorLuma(color) > 210;
    }

    public static boolean isColorTooDark(@ColorInt int color) {
        return getColorLuma(color) < 40;
    }

    private static double getColorLuma(@ColorInt int color) {
        int r = (color >> 16) & 0xff;
        int g = (color >>  8) & 0xff;
        int b = (color >>  0) & 0xff;

        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }
}
