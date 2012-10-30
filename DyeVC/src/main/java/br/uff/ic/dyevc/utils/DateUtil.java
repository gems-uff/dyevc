package br.uff.ic.dyevc.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Cristiano
 */
public class DateUtil {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static String getFormattedCurrentDate() {
        return dateFormat.format(new Date(System.currentTimeMillis()));
    }

    public static String format(Date date) {
        return dateFormat.format(date);
    }

    public static String format(Calendar calendar) {
        return dateFormat.format(calendar.getTime());
    }


}
