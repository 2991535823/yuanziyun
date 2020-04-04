package com.myapp.yuanzi.util;

import android.util.Log;

import com.myapp.yuanzi.ConstString.ConstStrings;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtil {
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder()
                .url(address)
                .addHeader(ConstStrings.HEADER,ConstStrings.TOKEN)
                .build();
        client.newCall(request).enqueue(callback);
//        try {
//            Response response =client.newCall(request).execute();
//            Log.d("response", response.body().string());
//        }catch (Exception e){e.printStackTrace();}
    }
}
