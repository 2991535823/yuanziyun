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

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.myapp.yuanzi.ConstString.ConstStrings;
import com.myapp.yuanzi.json.Msg;
import com.myapp.yuanzi.json.MsgItem;
import com.myapp.yuanzi.util.HttpUtil;
import com.myapp.yuanzi.util.StringUtil;
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
    public SwipeRefreshLayout refreshLayout;
    public DrawerLayout drawerLayout;
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
        refreshLayout=findViewById(R.id.swipe_Layout);
        drawerLayout=findViewById(R.id.drawer_layout);
        navBtn=findViewById(R.id.open_nav_btn);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary);
        final String  deviceNumber;
        final int orgid;
//        try {
//            deviceData=(DeviceData)getIntent().getSerializableExtra("devicedata");
//            orgid=deviceData.getDeviceOrgId();
//            deviceNumber=deviceData.getDeviceNumber();
//        }catch (NullPointerException e){
//            e.printStackTrace();
//        }


        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        String deviceInfo=sharedPreferences.getString("info",null);
//        orgid=sharedPreferences.getInt("orgid",0);
//        deviceNumber=sharedPreferences.getString("devicenumber",null);
        if (deviceInfo!=null){
            Log.d(ConstStrings.TAG, deviceInfo);
            Msg deviceMsg= Utility.handMsgResponse(deviceInfo);
            List<MsgItem> msgItemList=deviceMsg.msgItemList;//信息列表
            MsgItem deviceLastMsg=msgItemList.get(0);//最后一条信息
            deviceNumber=deviceLastMsg.deviceNumber;
            orgid=ConstStrings.ORG_ID;
            showAllMsg(deviceMsg);
            //显示数据
        }else {
            //网络请求

            deviceData=(DeviceData)getIntent().getSerializableExtra("devicedata");//意图里面获取deviceNumber
            deviceLayout.setVisibility(View.INVISIBLE);//设置页面不可见
            orgid=deviceData.getDeviceOrgId();deviceNumber=deviceData.getDeviceNumber();
            requestDeviceStatus(orgid,deviceNumber);//发起网络请求
        }
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestDeviceStatus(orgid,deviceNumber);
                Log.d(ConstStrings.TAG, "onRefresh: "+orgid+deviceNumber);
            }
        });
        navBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }
    private void showAllMsg(Msg msg){
        List<MsgItem> msgItemList=msg.msgItemList;//信息列表
        MsgItem deviceLastMsg=msgItemList.get(0);//最后一条信息
        String lastinfo=deviceLastMsg.dtuSendCloudMsg;
        Log.d(ConstStrings.TAG, "showAllMsg: "+lastinfo);
        deviceNumber.setText(deviceLastMsg.deviceNumber);
        deviceStatusHistoryLayout.removeAllViews();
        for (MsgItem item:msgItemList){
            View view= LayoutInflater.from(this).inflate(R.layout.device_history_msg_item,
                    deviceStatusHistoryLayout,false);
            TextView testmsg=view.findViewById(R.id.device_history_start_time);
            testmsg.setText(StringUtil.hexToAscii(item.dtuSendCloudMsg));
            deviceStatusHistoryLayout.addView(view);
        }
        deviceLayout.setVisibility(View.VISIBLE);

    }
    private void requestDeviceStatus(final int orgid, final String devicenumber){
        Log.d(ConstStrings.TAG, "requestDeviceStatus: "+orgid+"number:"+devicenumber);
        String url= ConstStrings.HTTP_ADDRESS+"/orgs/"+orgid+"/devicepacket/"+devicenumber+"?limit=10";
        Log.d(ConstStrings.TAG, "\nrequestDeviceStatus: "+url);
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.d(ConstStrings.TAG, "onFailure: httpRequest");
                refreshLayout.setRefreshing(false);
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
                            editor.putInt("orgid",orgid);
                            editor.putString("devicenumber",devicenumber);
                            editor.apply();
                            showAllMsg(msg);
                        }else {
                            Log.d(ConstStrings.TAG, "onResponse wrong");
                        }
                        refreshLayout.setRefreshing(false);
                    }
                });
            }
        });

    }
}
