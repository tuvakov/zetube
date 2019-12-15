package com.tuvakov.zeyoube.android.data;


import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;


public class DateConverter {

    public long toEpochMilli(ZonedDateTime publishedAtUtc) {
        if (publishedAtUtc == null) {
            return 0;
        }
        return publishedAtUtc.toInstant().toEpochMilli();
    }

    public ZonedDateTime toZonedDateTime(long epoch) {
        Instant instant = Instant.ofEpochMilli(epoch);
        return instant.atZone(ZoneId.of("UTC"));
    }
}
