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
    private TextView deviceNumberText;
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
    private String  deviceNumberString;
    private int orgid;
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
        deviceNumberText=findViewById(R.id.device_name);
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

        final SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        String deviceInfo=sharedPreferences.getString("info",null);
        deviceData=(DeviceData)getIntent().getSerializableExtra("devicedata");
        deviceLayout.setVisibility(View.INVISIBLE);//设置页面不可见
        orgid=deviceData.getDeviceOrgId();deviceNumberString=deviceData.getDeviceNumber();
        requestDeviceStatus(orgid,deviceNumberString);//发起网络请求
        //下拉数据刷新事件
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                orgid=sharedPreferences.getInt("orgid",0);
                deviceNumberString=sharedPreferences.getString("devicenumber",null);
                requestDeviceStatus(orgid,deviceNumberString);
                Log.d(ConstStrings.TAG, "onRefresh: "+orgid+deviceNumberString);
            }
        });
        //抽屉按钮事件
        navBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }
    private void showAllMsg(Msg msg,String deviceNumber){
        List<MsgItem> msgItemList=msg.msgItemList;//信息列表
        //判断信息
        String lastInfo;
        MsgItem deviceLastMsg;
        if (msgItemList.size()>0){
            deviceLastMsg=msgItemList.get(0);//DTU发往原子云的最后一条信息
            lastInfo=deviceLastMsg.dtuSendCloudMsg;//或者数据
            Log.d(ConstStrings.TAG, "showAllMsg: "+lastInfo);//打印数据
            deviceNumberText.setText(deviceLastMsg.deviceNumber);//显示设备编号
            //lastInfo数据解析，展示


            deviceStatusHistoryLayout.removeAllViews();//移除之前的数据
            for (MsgItem item:msgItemList){
                View view= LayoutInflater.from(this).inflate(R.layout.device_history_msg_item,
                        deviceStatusHistoryLayout,false);
                //展示历史消息的4个控件
                TextView deviceHistoryStartTime=view.findViewById(R.id.device_history_start_time);
                TextView deviceHistoryStopTime=view.findViewById(R.id.device_history_stop_time);
                TextView deviceHistoryRestTime=view.findViewById(R.id.device_history_rest_time);
                TextView deviceHistoryPulse=view.findViewById(R.id.device_history_pulse);
                deviceHistoryStartTime.setText(StringUtil.hexToAscii(item.dtuSendCloudMsg));

                deviceStatusHistoryLayout.addView(view);
            }
            deviceLayout.setVisibility(View.VISIBLE);
        }
        else {
            lastInfo="该DTU可能没有发送消息到云平台";
            deviceLastMsg=new MsgItem();
            deviceLastMsg.deviceNumber=deviceNumber;
            deviceNumberText.setText(deviceLastMsg.deviceNumber);//显示设备编号
            deviceStatusHistoryLayout.removeAllViews();//移除之前的数据
            View view= LayoutInflater.from(this).inflate(R.layout.device_history_msg_error,
                        deviceStatusHistoryLayout,false);
                //展示错误消息控件
                TextView deviceHistoryErrorText=view.findViewById(R.id.device_history_msg_error_text);
                deviceHistoryErrorText.setText(lastInfo);
                deviceStatusHistoryLayout.addView(view);
            deviceLayout.setVisibility(View.VISIBLE);
        }


    }
    public void requestDeviceStatus(final int orgid, final String devicenumber){
        Log.d(ConstStrings.TAG, "requestDeviceStatus: "+orgid+"number:"+devicenumber);//打印形参

        String url= ConstStrings.HTTP_ADDRESS+"/orgs/"+orgid+"/devicepacket/"+devicenumber+"?limit=10";//拼接Url limit限制为10;

        Log.d(ConstStrings.TAG, "\nrequestDeviceStatus: "+url);//打印URL
        //发起网络请求
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.d(ConstStrings.TAG, "onFailure: httpRequest");
                refreshLayout.setRefreshing(false);
                //toast提醒
                //code
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText=response.body().string();
                Log.d(ConstStrings.TAG, "onResponse: "+responseText);//打印返回的原始数据
                final Msg msg=Utility.handMsgResponse(responseText);//数据解析为msg (json实体类)
                //主线程Ui操作
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (msg!=null){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(MsgActivity.this).edit();
                            //把三个数据存储
                            editor.putString("info",responseText);
                            editor.putInt("orgid",orgid);
                            editor.putString("devicenumber",devicenumber);
                            editor.apply();
                            //展示数据
                            showAllMsg(msg,devicenumber);
                        }else {
                            Log.d(ConstStrings.TAG, "handMsgResponse wrong");
                        }
                        refreshLayout.setRefreshing(false);
                    }
                });
            }
        });

    }
}
