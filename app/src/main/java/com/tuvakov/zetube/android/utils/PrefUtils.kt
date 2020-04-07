package com.tuvakov.zetube.android.utils

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefUtils @Inject constructor(private val mSharedPreferences: SharedPreferences) {
    val accountName: String?
        get() = mSharedPreferences.getString(PREF_ACCOUNT_NAME, null)

    fun saveAccountName(accountName: String?) {
        mSharedPreferences.edit { putString(PREF_ACCOUNT_NAME, accountName) }
    }

    fun deleteAccountName() {
        saveAccountName(null)
    }

    val lastSyncTime: Long
        get() = mSharedPreferences.getLong(PREF_LAST_SYNC_TIME, 0)

    fun saveLastSyncTime(lastSyncTime: Long) {
        mSharedPreferences.edit { putLong(PREF_LAST_SYNC_TIME, lastSyncTime) }
    }

    companion object {
        private const val PREF_ACCOUNT_NAME = "accountName"
        private const val PREF_LAST_SYNC_TIME = "lastSyncTime"
    }

}