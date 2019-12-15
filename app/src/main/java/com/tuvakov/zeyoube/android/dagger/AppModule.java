package com.tuvakov.zeyoube.android.dagger;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private final Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides @Singleton
    Application provideApplication() {
        return application;
    }

    @Provides @Singleton
    Context provideAppContext() {
        return application;
    }

    @Provides @Singleton
    SharedPreferences provideSharedPref(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}