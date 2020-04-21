package com.myapp.yuanzi;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.myapp.yuanzi.ConstString.ConstStrings;

public class MainActivity extends AppCompatActivity {
    private String deviceNumber;
    private int orgId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        deviceNumber=preferences.getString("devicenumber",null);
        orgId=preferences.getInt("orgid",0);
        if (deviceNumber!=null&&orgId!=0){
            DeviceData device =new DeviceData();
            device.setDeviceOrgId(orgId);
            device.setDeviceNumber(deviceNumber);
            device.setDeviceId(0);
            device.setDeviceGroupId(0);
            Intent intent=new Intent(this,MsgActivity.class);
            intent.putExtra("devicedata",device);
            startActivity(intent);
            finish();
        }
    }

}
