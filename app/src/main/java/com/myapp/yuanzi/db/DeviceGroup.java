package com.myapp.yuanzi.db;

import org.litepal.crud.DataSupport;

public class DeviceGroup extends DataSupport {
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    private int id;

    public int getDeviceOrgId() {
        return deviceOrgId;
    }

    public void setDeviceOrgId(int deviceOrgId) {
        this.deviceOrgId = deviceOrgId;
    }

    public int getDeviceGroupId() {
        return deviceGroupId;
    }

    public void setDeviceGroupId(int deviceGroupId) {
        this.deviceGroupId = deviceGroupId;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    public String getDeviceGroupName() {
        return deviceGroupName;
    }

    public void setDeviceGroupName(String deviceGroupName) {
        this.deviceGroupName = deviceGroupName;
    }

    private int deviceOrgId;
    private int deviceGroupId;
    private int deviceCount;
    private String deviceGroupName;
}
