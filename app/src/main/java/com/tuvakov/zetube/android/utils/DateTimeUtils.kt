package com.tuvakov.zetube.android.utils

import com.tuvakov.zetube.android.utils.SyncUtils.Companion.SYNC_INTERVAL_HOURS
import org.threeten.bp.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DateTimeUtils @Inject constructor() {
    val utcEpoch: Long
        get() = Instant.now().toEpochMilli()

    fun getUtcEpochNDaysAgo(n: Int): Long {
        val now = Instant.now()
        return now.minusMillis(TimeUnit.DAYS.toMillis(n.toLong())).toEpochMilli()
    }

    fun isSyncAllowed(since: Long): Boolean {
        return utcEpoch - since >= HOUR_IN_MILLIS * SYNC_INTERVAL_HOURS
    }

    companion object {
        private const val SECOND_IN_MILLIS: Long = 1000
        private const val MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60
        private const val HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60
    }
}