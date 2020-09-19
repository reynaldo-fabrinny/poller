package se.kry.codetest.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Class with Utility methods.
 */
public class Utils {

    /**
     * Gets now.
     *
     * @return the now
     */
    public static String getNow() {
        Instant instant = new Date().toInstant();
        LocalDateTime ldt = instant.atOffset(ZoneOffset.UTC).toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_PATTERN);

        return ldt.format(formatter);
    }
}
