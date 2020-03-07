package com.tuvakov.zetube.android;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;
import com.tuvakov.zetube.android.dagger.AppComponent;
import com.tuvakov.zetube.android.dagger.AppModule;
import com.tuvakov.zetube.android.dagger.DaggerAppComponent;

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
