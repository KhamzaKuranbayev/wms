package uz.uzcard.genesis.dto;

import java.util.Date;

/**
 * Created by Virus on 03-Sep-16.
 */
public class JsonDateUtils {

    public static String setDate(Date date) {
        if (date == null) return null;
        Integer offset = date.getTimezoneOffset() * 60 * 1000;
        date = new Date(date.getTime() + offset);
        return (1900 + date.getYear()) + "-" + (date.getMonth() + 1) + "-" + date.getDate()
                + " " + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
    }

    public static Date convertDefaultTimeZone(Date date) {
        if (date == null) return null;
        Integer offset = date.getTimezoneOffset() * 60 * 1000;
        return new Date(date.getTime() + offset);
    }

    public static String setNonConvertableDate(Date date) {
        if (date == null) return null;
        return (1900 + date.getYear()) + "-" + (date.getMonth() + 1) + "-" + date.getDate()
                + "  " + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
    }

    public static Date getDate(String date) {
        if (date == null || date.isEmpty()) {
            return null;
        }
        String[] dateTime = date.split(" ");
        String[] yearMonthDate = dateTime[0].split("-");
        int year = Integer.valueOf(yearMonthDate[0]);
        int month = Integer.valueOf(yearMonthDate[1]);
        int day = Integer.valueOf(yearMonthDate[2]);

        int hour = 0, minute = 0, second = 0;
        if (dateTime.length > 1) {
            String[] hourMinuteSecond = dateTime[1].split(":");
            hour = Integer.valueOf(hourMinuteSecond[0]);
            minute = Integer.valueOf(hourMinuteSecond[1]);
            second = Integer.valueOf(hourMinuteSecond[2]);
        }
        Date date2 = new Date(year - 1900, month - 1, day, hour, minute, second);
        return new Date(date2.getTime() - date2.getTimezoneOffset() * 60 * 1000);
    }

    public static Date getNonConvertableDate(String date) {
        if (date == null || date.isEmpty()) {
            return null;
        }
        String[] dateTime = date.split(" ");
        String[] yearMonthDate = dateTime[0].split("-");
        String[] hourMinuteSecond = dateTime[1].split(":");
        int year = Integer.valueOf(yearMonthDate[0]);
        int month = Integer.valueOf(yearMonthDate[1]);
        int day = Integer.valueOf(yearMonthDate[2]);
        int hour = Integer.valueOf(hourMinuteSecond[0]);
        int minute = Integer.valueOf(hourMinuteSecond[1]);
        int second = Integer.valueOf(hourMinuteSecond[2]);
        return new Date(year - 1900, month - 1, day, hour, minute, second);
    }

}