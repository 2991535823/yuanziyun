package com.myapp.yuanzi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.myapp.yuanzi.ConstString.ConstStrings;
import com.myapp.yuanzi.json.Msg;
import com.myapp.yuanzi.json.MsgItem;
import com.myapp.yuanzi.util.HttpUtil;
import com.myapp.yuanzi.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MsgActivity extends AppCompatActivity {
    private ScrollView deviceLayout;
    //title控件，相关信息
    private Button navBtn;
    private TextView deviceNumber;
    private TextView msgPageInfo;
    //device_status_now控件，机器相应工作状态
    private TextView startTimeText;
    private TextView stopTimeText;
    private TextView restTimeText;
    private TextView devicePulseText;
    //device_status控件,历史消息
    private LinearLayout deviceStatusHistoryLayout;
    private DeviceData deviceData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_msg);
        deviceLayout=findViewById(R.id.device_layout);
        navBtn=findViewById(R.id.open_nav_btn);
        deviceNumber=findViewById(R.id.device_name);
        msgPageInfo=findViewById(R.id.page_msg);
        startTimeText=findViewById(R.id.device_start_time);
        stopTimeText=findViewById(R.id.device_stop_time);
        restTimeText=findViewById(R.id.device_rest_time);
        devicePulseText=findViewById(R.id.device_pulse);
        deviceStatusHistoryLayout=findViewById(R.id.device_status_history_layout);
        final String  deviceNumber;
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        String deviceInfo=sharedPreferences.getString("info",null);

        if (deviceInfo!=null){
            Log.d(ConstStrings.TAG, deviceInfo);
            Msg deviceMsg= Utility.handMsgResponse(deviceInfo);
            List<MsgItem> msgItemList=deviceMsg.msgItemList;//信息列表
            MsgItem deviceLastMsg=msgItemList.get(msgItemList.size()-1);//最后一条信息
            deviceNumber=deviceLastMsg.deviceNumber;
            showAllMsg(deviceMsg);
            //显示数据
        }else {
            //网络请求

            deviceData=(DeviceData)getIntent().getSerializableExtra("devicedata");//意图里面获取deviceNumber
            deviceLayout.setVisibility(View.INVISIBLE);//设置页面不可见
            requestDeviceStatus(deviceData.getDeviceOrgId(),deviceData.getDeviceNumber());//发起网络请求
        }
    }
    private void showAllMsg(Msg msg){
        List<MsgItem> msgItemList=msg.msgItemList;//信息列表
        MsgItem deviceLastMsg=msgItemList.get(msgItemList.size()-1);//最后一条信息
        String lastinfo=deviceLastMsg.dtuSendCloudMsg;
        Log.d(ConstStrings.TAG, "showAllMsg: "+lastinfo);
        for (MsgItem item:msgItemList){
            View view= LayoutInflater.from(this).inflate(R.layout.device_history_msg_item,
                    deviceStatusHistoryLayout,false);
            TextView testmsg=view.findViewById(R.id.device_history_start_time);
            testmsg.setText(item.dtuSendCloudMsg);
            deviceStatusHistoryLayout.addView(view);
        }
        deviceLayout.setVisibility(View.VISIBLE);

    }
    private void requestDeviceStatus(int orgid,String devicenumber){
        Log.d(ConstStrings.TAG, "requestDeviceStatus: "+orgid+"number:"+devicenumber);
        String url= ConstStrings.HTTP_ADDRESS+"/orgs/"+orgid+"/devicepacket/"+devicenumber+"?limit=10";
        Log.d(ConstStrings.TAG, "\nrequestDeviceStatus: "+url);
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.d(ConstStrings.TAG, "onFailure: httpRequest");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText=response.body().string();
                Log.d(ConstStrings.TAG, "onResponse: "+responseText);
                final Msg msg=Utility.handMsgResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (msg!=null){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(MsgActivity.this).edit();
                            editor.putString("info",responseText);
                            editor.apply();
                            showAllMsg(msg);
                        }else {
                            Log.d(ConstStrings.TAG, "onResponse wrong");
                        }
                    }
                });
            }
        });

    }
}
