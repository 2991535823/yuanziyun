package com.myapp.yuanzi.util;

import android.text.TextUtils;


import com.google.gson.Gson;
import com.myapp.yuanzi.db.Device;
import com.myapp.yuanzi.db.DeviceGroup;
import com.myapp.yuanzi.db.Orgs;
import com.myapp.yuanzi.json.Msg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
    public static boolean handleOrgsResponse(String response){
       LogUtil.d( response);
        if (!TextUtils.isEmpty(response)){
            try {
                JSONObject JsonOrgsdata=new JSONObject(response);
//                String responseStatus=JsonOrgsdata.getString("");
                String Orgsdata=JsonOrgsdata.getString("data");
                JSONArray OrgsArray=new JSONArray(Orgsdata);
                for (int i=0;i<OrgsArray.length();i++){
                    JSONObject OrgObject=OrgsArray.getJSONObject(i);
                    Orgs orgs=new Orgs();
                    orgs.setOrgCode(OrgObject.getInt("id"));//机构id
                    orgs.setOrgName(OrgObject.getString("name"));
                    orgs.setDeviceLimit(OrgObject.getInt("device_limit"));
                    orgs.setDeviceCounter(OrgObject.getInt("device_counter"));
                    orgs.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
    //处理组数据
    public static boolean handleGroupResponse(String response,int org_Id){
        LogUtil.d(response);
        if (!TextUtils.isEmpty(response)){
            try {
                JSONObject JsonGroupdata=new JSONObject(response);
                String Groupdata=JsonGroupdata.getString("data");
                JSONArray GroupArray=new JSONArray(Groupdata);
                for (int i=0;i<GroupArray.length();i++){
                    JSONObject groupObject=GroupArray.getJSONObject(i);
                    DeviceGroup group=new DeviceGroup();
                    group.setDeviceGroupId(groupObject.getInt("id"));
                    group.setDeviceGroupName(groupObject.getString("name"));
                    group.setDeviceCount(groupObject.getInt("count_device"));//原子云api问题，该数据为0
                    group.setDeviceOrgId(org_Id);
                    group.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleDeviceResponse(String response,int group_Id){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONObject JsonDevicedata=new JSONObject(response);
                String deviceData=JsonDevicedata.getString("data");
                JSONArray deviceArray=new JSONArray(deviceData);
                for (int i=0;i<deviceArray.length();i++){
                    JSONObject deviceObject=deviceArray.getJSONObject(i);
                    Device device=new Device();
                    device.setDeviceGroupId(group_Id);
                    device.setDeviceId(deviceObject.getInt("id"));
                    device.setDeviceShowId(deviceObject.getInt("show_id"));
                    device.setDeviceName(deviceObject.getString("name"));
                    device.setDeviceNumber(deviceObject.getString("number"));
                    device.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
    public static Msg handMsgResponse(String response){
        try {
            JSONObject msgDate=new JSONObject(response);
            String msgDateString=msgDate.getString("data");
            return new Gson().fromJson(msgDateString,Msg.class);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }
    public static boolean handleLoginResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONObject JsonOrgsdata=new JSONObject(response);
                int responseStatus=JsonOrgsdata.getInt("code");
                if (responseStatus==200){
                    return true;
                }

            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
}
