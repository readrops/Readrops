package com.readrops.app.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class Utils {

    public static final String HTTP_PREFIX = "http://";

    public static final String HTTPS_PREFIX = "https://";

    public static void displayErrorInMainThread(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);

        if (!(Looper.myLooper() == Looper.getMainLooper())) {
            Handler handler = new Handler(Looper.getMainLooper());
            Looper.prepare();
            handler.post(toast::show);
        } else
            toast.show();

    }

    public static Bitmap getImageFromUrl(String url) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        Response response = okHttpClient.newCall(request).execute();

        if (response.isSuccessful()) {
            InputStream inputStream = response.body().byteStream();
            return BitmapFactory.decodeStream(inputStream);
        } else
            return null;
    }

    public static int getDeviceWidth(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        return displayMetrics.widthPixels;
    }

}
