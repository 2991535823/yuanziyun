package com.myapp.yuanzi.util;

import android.text.TextUtils;

import com.myapp.yuanzi.ConstString.ConstStrings;
import com.myapp.yuanzi.DeviceData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketUtilManager {
    public WebSocket wSocket;
    public WSListener listener;
    private DeviceData deviceData;
    public static void connectWebSocket(String url, WebSocketListener listener){
        Request request = new Request.Builder().url(url).build();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
        okHttpClient.newWebSocket(request,listener);
    }
    public  void connectWebSocket(DeviceData device, final WSListener listener){
        this.listener=listener;
        this.deviceData=device;
        String url= ConstStrings.WS_ADDRESS+ConstStrings.TOKEN+"/org/"+device.getDeviceOrgId()
                +"?token="+ UUID.randomUUID().toString();
        Request request = new Request.Builder().url(url).build();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
        okHttpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                if (listener!=null){
                    listener.onDisConnected();
                    LogUtil.d("ws closed");
                }
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                if (listener!=null&& !TextUtils.isEmpty(t.getMessage())){
                    listener.onFailed(t.getMessage());
                    LogUtil.d("ws fail");

                }
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                super.onMessage(webSocket, text);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                if (listener != null) {
                    byte[] byteArray = bytes.toByteArray();
                    if (byteArray.length > 0) {
                        switch (byteArray[0]) {
                            case 0x04:
                                listener.onConnected();
                                break;
                            case 0x05:
                                listener.onDisConnected();
                                break;
                            case 0x06:
                            case 0x08:
                                //
                                listener.onMessage();
                                break;
                        }
                    }
                }
            }

            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                wSocket=webSocket;
                if (listener!=null){
                    listener.onConnected();
                }
            }
        });

    }

    public boolean subDevice(){
        byte[] head = {0x01};
        byte[] dataByte = concat(head, deviceData.getDeviceNumber().getBytes());
        for (byte b:dataByte){
            LogUtil.e(b);
        }
        return wSocket.send(ByteString.of(dataByte));
    }
    public boolean unSubDevice(){
        byte[] head = {0x02};
        byte[] dataByte = concat(head, deviceData.getDeviceNumber().getBytes());
        for (byte b:dataByte){
            LogUtil.e(b);
        }
        return wSocket.send(ByteString.of(dataByte));
    }

    public boolean sendMsgToDTU(String msg) {
        String add="\r\n";
        msg=msg+add;
        byte[] head = concat(new byte[]{0x03},deviceData.getDeviceNumber().getBytes());

        return wSocket.send(ByteString.of(concat(head, msg.getBytes())));
    }

    public void close() {
        if (wSocket != null) {
            wSocket.cancel();
        }
    }
    static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
    public interface WSListener{
        void onConnected();
        void onFailed(String msg);
        void onMessage();
        void onDisConnected();
    }
}
