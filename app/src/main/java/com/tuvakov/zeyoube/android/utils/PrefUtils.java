package com.tuvakov.zeyoube.android.utils;

import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PrefUtils {

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String PREF_IS_FROM_SCRATCH_SYNC = "isFromScratchSync";

    private final SharedPreferences mSharedPreferences;

    @Inject
    public PrefUtils(SharedPreferences sharedPreferences) {
        mSharedPreferences = sharedPreferences;
    }

    public boolean isFromScratchSync() {
        return mSharedPreferences.getBoolean(PREF_IS_FROM_SCRATCH_SYNC, true);
    }

    public  void setIsFromScratchSync(boolean value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(PREF_IS_FROM_SCRATCH_SYNC, value);
        editor.apply();
        editor.commit();
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
}
