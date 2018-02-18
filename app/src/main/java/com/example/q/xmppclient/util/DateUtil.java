package com.example.q.xmppclient.util;

import com.example.q.xmppclient.common.Constant;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by q on 2017/11/5.
 */

public class DateUtil {
    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static String date2Str(Date d) {// yyyy-MM-dd HH:mm:ss
        return date2Str(d, null);
    }

    public static String date2Str(Date d, String format) {// yyyy-MM-dd HH:mm:ss
        if (d == null) {
            return null;
        }
        if (format == null || format.length() == 0) {
            format = FORMAT;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String s = sdf.format(d);
        return s;
    }
    public static String date2Str(Calendar c) {// yyyy-MM-dd HH:mm:ss
        return date2Str(c, null);
    }

    public static String date2Str(Calendar c, String format) {
        if (c == null) {
            return null;
        }
        if (format == null || format.length() == 0) {
            format = FORMAT;
        }
        return date2Str(c.getTime(), format);
    }

    public static Date str2Date(String str, String format) {
        if (str == null || str.length() == 0) {
            return null;
        }
        if (format == null || format.length() == 0) {
            format = FORMAT;
        }
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            date = sdf.parse(str);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;

    }




    /**
     * @param date String 想要格式化的日期
     * @return String
     */
    public static String LongDate2Short(String date) {

        try{
            SimpleDateFormat sdf1 = new SimpleDateFormat(Constant.LONGTIME_FORMART) ;        // 实例化模板对象
            SimpleDateFormat sdf2 = new SimpleDateFormat(Constant.MIDTIME_FORMART) ;        // 实例化模板对象
            Date d = null ;
            d = sdf1.parse(date) ;   // 将给定的字符串中的日期提取出来
            return sdf2.format(d);
        }catch(Exception e){            // 如果提供的字符串格式有错误，则进行异常处理
            e.printStackTrace() ;       // 打印异常信息
        }
        return "";
    }
}
