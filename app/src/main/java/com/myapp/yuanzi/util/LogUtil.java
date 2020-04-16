package com.myapp.yuanzi.util;

import android.util.Log;

public class LogUtil {
    private static final int DEBUG = 1;
    private static final int INFO = 2;
    private static final int WARN= 3;
    private static final int ERROR = 4;
    private static final int NOTHING = 5;
    public static int LEVEL=DEBUG;
    public static void d(String TAG,String msg){
        if (LEVEL<=DEBUG){
            Log.d(TAG, msg);
        }
    }
    public static void i(String TAG,String msg){
        if (LEVEL<=INFO){
            Log.i(TAG, msg);
        }
    }
    public static void w(String TAG,String msg){
        if (LEVEL<=WARN){
            Log.w(TAG, msg);
        }
    }
    public static void e(String TAG,String msg){
        if (LEVEL<=ERROR){
            Log.e(TAG, msg);
        }
    }
}
