package com.smartCarSeatProject.utl;


import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    //获取当前完整的日期和时间
     public static String getNowDateTime() {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         return sdf.format(new Date());
     }
     //获取当前时间
    public static String getNowTime() {
         SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
         return sdf.format(new Date());
     }
     //获取当前时间（精确到毫秒）
    public static String getNowTimeDetail() {
         SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
         return sdf.format(new Date());
     }
}