package com.tuvakov.zeyoube.android;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class ZeYouBe extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }
}
