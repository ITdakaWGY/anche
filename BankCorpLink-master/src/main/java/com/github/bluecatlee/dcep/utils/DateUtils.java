package com.github.bluecatlee.dcep.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

public class DateUtils {
    public static final long MILLIS_PER_SECOND = 1000L;
    public static final long MILLIS_PER_MINUTE = 60000L;
    public static final long MILLIS_PER_HOUR = 3600000L;
    public static final long MILLIS_PER_DAY = 86400000L;
    public static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");
    public static final FastDateFormat DATETIME_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
    public static final FastDateFormat DATETIME_MILLISECOND_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss:SSS");
    public static final String COMPACT_DATE_FORMAT_PATTERN = "yyyyMMdd";
    public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";
    public static String DATETIME_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATETIME_MILLISECOND_PATTERN = "yyyy-MM-dd HH:mm:ss:SSS";

    public DateUtils() {
    }

    public static Date parse(String str) {
        return parse(str, "yyyy-MM-dd");
    }

    public static Date parseString(String str) {
        return parse(str, DATETIME_FORMAT_PATTERN);
    }

    public static Date parse(String str, String pattern) {
        if (StringUtils.isBlank(str)) {
            return null;
        } else {
            SimpleDateFormat parser = new SimpleDateFormat(pattern);

            try {
                return parser.parse(str);
            } catch (ParseException var4) {
                throw new IllegalArgumentException("Can't parse " + str + " using " + pattern);
            }
        }
    }

    public static String format(Date date, String pattern) {
        if (date == null) {
            return null;
        } else {
            FastDateFormat df = FastDateFormat.getInstance(pattern);
            return df.format(date);
        }
    }

    public static String format(Date date) {
        return date == null ? null : DATE_FORMAT.format(date);
    }

    public static String getCurrentDateAsString() {
        return DATE_FORMAT.format(new Date());
    }

    public static String getCurrentDateAsString(String pattern) {
        FastDateFormat formatter = FastDateFormat.getInstance(pattern);
        return formatter.format(new Date());
    }

    public static String getCurrentDateTimeAsString() {
        return DATETIME_FORMAT.format(new Date());
    }

    public static String getCurrentDateTimeAsString(String pattern) {
        FastDateFormat formatter = FastDateFormat.getInstance(pattern);
        return formatter.format(new Date());
    }

    public static Date getStartDateTimeOfCurrentMonth() {
        return getStartDateTimeOfMonth(new Date());
    }

    public static Date getStartDateTimeOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(5, 1);
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        return cal.getTime();
    }

    public static Date getEndDateTimeOfCurrentMonth() {
        return getEndDateTimeOfMonth(new Date());
    }

    public static Date getEndDateTimeOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(5, cal.getActualMaximum(5));
        cal.set(11, 23);
        cal.set(12, 59);
        cal.set(13, 59);
        return cal.getTime();
    }

    public static Date getStartTimeOfCurrentDate() {
        return getStartTimeOfDate(new Date());
    }

    public static Date getStartTimeOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        cal.set(14, 0);
        return cal.getTime();
    }

    public static Date getEndTimeOfCurrentDate() {
        return getEndTimeOfDate(new Date());
    }

    public static Date getEndTimeOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(11, 23);
        cal.set(12, 59);
        cal.set(13, 59);
        return cal.getTime();
    }

    public static Date addHours(Date date, int hours) {
        return add(date, 11, hours);
    }

    public static Date addMinutes(Date date, int minutes) {
        return add(date, 12, minutes);
    }

    public static Date addDays(Date date, int days) {
        return add(date, 5, days);
    }

    public static Date addMonths(Date date, int months) {
        return add(date, 2, months);
    }

    public static Date addYears(Date date, int years) {
        return add(date, 1, years);
    }

    private static Date add(Date date, int field, int amount) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(field, amount);
        return cal.getTime();
    }

    public static final int daysBetween(Date early, Date late) {
        Calendar ecal = Calendar.getInstance();
        Calendar lcal = Calendar.getInstance();
        ecal.setTime(early);
        lcal.setTime(late);
        long etime = ecal.getTimeInMillis();
        long ltime = lcal.getTimeInMillis();
        return (int)((ltime - etime) / 86400000L);
    }

    public static String getNow() {
        String sCurTime = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            Calendar sNow = Calendar.getInstance();
            sCurTime = sdf.format(sNow.getTime());
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return sCurTime;
    }

    public static boolean is_valid_date_formate(String str_date) {
        if (str_date.length() > 10) {
            str_date = str_date.substring(1);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            sdf.parse(str_date);
            return true;
        } catch (ParseException var3) {
            return false;
        }
    }

    public static boolean is_valid_str_date_formate(String str_date) {
        boolean flag = false;
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");

        try {
            if (str_date.equals(date.format(date.parse(str_date)))) {
                flag = true;
            }
        } catch (ParseException var4) {
        }

        return flag;
    }

    public static boolean before(Date first, Date second) {
        return first.before(second);
    }

    public static String[] getSpecailDates(String beginDate, String endDate, int month, int week, int day) {
        if (beginDate != null && !beginDate.trim().equals("") && endDate != null && !endDate.trim().equals("")) {
            Date bd = parse(beginDate, "yyyy-MM-dd");
            Date ed = parse(endDate, "yyyy-MM-dd");
            if (bd.after(ed)) {
                return new String[0];
            } else {
                ArrayList<String> list = new ArrayList();
                int diffDays = Long.valueOf((ed.getTime() - bd.getTime()) / 86400000L).intValue();

                for(int i = 0; i <= diffDays; ++i) {
                    Date curD = addDays(bd, i);
                    Calendar cc = Calendar.getInstance();
                    cc.setTime(curD);
                    int curDay = cc.get(7);
                    int curWeek = cc.get(4);
                    int curMonth = cc.get(2);
                    if (month == -1) {
                        if (week == 0) {
                            if (day == curDay) {
                                list.add(format(curD, "yyyy-MM-dd"));
                            }
                        } else if (week == curWeek) {
                            if (day == 0) {
                                list.add(format(curD, "yyyy-MM-dd"));
                            } else if (day == curDay) {
                                list.add(format(curD, "yyyy-MM-dd"));
                            }
                        }
                    }

                    if (month != -1 && month == curMonth) {
                        if (week == 0) {
                            if (day == curDay) {
                                list.add(format(curD, "yyyy-MM-dd"));
                            } else if (day == 0) {
                                list.add(format(curD, "yyyy-MM-dd"));
                            }
                        } else if (week == curWeek) {
                            if (day == 0) {
                                list.add(format(curD, "yyyy-MM-dd"));
                            } else if (day == curDay) {
                                list.add(format(curD, "yyyy-MM-dd"));
                            }
                        }
                    }
                }

                return (String[])list.toArray(new String[list.size()]);
            }
        } else {
            return new String[0];
        }
    }

    public static String getDaysBeforeTime(int daysBefore) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(5, -daysBefore);
        String time = simpleDateFormat.format(cal.getTime());
        return time;
    }

    public static final int minutesBetween(Date early, Date late) {
        Calendar ecal = Calendar.getInstance();
        Calendar lcal = Calendar.getInstance();
        ecal.setTime(early);
        lcal.setTime(late);
        long etime = ecal.getTimeInMillis();
        long ltime = lcal.getTimeInMillis();
        return (int)((ltime - etime) / 60000L);
    }

    public static Date addSeconds(Date date, int seconds) {
        return add(date, 13, seconds);
    }

    public static Date getEndDateTimeOfMonth(String monthly) {
        Calendar cal = Calendar.getInstance();
        cal.set(1, Integer.valueOf(monthly.substring(0, 4)));
        cal.set(2, Integer.valueOf(monthly.substring(5)));
        cal.set(5, cal.getActualMaximum(5));
        cal.set(11, 23);
        cal.set(12, 59);
        cal.set(13, 59);
        return cal.getTime();
    }

    public static void main(String[] args) {
        System.out.println(Arrays.deepToString(getSpecailDates("2010-11-25", "2010-12-27", 0, 2, 3)));
    }
}
