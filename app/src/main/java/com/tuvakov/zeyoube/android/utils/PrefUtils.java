package com.tuvakov.zeyoube.android.utils;

import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PrefUtils {

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String PREF_LAST_SYNC_TIME = "lastSyncTime";

    private final SharedPreferences mSharedPreferences;

    @Inject
    public PrefUtils(SharedPreferences sharedPreferences) {
        mSharedPreferences = sharedPreferences;
    }


    public String getAccountName() {
        return mSharedPreferences.getString(PREF_ACCOUNT_NAME, null);
    }

    public void saveAccountName(String accountName) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(PREF_ACCOUNT_NAME, accountName);
        editor.apply();
    }

    public void deleteAccountName() {
        saveAccountName(null);
    }

    public long getLastSyncTime() {
        return mSharedPreferences.getLong(PREF_LAST_SYNC_TIME, 0);
    }

    public void saveLastSyncTime(long lastSyncTime) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(PREF_LAST_SYNC_TIME, lastSyncTime);
        editor.apply();
    }
}
