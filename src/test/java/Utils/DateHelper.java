/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Functions.Utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author ABMV537
 */
public class DateHelper {

    public static Date toDate(String date) throws ParseException {
        Date d = new Date();
        try {
            d = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse(date);
        } catch (ParseException e) {
            throw new ParseException("The date \"" + date + "\" has an invalid format. Please use format \"dd-MMM-yyyy HH:mm:ss\" e.g: 07-Jul-2017 11:42:43", 0);
        }
        return d;
    }
    public static String getDate() {
        String p =null;
        try {
            DateTimeFormatter dtf = DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm:ss");
            // Parsing the date
            p= DateTime.now().toString(dtf);
            //jodatime = dtf.parseDateTime(DateTime.now().toString(dtf));

            //return jodatime.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }

    public static String toString(Date date) throws ParseException {
        return new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(date);
    }

    public static long dateDifferencemillisSeconds(String date1, String date2) throws ParseException {
        Date d1 = toDate(date1);
        Date d2 = toDate(date2);
        long differenceInSenconds = (d1.getTime() - d2.getTime());
        return differenceInSenconds;
    }
}
