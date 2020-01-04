package com.tuvakov.zeyoube.android;

import com.tuvakov.zeyoube.android.utils.DateTimeUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class DateTimeUtilsTest {

    private DateTimeUtils dateTimeUtils = new DateTimeUtils();

    @Test
    public void getUtcEpochNDaysAgo_now() {
        long utcNow = dateTimeUtils.getUtcEpoch();
        long utcZeroDaysAgo = dateTimeUtils.getUtcEpochNDaysAgo(0);
        assertEquals(utcNow, utcZeroDaysAgo);
    }

    @Test
    public void getUtcEpochNDaysAgo_dayAgo() {
        long utcDayAgo = dateTimeUtils.getUtcEpochNDaysAgo(1);
        long utcNow = dateTimeUtils.getUtcEpoch();
        long difference = utcNow - utcDayAgo;
        long expectedDifference = TimeUnit.DAYS.toMillis(1); // A day in millis;
        assertEquals(expectedDifference, difference);
    }
}
