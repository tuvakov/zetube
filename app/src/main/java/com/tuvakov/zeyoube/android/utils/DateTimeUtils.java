package com.tuvakov.zeyoube.android.utils;

import org.threeten.bp.Instant;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DateTimeUtils {

    private static final long SECOND_IN_MILLIS = 1000;
    private static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
    private static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
    private static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;

    @Inject
    public DateTimeUtils() { }

    public long getUtcEpoch() {
        return Instant.now().toEpochMilli();
    }

    public long getUtcEpochNDaysAgo(int n) {
        Instant now = Instant.now();
        return now.minusMillis(TimeUnit.DAYS.toMillis(n)).toEpochMilli();
    }

    public boolean hasDayPassed(long since) {
        return getUtcEpoch() - since >= DAY_IN_MILLIS;
    }
}
