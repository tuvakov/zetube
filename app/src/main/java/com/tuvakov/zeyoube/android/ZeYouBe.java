package com.tuvakov.zeyoube.android;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;
import com.tuvakov.zeyoube.android.dagger.AppComponent;
import com.tuvakov.zeyoube.android.dagger.AppModule;
import com.tuvakov.zeyoube.android.dagger.DaggerAppComponent;

public class ZeYouBe extends Application {

    private AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
        mAppComponent = DaggerAppComponent.factory().create(new AppModule(this));
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }
}
