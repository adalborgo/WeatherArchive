package it.dibis.common;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Some methods for time management
 *
 * @author Antonio Dal Borgo (adalborgo@gmail.com)
 */
public class CalendarUtils {

    private int dayOfYear = -1;
    private int year = -1;

    private Calendar calendar = null;

    /**
     * Transforms calendar in a long (ms since 1-1-1970)
     * long cal = calendar.getTime().getTime();
     * +: advance; -: delay
     * long cal += offset;
     * calendar.setTimeInMillis(cal);
     *
     * @param sday
     * @param smonth
     * @param syear
     */
    void setCalendar(String sday, String smonth, String syear) {
        int day = stringToInt(sday);
        int month = stringToInt(smonth);
        int year = stringToInt(syear);

        setCalendar(day, month, year);
    }

    void setCalendar(int day, int month, int year) {
        this.calendar = new GregorianCalendar(year, month - 1, day);
        this.dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        this.year = calendar.get(Calendar.YEAR);
    }

    void setCalendar(boolean setUTC) {
        if (setUTC) {
            this.calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));    // Ora UTC
        } else {
            this.calendar = Calendar.getInstance();    // Local Time System
        }

        this.dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        this.year = calendar.get(Calendar.YEAR);
    }

    /**
     * Returns the Calendar system with offset
     *
     * @param offset (+: advance; -: delay)
     * @return Calendar
     */
    public static Calendar getCalendar(long offset) {
        Calendar calendar = Calendar.getInstance();
        if (offset != 0) {
            long cal = Calendar.getInstance().getTimeInMillis() + offset;
            calendar.setTimeInMillis(cal);
        }

        return calendar;
    }

    /**
     * Get Daylight Saving Time
     *
     * @param date
     * @param timeZone
     * @return boolean
     */
    public static boolean getDST(Date date, String timeZone) {
        // boolean dst = getDST(new Date(), Constants.STRING_TIMEZONE);
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        return tz.inDaylightTime(date);
    }

	/**
	 * Get TimeZone
	 * @param sTimeZone
	 * @return
	 */
	public static TimeZone getTimeZone(String sTimeZone) {
        TimeZone tz = null;
        String[] tList = TimeZone.getAvailableIDs();
        boolean found = false;
        for (String s : tList) {
            if (s.equals(sTimeZone)) {
                found = true;
                tz = TimeZone.getTimeZone(s);
                break;
            }
        }

        return (found) ? tz : TimeZone.getDefault();
    }

    public static String getDate(Calendar calendar) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT")); // Added for UTC
        return formatter.format(calendar.getTime());
    }

    /**
     * Return Calendar of yesterday (-24 h), same hour, minute and second
     */
    public void setYesterdayCalendar() {
        // Calendar calendar = Utils.getCalendar(-3600000); // Yesterday (offset = -1 hour)
        final long MILLISEC_OF_DAY = 86400000L;
        this.calendar = Calendar.getInstance();    // Now
        long cal = calendar.getTime().getTime() - MILLISEC_OF_DAY;
        this.calendar.setTimeInMillis(cal);

        this.dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        this.year = calendar.get(Calendar.YEAR);
    }

    /**
     * @param s
     * @return int
     */
    public static int stringToInt(String s) {
        int n = 0;
        try {
            return Integer.valueOf(s).intValue();

        } catch (NumberFormatException e) {
            System.out.println("Non e' un int!");
        }

        return Integer.MIN_VALUE;
    }

}
