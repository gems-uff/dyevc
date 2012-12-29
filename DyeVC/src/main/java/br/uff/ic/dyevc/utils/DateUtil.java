package br.uff.ic.dyevc.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
     * @return the current date formated as a string.
     */
    public static String getFormattedCurrentDate() {
        return dateFormat.format(new Date(System.currentTimeMillis()));
    }

    /**
     * Formats the specified date using the default date format.
     * @param date the date to be formatted.
     * @return the specified date formatted according to default date format.
     */
    public static String format(Date date) {
        return dateFormat.format(date);
    }

    /**
     * Formats specified date using the specified format.
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
     * @param calendar the calendar to extract date to be formatted
     * @return the date formatted according to default date format.
     */
    public static String format(Calendar calendar) {
        return dateFormat.format(calendar.getTime());
    }


}
