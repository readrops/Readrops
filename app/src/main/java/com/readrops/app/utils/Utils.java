package com.readrops.app.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

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
}
