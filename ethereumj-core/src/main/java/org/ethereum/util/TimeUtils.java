package org.ethereum.util;

/**
 * @author Mikhail Kalinin
 * @since 10.08.2015
 */
public class TimeUtils {

    /**
     * Converts minutes to millis
     *
     * @param minutes time in minutes
     * @return corresponding millis value
     */
    public static long minutesToMillis(long minutes) {
        return minutes * 60 * 1000;
    }

    /**
     * Converts seconds to millis
     *
     * @param seconds time in seconds
     * @return corresponding millis value
     */
    public static long secondsToMillis(long seconds) {
        return seconds * 1000;
    }

    /**
     * Converts millis to minutes
     *
     * @param millis time in millis
     * @return time in minutes
     */
    public static long millisToMinutes(long millis) {
        return Math.round(millis / 60.0 / 1000.0);
    }

    /**
     * Returns timestamp in the future after some millis passed from now
     *
     * @param millis millis count
     * @return future timestamp
     */
    public static long timeAfterMillis(long millis) {
        return System.currentTimeMillis() + millis;
    }
}
