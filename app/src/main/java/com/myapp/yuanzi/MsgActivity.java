package com.myapp.yuanzi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.myapp.yuanzi.ConstString.ConstStrings;
import com.myapp.yuanzi.json.Msg;
import com.myapp.yuanzi.json.MsgItem;
import com.myapp.yuanzi.util.Utility;

import java.util.List;

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
        String deviceInfo=sharedPreferences.getString("lastInfo",null);
        if (deviceInfo!=null){
            Msg deviceMsg= Utility.handMsgResponse(deviceInfo);
            List<MsgItem> msgItemList=deviceMsg.msgItemList;//信息列表
            MsgItem deviceLastMsg=msgItemList.get(msgItemList.size()-1);//最后一条信息
            deviceNumber=deviceLastMsg.deviceNumber;
            showAllMsg(deviceMsg);
            //显示数据
        }else {
            //网络请求

            //deviceData=(DeviceData)getIntent().getSerializableExtra("devicedata");//意图里面获取deviceNumber
            deviceLayout.setVisibility(View.INVISIBLE);//设置页面不可见
            //requestDeviceStatus(deviceData.getDeviceNumber());//发起网络请求
        }
    }
    private void showAllMsg(Msg msg){
        List<MsgItem> msgItemList=msg.msgItemList;//信息列表
        MsgItem deviceLastMsg=msgItemList.get(msgItemList.size()-1);//最后一条信息
    }
    private void requestDeviceStatus(String devicenumber){
        String url= ConstStrings.HTTP_ADDRESS+"/orgs/";
    }
}
