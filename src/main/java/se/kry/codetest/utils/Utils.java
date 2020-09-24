package se.kry.codetest.utils;

import java.net.URI;
import java.net.URISyntaxException;
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
     * Gets now based on the Time Zone and with the follow date pattern.
     *
     * @return the now
     */
    public static String getNow() {
        Instant instant = new Date().toInstant();
        LocalDateTime ldt = instant.atOffset(ZoneOffset.UTC).toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_PATTERN);

        return ldt.format(formatter);
    }

    /**
     * Is valid url boolean.
     *
     * @param url the url
     * @return the boolean
     */
    public static boolean isValidUrl(String url) {
        try {
            if (url == null || url.equals("")) {
                return false;
            }
            URI parsedUrl = new URI(url).parseServerAuthority();
            if (parsedUrl.getScheme() == null || (!parsedUrl.getScheme().equals("http") &&
                    !parsedUrl.getScheme().equals("https"))) {
                return false;
            }
        } catch (URISyntaxException e) {
            return false;
        }
        return true;
    }

    public static String generateUniqueUserCookieId() {
        return Math.floor(Math.random() * 21) + new Date().getTime() + "";
    }
}
