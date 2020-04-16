package com.myapp.yuanzi.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.myapp.yuanzi.ConstString.ConstStrings;
import com.myapp.yuanzi.DeviceData;
import com.myapp.yuanzi.MainActivity;
import com.myapp.yuanzi.MsgActivity;
import com.myapp.yuanzi.R;
import com.myapp.yuanzi.db.Device;
import com.myapp.yuanzi.db.DeviceGroup;
import com.myapp.yuanzi.db.Orgs;
import com.myapp.yuanzi.util.HttpUtil;
import com.myapp.yuanzi.util.LogUtil;
import com.myapp.yuanzi.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseDeviceFragment extends Fragment {
    private static final int LEVEL_ORGS = 0;
    private static final int LEVEL_GROUP = 1;
    private static final int LEVEL_DEVICE = 2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button back_Button;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList<>();
    private List<Orgs> orgsList;
    private List<DeviceGroup> groupList;
    private List<Device> deviceList;
    private Orgs selectedOrg;
    private DeviceGroup selectedGroup;
    private Device selectedDevice;
    private int selectedLevel;
    private DeviceData device=new DeviceData();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_device,container,false);
        titleText=view.findViewById(R.id.title_text);
        back_Button=view.findViewById(R.id.back_button);
        listView=view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        if(getContext() instanceof MsgActivity) {
            //获取状态栏高度
            int statusBarHeight = 0;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0)
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            view.findViewById(R.id.choose_device_fragment).setPadding(0, statusBarHeight, 0, 0);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (selectedLevel==LEVEL_ORGS){
                    selectedOrg=orgsList.get(i);//获取org对象
                    LogUtil.d(ConstStrings.TAG, "onItemClick: "+selectedOrg.getOrgCode());
                    device.setDeviceOrgId(selectedOrg.getOrgCode());
                    ConstStrings.ORG_ID=selectedOrg.getOrgCode();
                    //查询组
                    queryGroup();
                }else if (selectedLevel==LEVEL_GROUP){
                    selectedGroup=groupList.get(i);
                    device.setDeviceGroupId(selectedGroup.getDeviceGroupId());
                    queryDevice();
                }else if(selectedLevel==LEVEL_DEVICE){
                    selectedDevice=deviceList.get(i);//获取当前选择的device
                    //给device对象添加相应属性
                    device.setDeviceId(selectedDevice.getDeviceId());
                    device.setDeviceNumber(selectedDevice.getDeviceNumber());
                    LogUtil.d(ConstStrings.TAG, "onItemClick: "+device.getDeviceOrgId()+" number:"+device.getDeviceNumber());
                    //启动设备界面
                    if (getActivity() instanceof MainActivity){
                        Intent intent=new Intent(getActivity(), MsgActivity.class);
                        intent.putExtra("devicedata",device);
                        startActivity(intent);
                        getActivity().finish();
                    }
                    if (getActivity() instanceof MsgActivity){
                        MsgActivity msgActivity=(MsgActivity)getActivity();
                        msgActivity.drawerLayout.closeDrawers();
                        msgActivity.refreshLayout.setRefreshing(true);
                        msgActivity.requestDeviceStatus(device.getDeviceOrgId(),device.getDeviceNumber());
                    }

                }
            }
        });
        back_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedLevel==LEVEL_GROUP){
                    queryOrgs();
                }else if(selectedLevel==LEVEL_DEVICE){
                    queryGroup();
                }

            }
        });

        //
        queryOrgs();
    }
    private void queryOrgs(){
        titleText.setText(ConstStrings.TITLE_MSG);
        back_Button.setVisibility(View.GONE);
        orgsList= DataSupport.findAll(Orgs.class);
        if (orgsList.size()>0){
            //本地查询
            dataList.clear();
            for (Orgs orgs:orgsList){
                dataList.add("机构名称:"+orgs.getOrgName()+"\n机构ID:"+orgs.getOrgCode()+
                        "\n设备数:"+orgs.getDeviceCounter());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            selectedLevel=LEVEL_ORGS;
            //更新数据
            DataSupport.deleteAll(Orgs.class);
        }else {
            //服务器查询
            String address=ConstStrings.HTTP_ADDRESS+"/orgs";
            queryFromServer(address,"orgs");

        }
    }
    private void queryGroup(){
        titleText.setText(selectedOrg.getOrgName());
        back_Button.setVisibility(View.VISIBLE);
        groupList=DataSupport.where("deviceorgid = ?",String.valueOf(selectedOrg.getOrgCode())).find(DeviceGroup.class);
        if (groupList.size()>0){
            dataList.clear();
            for (DeviceGroup group:groupList){
                //显示数据
                dataList.add("组名:"+group.getDeviceGroupName()+"\n组ID:"+group.getDeviceGroupId()
                +"\n设备数(暂时有误):"+group.getDeviceCount());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            selectedLevel=LEVEL_GROUP;
            DataSupport.deleteAll(DeviceGroup.class);
        }else {
            String address=ConstStrings.HTTP_ADDRESS+"/orgs/"+selectedOrg.getOrgCode()+"/grouplist";
            queryFromServer(address,"group");
        }

    }
    private void queryDevice(){
        titleText.setText(selectedGroup.getDeviceGroupName());
        back_Button.setVisibility(View.VISIBLE);
        deviceList=DataSupport.where("devicegroupid = ?",String.valueOf(selectedGroup.getDeviceGroupId())).find(Device.class);
        if (deviceList.size()>0){
            dataList.clear();
            for (Device device:deviceList){
                dataList.add("\n设备名:"+device.getDeviceName()+"\n设备id:"+device.getDeviceId()+"\n设备编号:"+
                        device.getDeviceNumber());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            selectedLevel=LEVEL_DEVICE;
            DataSupport.deleteAll(Device.class);
        }else {
            String address=ConstStrings.HTTP_ADDRESS+"/orgs/"+selectedOrg.getOrgCode()+"/groups/"+selectedGroup.getDeviceGroupId()+"/devices";
            queryFromServer(address,"device");
        }
    }
    private void queryFromServer(String address,final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        LogUtil.d("queryFromServer", "onFailure: ");
                    }
                });

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText=response.body().string();
                LogUtil.d("queryFromServer1", responseText);
                boolean result=false;
                if ("orgs".equals(type)){
                    result= Utility.handleOrgsResponse(responseText);
                }
                else if("group".equals(type)){
                    result=Utility.handleGroupResponse(responseText,selectedOrg.getOrgCode());
                }else if("device".equals(type)){
                    result=Utility.handleDeviceResponse(responseText,selectedGroup.getDeviceGroupId());
                }
                if (result){
                    LogUtil.d("queryFromServer", result+"");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("orgs".equals(type)){
                                queryOrgs();
                            }
                            else if("group".equals(type)){
                                queryGroup();
                            }else if("device".equals(type)){
                                queryDevice();
                            }
                        }
                    });
                }
            }
        });
    }
    private void showProgressDialog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage(ConstStrings.PROGRESS_MSG);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    private void closeProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }

}
