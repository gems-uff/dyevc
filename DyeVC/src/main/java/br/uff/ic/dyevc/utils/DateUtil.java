package br.uff.ic.dyevc.utils;

//~--- JDK imports ------------------------------------------------------------

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Utility methods to work with dates.
 *
 * @author Cristiano
 */
public class DateUtil {
    /**
     * Default date format to be used when formatting dates.
     */
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Returns the current date formated according to default date format.
     *
     * @return the current date formated as a string.
     */
    public static String getFormattedCurrentDate() {
        return dateFormat.format(new Date(System.currentTimeMillis()));
    }

    /**
     * Formats the specified date using the default date format.
     *
     * @param date the date to be formatted.
     * @return the specified date formatted according to default date format.
     */
    public static String format(Date date) {
        return dateFormat.format(date);
    }

    /**
     * Formats specified date using the specified format.
     *
     * @param date the date to be formatted.
     * @param format the format to be used.
     * @return the specified date formatted according to specified format.
     * @see DateFormat
     */
    public static String format(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * Extrats date from the specified calendar and formats it using default date format.
     *
     * @param calendar the calendar to extract date to be formatted
     * @return the date formatted according to default date format.
     */
    public static String format(Calendar calendar) {
        return dateFormat.format(calendar.getTime());
    }

    /**
     * Adds the specified number of days to the given date. The number of days can be negative, so that the original
     * date will be decremented.
     *
     * @param date The date to be added
     * @param days The number of days to add
     * @return A new date.
     */
    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);    // minus number would decrement the days

        return cal.getTime();
    }

    /**
     * Retrieves the local time, but in UTC time zone.
     * @return The local time according to UTC time zone.
     */
    public static Date getLocalTimeInUTC() {
        return new GregorianCalendar(TimeZone.getTimeZone("UTC")).getTime();
    }

    /**
     * Convert the specified date from local time zone to UTC time zone.
     * @param date The date to be converted.
     * @return The converted date
     */
    public static Date toUTC(Date date) {
        Calendar cal = new GregorianCalendar(TimeZone.getDefault());
        cal.setTime(date);
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));

        return cal.getTime();
    }

    /**
     * Convert the specified date from UTC time zone to local time zone.
     * @param date The date to be converted.
     * @return The converted date
     */
    public static Date fromUTC(Date date) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        cal.setTimeZone(TimeZone.getDefault());

        return cal.getTime();
    }
}
