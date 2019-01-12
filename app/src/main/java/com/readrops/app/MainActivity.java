package com.readrops.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.readrops.readropslibrary.PageParser;

public class MainActivity extends AppCompatActivity {

    String url = "https://framablog.org/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread thread = new Thread(()-> {
            String feedUrl = PageParser.getFeedLink(url);

            Log.d("", "");
        });

        thread.start();
    }


}
