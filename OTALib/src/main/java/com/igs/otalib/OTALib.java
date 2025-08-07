package com.igs.otalib;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.TimeZone;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Log;

public class OTALib {

    private static Context sContext;

    public static void init(Context mContext){
        if(sContext != null) {
            return;
        }
        sContext = mContext;
    }

    public static float GetDownloadSpeed(){
        //Log.d(TAGS, "GetDownloadSpeed");

        String Value = SystemProperties.get("rw.igs.downloadspeed");
        if(Value.equals("")){
            return 0;
        }else{
            return Float.parseFloat(Value);
        }
    }

    public static long GetDownloadByte(){
        //Log.d(TAGS, "GetDownloadByte");

        String Value = SystemProperties.get("rw.igs.downloadcount");
        if(Value.equals("")){
            return 0;
        }else{
            return Long.parseLong(Value);
        }
    }

    public static String GetOTAStatus(){
        //Log.d(TAGS, "GetOTAStatus");

        return SystemProperties.get("rw.igs.otastatus");
    }

    public static void SetOTAStatusUpdate(){
        //Log.d(TAGS, "SetOTAStatusUpdate");

        Intent mIntent = new Intent();
        mIntent.setAction("OTA_RECV_FROM_GAME");
        mIntent.putExtra("Command", 0);
        sContext.sendBroadcast(mIntent);
    }

    public static void SetOTAStatusDownload(String Json){
        //Log.d(TAGS, "SetOTAStatusDownload");

        Intent mIntent = new Intent();
        mIntent.setAction("OTA_RECV_FROM_GAME");
        mIntent.putExtra("Command", 1);
        mIntent.putExtra("DownloadInfo", Json);
        sContext.sendBroadcast(mIntent);
    }

    public static void SetOTAStatusCancel(){
        //Log.d(TAGS, "SetOTAStatusCancel");

        Intent mIntent = new Intent();
        mIntent.setAction("OTA_RECV_FROM_GAME");
        mIntent.putExtra("Command", 2);
        sContext.sendBroadcast(mIntent);
    }

    public static void SetOTASpeed(int Sleep, int Bytes){
        //Log.d(TAGS, "SetOTASpeed");

        Intent mIntent = new Intent();
        mIntent.setAction("OTA_RECV_FROM_GAME");
        mIntent.putExtra("Command", 3);
        mIntent.putExtra("PerSleepMs", Sleep);
        mIntent.putExtra("PerSleepBytes", Bytes);
        sContext.sendBroadcast(mIntent);
    }

    public static int SetDateTime(String Datetime){

        int rtn = 0;

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
            LocalDateTime localDateTime = LocalDateTime.parse(Datetime, formatter);

            Intent mIntent = new Intent();
            mIntent.setAction("Time_update");
            mIntent.putExtra("DateTime", Datetime);
            sContext.sendBroadcast(mIntent);
            rtn = 0;
        }
        catch(DateTimeParseException e){
            rtn = -1;
        }

        return rtn;
    }

    public static void SetTimeZone(String TimeZone){

        Intent mIntent = new Intent();
        mIntent.setAction("TimeZone_update");
        mIntent.putExtra("TimeZone", TimeZone);
        sContext.sendBroadcast(mIntent);
    }

    public static int NetworkCheck(){

        int rtn = 0;

        String host = "8.8.8.8";
        int timeout = 5000; // 5秒超时

        try {
            InetAddress inetAddress = InetAddress.getByName(host);
            boolean reachable = inetAddress.isReachable(timeout);

            if (reachable) {
                rtn = 0;
                Log.d(TAGS, "Host is reachable.");
            } else {
                rtn = -1;
                Log.d(TAGS, "Host is not reachable.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            rtn = -2;
        }

        return rtn;
    }

    public static int GetSystemTimeZoneOffset(){

        int offsetTotal = 0;

        TimeZone timeZone = TimeZone.getDefault();

        // 获取当前时间
        Calendar calendar = Calendar.getInstance(timeZone);

        // 获取时区的偏移量（以毫秒为单位）
        int offsetMillis = timeZone.getOffset(calendar.getTimeInMillis());

        offsetTotal = offsetMillis / (1000*60);

        return offsetTotal;
    }

    public static String GetCurrentTime(){

        LocalDateTime now = LocalDateTime.now();

        // 定义日期时间格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        // 将当前时间格式化为指定的字符串格式
        String formattedDateTime = now.format(formatter);

        return formattedDateTime;
    }

    static String TAGS = "## [KO] OTALib";
}
