package com.myapp.yuanzi.util;

import android.util.Log;

public class LogUtil {
    private static final int DEBUG = 1;
    private static final int INFO = 2;
    private static final int WARN= 3;
    private static final int ERROR = 4;
    private static final int NOTHING = 5;
    public static int LEVEL=DEBUG;
    private static String TAG="G_BUG";
    private final static int LOG_LENGTH = 2000;
    public static void d(String msg){
        if (LEVEL<=DEBUG){
            Log.d(TAG, msg);
        }
    }
    public static void i(String msg){
        if (LEVEL<=INFO){
            Log.i(TAG, msg);
        }
    }
    public static void w(String msg){
        if (LEVEL<=WARN){
            Log.w(TAG, msg);
        }
    }
    public static void e(String msg){
        if (LEVEL<=ERROR){
            Log.e(TAG, msg);
        }
    }
    public static void e(Object object) {
        if (LEVEL<ERROR){
            if (object != null) {
                String log = object.toString();
                int length = log.length();
                for (int i = 0; i < length; i += LOG_LENGTH) {
                    if (i + LOG_LENGTH < length) {
                        Log.e(TAG, log.substring(i, i + LOG_LENGTH));
                    } else {
                        Log.e(TAG, log.substring(i, length));
                    }
                }
            }
        }

    }
}
