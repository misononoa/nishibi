package cc.misononoa.nishibi.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeUtils {

    private TimeUtils() {
    }

    public static String toString(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toString();
    }

    public static String toString(Instant instant) {
        return toString(instant.atZone(ZoneId.systemDefault()));
    }

    public static String toString(LocalDateTime localDateTime) {
        return toString(localDateTime.atZone(ZoneId.systemDefault()));
    }

    public static String toString(LocalDate localDate) {
        return toString(localDate.atStartOfDay(ZoneId.systemDefault()));
    }

    public static String epochMilliToString(long epochMilli) {
        return toString(Instant.ofEpochMilli(epochMilli));
    }

    public static String nowString() {
        return toString(ZonedDateTime.now());
    }

}
