package cc.misononoa.nishibi.common.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SqlTimeUtils {

    private SqlTimeUtils() {
    }

    private static long toEpochMilli(LocalDateTime dateTime) {
        return ZonedDateTime.of(dateTime, ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static java.sql.Date toSqlDate(LocalDateTime dateTime) {
        return new Date(toEpochMilli(dateTime));
    }

    public static java.sql.Timestamp toSqlTimestamp(LocalDateTime dateTime) {
        return new Timestamp(toEpochMilli(dateTime));
    }

    public static java.sql.Date getTodaySqlDate() {
        return new Date(System.currentTimeMillis());
    }

    public static java.sql.Timestamp getNowTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

}
