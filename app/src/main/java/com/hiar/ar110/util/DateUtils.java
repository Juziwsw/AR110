package com.hiar.ar110.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * 提供与日期有关的工具方法
 */
public class DateUtils {

    private DateUtils() {
    }

    /**
     * YYYY-MM-DD时间格式，示例：1986-04-09
     */
    public static final String YMD_FORMAT = "yyyy-MM-dd";

    /**
     * YYYY-MM-DD时间格式，示例：1986年4月9日
     */
    public static final String YMD_FORMAT2 = "yyyy年M月d日";
    public static final String YMD_FORMAT3 = "yyyy 年 M 月 d 日";

    /**
     * YYYY-MM时间格式，示例：1986-04
     */
    public static final String YM_FORMAT = "yyyy-MM";

    /**
     * MM-DD时间格式，示例：04-09
     */
    public static final String MD_FORMAT = "MM-dd";

    /**
     * MM.DD时间格式，示例：04.09
     */
    public static final String MD_FORMAT_2 = "MM.dd";

    /**
     * YYYY-MM-DD时间格式，示例：1986-04-09
     */
    public static final String HMS_FORMAT = "HH:mm:ss";

    /**
     * HH:mm时间格式，示例：18:30
     */
    public static final String HM_FORMAT = "HH:mm";

    /**
     * YYYY-MM-DD HH:mm:ss时间格式，示例：1986-04-09 18:30:29
     */
    public static final String YMD_HMS_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * YYYY-MM-DD HH:mm时间格式，示例：1986-04-09 18:30
     */
    public static final String YMD_HM_FORMAT = "yyyy-MM-dd HH:mm";

    /**
     * M月d日 示例：示例：4月9日
     */
    public static final String MD2_FORMAT = "M月d日";

    /**
     * M月d日 HH:mm时间格式，示例：4月9日 18:30
     */
    public static final String MD_HM_FORMAT = "M月d日 HH:mm";

    /**
     * M-d HH:mm时间格式，示例：4-9 18:30
     */
    public static final String MD_HM_FORMAT2 = "MM-dd HH:mm";

    /**
     * 获取相对于某天的任意一天
     *
     * @param date
     * @param delta
     * @return
     */
    public static Date dateFrom (Date date, int delta) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DATE, c.get(Calendar.DATE) + delta);
        return c.getTime();
    }

    /**
     * 相对于今天的任意一天
     *
     * @param delta
     * @return
     */
    public static Date dateFrom (int delta) {
        return dateFrom(new Date(), delta);
    }

    /**
     * 相对于今天的任意一天
     *
     * @param delta
     * @return
     */
    public static Date dateFromSecond (int delta) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.SECOND, delta);
        return c.getTime();
    }

    /**
     * Converts a Date object to its corresponding formatted string.
     *
     * @param date
     * @param format
     * @return formatted string
     */
    public static String dateToString (Date date, String format) {
        DateFormat outputDf = null;
        try {
            outputDf = new SimpleDateFormat(format);
        } catch ( Exception e ) {
            return null;
        }
        return outputDf.format(date);
    }

    /**
     * Converts a string representation of a Date to its Date object.
     *
     * @param date
     * @param format
     * @return
     */
    public static Date stringToDate (String date, String format) {
        DateFormat inputDf = null;
        try {
            inputDf = new SimpleDateFormat(format);
        } catch ( Exception e ) {
            return null;
        }

        Date result = null;

        try {
            result = inputDf.parse(date);
        } catch ( ParseException e ) {
        }
        return result;
    }

    /**
     * 获取某个日期的字符串表示
     *
     * @param delta
     * @return
     */
    public static String stringFrom (int delta, String format) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.set(Calendar.DATE, c.get(Calendar.DATE) + delta);

        DateFormat outputDf = null;

        try {
            outputDf = new SimpleDateFormat(format);
        } catch ( Exception e ) {
            return null;
        }
        return outputDf.format(c.getTime());
    }

    /**
     * 对日期的字符串表示进行重排
     *
     * @param dateString
     * @param inDf       输入日期字符串格式
     * @param outDf      输出日期字符串格式
     * @return
     */
    public static String stringToString (String dateString, String inDf,
                                         String outDf) {
        DateFormat outputDf = null;
        DateFormat inputDf = null;
        try {
            outputDf = new SimpleDateFormat(outDf);
            inputDf = new SimpleDateFormat(inDf);
        } catch ( Exception e ) {
            return null;
        }

        String result = "";

        try {
            result = outputDf.format(inputDf.parse(dateString));
        } catch ( ParseException e ) {
            System.err.println("This method should work for all date/"
                + "time strings you find in our data.");
        }
        return result;
    }

    /**
     * 获得当前日期
     *
     * @return 当前日期
     */
    public static Date getNow () {
        return new Date();
    }

    /**
     * 比较两个含开始和结束时间的先后
     *
     * @param oneStart
     * @param oneEnd
     * @param twoStart
     * @param twoEnd
     * @return 若one的时间先于two，返回-1；相等返回0；晚于返回1
     */
    public static int compare (Date oneStart, Date oneEnd, Date twoStart,
                               Date twoEnd) {
        if ( (oneStart.compareTo(twoStart) < 0 && oneStart.compareTo(twoEnd) < 0)
            || (oneStart.compareTo(twoStart) < 0 && oneEnd
            .compareTo(twoEnd) < 0)
            || (oneStart.compareTo(twoStart) < 0)
            || (oneStart.compareTo(twoStart) == 0 && oneStart
            .compareTo(twoEnd) < 0) ) {
            return - 1;
        } else if ( oneStart.compareTo(twoStart) == 0
            && oneEnd.compareTo(twoEnd) == 0 ) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * 判断两个日期是否在同一天
     *
     * @param startDate
     * @param endDate
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static boolean isInSameDay (Date startDate, Date endDate) {
        String startTime = DateUtils.dateToString(startDate,
            DateUtils.YMD_FORMAT);
        String endTime = DateUtils.dateToString(endDate, DateUtils.YMD_FORMAT);
        if ( startTime.equals(endTime) ) {
            // 两个日期在同一天
            return true;
        }
        return false;
    }

    /**
     * 两个时间之间的天数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static long getDays (String date1, String date2) {
        if ( date1 == null || date1.equals("") ) {
            return 0;
        }
        if ( date2 == null || date2.equals("") ) {
            return 0;
        }
        // 转换为标准时间
        SimpleDateFormat myFormatter = new SimpleDateFormat(YMD_FORMAT);
        Date date = null;
        Date mydate = null;
        try {
            date = myFormatter.parse(date1);
            mydate = myFormatter.parse(date2);
        } catch ( Exception e ) {
        }
        long day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
        return day;
    }

    /**
     * 判断二个日期是否在同一个周
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameWeekDates (Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        int subYear = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
        if ( 0 == subYear ) {
            if ( cal1.get(Calendar.WEEK_OF_YEAR) == cal2
                .get(Calendar.WEEK_OF_YEAR) ) {
                return true;
            }
        } else if ( 1 == subYear && 11 == cal2.get(Calendar.MONTH) ) {
            // 如果12月的最后一周横跨来年第一周的话则最后一周即算做来年的第一周
            if ( cal1.get(Calendar.WEEK_OF_YEAR) == cal2
                .get(Calendar.WEEK_OF_YEAR) ) {
                return true;
            }
        } else if ( - 1 == subYear && 11 == cal1.get(Calendar.MONTH) ) {
            if ( cal1.get(Calendar.WEEK_OF_YEAR) == cal2
                .get(Calendar.WEEK_OF_YEAR) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据一个日期，返回是星期几的字符串
     *
     * @return
     */
    public static String getWeek (String dateString) {
        Date date = stringToDate(dateString, YMD_FORMAT);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return new SimpleDateFormat("EEEE").format(c.getTime());
    }

    public static String getWeekStr (String sdate) {
        String str = "";
        str = getWeek(sdate);
        if ( "1".equals(str) ) {
            str = "星期日";
        } else if ( "2".equals(str) ) {
            str = "星期一";
        } else if ( "3".equals(str) ) {
            str = "星期二";
        } else if ( "4".equals(str) ) {
            str = "星期三";
        } else if ( "5".equals(str) ) {
            str = "星期四";
        } else if ( "6".equals(str) ) {
            str = "星期五";
        } else if ( "7".equals(str) ) {
            str = "星期六";
        }
        return str;
    }

    /**
     * 评论时间调用。
     *
     * @param time
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static String getTimeForShow (long time) {
        Calendar c = GregorianCalendar.getInstance();
        int yearNow = c.get(Calendar.YEAR);
        int weekNow = c.get(Calendar.WEEK_OF_YEAR);
        c.setTime(new Date(time));
        String str = null;
        try {
            if ( isToday(time) || time > System.currentTimeMillis() ) {
                // 今天or未来
                str = String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY),
                    c.get(Calendar.MINUTE));
            } else if ( isYestoday(time) ) {
                // 昨天
                str = "昨天";
            } else if ( weekNow == c.get(Calendar.WEEK_OF_YEAR) ) {
                // 这周
                int i = c.get(Calendar.DAY_OF_WEEK);
                if ( i == 1 ) {
                    // 星期天是一周的最后一天，跟老外的不一样
                    str = "上周日";
                } else {
                    str = "星期" + DAYS_STRING[i];
                }
            } else if ( weekNow - 1 == c.get(Calendar.WEEK_OF_YEAR) ) {
                // 上周
                int i = c.get(Calendar.DAY_OF_WEEK);
                if ( i == 1 ) {
                    // 上上周日，显示日期
                    str = String.format("%02d月%02d日",
                        c.get(Calendar.MONTH) + 1,
                        c.get(Calendar.DAY_OF_MONTH));
                } else {
                    str = "上周" + DAYS_STRING[c.get(Calendar.DAY_OF_WEEK)];
                }
            } else if ( yearNow == c.get(Calendar.YEAR) ) {
                // 今年
                str = String.format("%02d月%02d日", c.get(Calendar.MONTH) + 1,
                    c.get(Calendar.DAY_OF_MONTH));
            } else {
                // 往年
                str = String
                    .format("%04d年%02d月%02d日", c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH) + 1,
                        c.get(Calendar.DAY_OF_MONTH));
            }
        } catch ( Exception e ) {
            str = "";
        }

        return str;
    }

    /**
     * Calendar.DAY_OF_WEEK，1-7对应
     */
    private static final String[] DAYS_STRING = {"", "日", "一", "二", "三", "四",
        "五", "六"};

    public static boolean isSameDay (long a, long b) {
        long offset = TimeZone.getDefault().getRawOffset();
        long dayA = (a + offset) / 3600000 / 24;
        long dayB = (b + offset) / 3600000 / 24;
        return dayA == dayB;
    }

    public static boolean isToday (long time) {
        return isSameDay(time, System.currentTimeMillis());
    }

    public static boolean isYestoday (long time) {
        long offset = TimeZone.getDefault().getRawOffset();
        long last = (time + offset) / 3600000 / 24;
        long now = (System.currentTimeMillis() + offset) / 3600000 / 24;
        return now - last == 1;
    }

    public static String getLastRefreshTime (long time) {
        // return dateFormat.format(time);
        // 当天的只显示小时分钟
        // 一周以内的只显示周几
        // 一周以外的显示年月日啊

        Date nowDate = new Date(System.currentTimeMillis());
        String nowDateString = DateUtils.dateToString(nowDate,
            DateUtils.YMD_FORMAT);
        Date updateDate = new Date(time);
        String updateDateString = DateUtils.dateToString(updateDate,
            DateUtils.YMD_FORMAT);
        long days = DateUtils.getDays(updateDateString, nowDateString);
        if ( days < - 6 ) {
            // 超过一周的显示日期，例：5月25日
            String date = DateUtils.dateToString(updateDate,
                DateUtils.MD2_FORMAT);
            return date;
        } else if ( days >= - 6 && days < - 1 ) {
            // 一周以内的显示星期，例：星期三
            Boolean isSameWeek = DateUtils.isSameWeekDates(updateDate, nowDate);
            String week = DateUtils.getWeekStr(updateDateString);
            if ( isSameWeek ) {
                // 开始时间与今天在同一个周内
                return week;
            } else {
                String subString = week.substring(2);
                return "上周" + subString;
            }
        } else if ( days == - 1 ) {
            // 开始时间在昨天,昨天的显示为昨天，例：昨天
            return "昨天";// context.getString(R.string.live_alarm_yesterday);
        } else if ( days == 0 ) {
            // 开始时间在今天,今日的显示为具体时间，例：23：20
            return DateUtils.dateToString(updateDate, DateUtils.HM_FORMAT);
        }

        return DateUtils.dateToString(updateDate, DateUtils.MD_HM_FORMAT);

    }

    /**
     * 将获取的当前系统时间转换成Date类型再返回指定时间格式
     *
     * @param currentTime
     * @return
     */
    public static String getCurrentTime (long currentTime, String format) {
        Date date = new Date(currentTime);
        return DateUtils.dateToString(date, format);
    }

    /**
     * 获得当前日期
     *
     * @return 当前日期
     */
    public static Date getDate (String format, long time) {
        Date now = new Date(time);
        SimpleDateFormat outputDf = null;
        try {
            outputDf = new SimpleDateFormat(format);
            now = outputDf.parse(outputDf.format(now));
        } catch ( Exception e ) {
            return null;
        }
        return now;
    }
    /**
     * 获得当前日期时间
     *
     * @return 当前日期
     */
    public static String getDate (String format) {
        SimpleDateFormat outputDf = new SimpleDateFormat(format);
        return outputDf.format(new Date());
    }

    /**
     * 对日期的字符串表示进行格式化
     *
     * @param dateString
     * @param outDf      输出日期字符串格式
     * @return
     */
    public static String stringToString (String dateString, String outDf) {
        if ( dateString == null || dateString.equals("") ) {
            return null;
        }
        DateFormat outputDf = null;
        String result = null;
        try {
            outputDf = new SimpleDateFormat(outDf);
            result = outputDf.format(outputDf.parse(dateString));
        } catch ( Exception e ) {
            return null;
        }
        return result;
    }

    /**
     * 获取当天第一毫秒
     *
     * @return
     */
    public static long getTodayFirstMillSecond () {
        SimpleDateFormat format = new SimpleDateFormat(YMD_FORMAT);
        Date today = new Date(System.currentTimeMillis());
        String todayText = format.format(today);
        String todayFirstText = todayText + " 00:00:00";
        SimpleDateFormat sdf = new SimpleDateFormat(YMD_HMS_FORMAT);
        long firstMillSecond = 0;
        try {
            Date date = sdf.parse(todayFirstText);
            firstMillSecond = date.getTime();
        } catch ( ParseException e ) {
            e.printStackTrace();
        }

        return firstMillSecond;
    }

    /**
     * 计算时间差
     *
     * @param date1
     * @param date2
     * @return Minutes
     */
    public static long calculateDifference (long date1, long date2) {
        Date before = getDate(YMD_HMS_FORMAT, date1);
        Date after = getDate(YMD_HMS_FORMAT, date2);
        if ( after != null && before != null ) {
            long difference = (after.getTime() - before.getTime()) / 1000;
            return difference / 60;
        } else {
            return 60;
        }
    }

    /**
     * 返回当前时间格式的时间
     *
     * @param time
     * @param format
     * @return long
     */
    public static long getTime (String time, String format) {
        if ( time == null || time.equals("") ) {
            return 0;
        }
        try {
            SimpleDateFormat outputDf = new SimpleDateFormat(format);
            return outputDf.parse(time).getTime();
        } catch ( Exception e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取系统时间的10位的时间戳
     *
     * @return
     */
    public static int getTimeStamp () {
        return (int) (System.currentTimeMillis() / 1000);
    }

    /**
     * 获取两个时间差几天
     *
     * @return
     */
    public static int getDays (Date date1, Date date2) {
        long days = (date1.getTime() - date2.getTime()) / (1000 * 60 * 60 * 24);
        return (int) days;
    }
    public static int getHours (Date date1, Date date2) {
        long hours = (date1.getTime() - date2.getTime()) / (1000 * 60 * 60 );
        return (int) hours;
    }

    // 获得10位时间戳
    public static int getTimeStamp (Date date) {
        return (int) (date.getTime() / 1000);
    }
}