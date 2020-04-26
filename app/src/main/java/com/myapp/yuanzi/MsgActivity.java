package com.myapp.yuanzi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.myapp.yuanzi.ConstString.ConstStrings;
import com.myapp.yuanzi.json.Msg;
import com.myapp.yuanzi.json.MsgItem;
import com.myapp.yuanzi.util.HttpUtil;
import com.myapp.yuanzi.util.LogUtil;
import com.myapp.yuanzi.util.StringUtil;
import com.myapp.yuanzi.util.Utility;
import com.myapp.yuanzi.util.WebSocketUtilManager;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okio.ByteString;

public class MsgActivity extends AppCompatActivity implements WebSocketUtilManager.WSListener {
    private ScrollView deviceLayout;
    //title控件，相关信息
    private Button navBtn;
    private TextView deviceNumberText;
    private Button subDevice;
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
    private EditText sendMsg;
    private Button sendBtn;
    public boolean subStatus=false;
    public WebSocketUtilManager webSocketUtilManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_msg);
        deviceData=(DeviceData)getIntent().getSerializableExtra("devicedata");
        deviceLayout=findViewById(R.id.device_layout);
        navBtn=findViewById(R.id.open_nav_btn);
        deviceNumberText=findViewById(R.id.device_name);
        subDevice=findViewById(R.id.sub_device_btn);
        startTimeText=findViewById(R.id.device_start_time);
        stopTimeText=findViewById(R.id.device_stop_time);
        restTimeText=findViewById(R.id.device_rest_time);
        devicePulseText=findViewById(R.id.device_pulse);
        deviceStatusHistoryLayout=findViewById(R.id.device_status_history_layout);
        refreshLayout=findViewById(R.id.swipe_Layout);
        drawerLayout=findViewById(R.id.drawer_layout);
        navBtn=findViewById(R.id.open_nav_btn);
        sendMsg=findViewById(R.id.send_msg_to_dtu);
        sendBtn=findViewById(R.id.send_msg_btn);
        sendBtn.setClickable(false);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary);

        final SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        String deviceInfo=sharedPreferences.getString("info",null);

        connectDevice(deviceData);
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
                LogUtil.d("onRefresh: "+orgid+deviceNumberString);
            }
        });
        //抽屉按钮事件
        navBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg=sendMsg.getText().toString();
                //发送消息
                if (!subStatus){
                    LogUtil.d("1");
                    showToastMsg("先点击右上角订阅设备，才能发送消息" +
                            "\ntips:长按取消订阅");
//                    if (webSocketUtilManager!=null){
//                        if (webSocketUtilManager.subDevice()){
//                            subStatus=false;
//                            webSocketUtilManager.sendMsgToDTU(msg);
//                            sendMsg.setText("");
//                        }
//                    }
                }else {
                    LogUtil.d("2");
                    if (webSocketUtilManager!=null&&!TextUtils.isEmpty(msg)){
                        webSocketUtilManager.sendMsgToDTU(msg);
                        sendMsg.setText("");
                    }
                }
            }
        });
        subDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (webSocketUtilManager!=null){
                    subStatus=webSocketUtilManager.subDevice();
                    if (subStatus){
                        showToastMsg("设备订阅成功，可以收发消息");
                    }
                }
            }
        });
        subDevice.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (webSocketUtilManager!=null){
                    subStatus=false;
                    showToastMsg("设备订阅取消");
                    return webSocketUtilManager.unSubDevice();
                }else {
                    return false;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (webSocketUtilManager!=null){
            webSocketUtilManager.close();
        }
        super.onDestroy();
    }

    private void showAllMsg(Msg msg, String deviceNumber){
        List<MsgItem> msgItemList=msg.msgItemList;//信息列表
        //判断信息
        LogUtil.e(msgItemList.size());
        String lastInfo;
        MsgItem deviceLastMsg;
        if (msgItemList.size()>0){
            deviceLastMsg=msgItemList.get(0);//DTU发往原子云的最后一条信息
            lastInfo=deviceLastMsg.dtuSendCloudMsg;//数据
            LogUtil.d("showAllMsg: "+lastInfo);//打印数据
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
                deviceHistoryRestTime.setText(item.dtuArriveCloudTime);
                LogUtil.e(item.dtuSendCloudMsg);
                deviceStatusHistoryLayout.addView(view);
            }
            deviceLayout.setVisibility(View.VISIBLE);
        }
        else {
            lastInfo="该DTU可能没有发送消息到云平台\n";
            String info="别着急，可能等会就有数据了\n\n";
            deviceLastMsg=new MsgItem();
            deviceLastMsg.deviceNumber=deviceNumber;
            deviceNumberText.setText(deviceLastMsg.deviceNumber);//显示设备编号
            deviceStatusHistoryLayout.removeAllViews();//移除之前的数据
            for (int i=0;i<10;i++){
                View view= LayoutInflater.from(this).inflate(R.layout.device_history_msg_error,
                        deviceStatusHistoryLayout,false);
                //展示错误消息控件
                TextView deviceHistoryErrorText=view.findViewById(R.id.device_history_msg_error_text);
                deviceHistoryErrorText.setText(lastInfo);
                lastInfo=info;
                deviceStatusHistoryLayout.addView(view);
            }

            deviceLayout.setVisibility(View.VISIBLE);
        }


    }
    public void requestDeviceStatus(final int orgid, final String devicenumber){
        LogUtil.d( "requestDeviceStatus: "+orgid+"number:"+devicenumber);//打印形参

        String url= ConstStrings.HTTP_ADDRESS+"/orgs/"+orgid+"/devicepacket/"+devicenumber+"?limit=10";//拼接Url limit限制为10;

        LogUtil.d("\nrequestDeviceStatus: "+url);//打印URL
        //发起网络请求
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                LogUtil.d("onFailure: httpRequest");
                refreshLayout.setRefreshing(false);
                //toast提醒
                //code
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText=response.body().string();
                LogUtil.d("onResponse: "+responseText);//打印返回的原始数据
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
                            LogUtil.d( "handMsgResponse wrong");
                        }
                        refreshLayout.setRefreshing(false);
                    }
                });
            }
        });

    }
    public void connectDevice(DeviceData deviceData){
        webSocketUtilManager=new WebSocketUtilManager();
        webSocketUtilManager.connectWebSocket(deviceData,this);
    }
    @Override
    public void onConnected() {
        sendBtn.setClickable(true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MsgActivity.this,"连接设备成功",Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    public void onFailed(final String msg) {
        LogUtil.e(msg);
        //重连
        //connectDevice(deviceData);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MsgActivity.this,"重连中:caused by:"+msg,Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onMessage() {
        LogUtil.e("msg arrive");
        requestDeviceStatus(deviceData.getDeviceOrgId(),deviceData.getDeviceNumber());
    }

    @Override
    public void onDisConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MsgActivity.this,"连接设备断开",Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showToastMsg(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
}
