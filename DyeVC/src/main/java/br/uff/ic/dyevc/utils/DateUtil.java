/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author DanCastellani
 */
public class DateUtil {

    private static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy - HH:mm:ss");

    public static synchronized String format(Date date) {
        return dateFormat.format(date);
    }

    public static synchronized String format(Calendar calendar) {
        return dateFormat.format(calendar.getTime());
    }


}
