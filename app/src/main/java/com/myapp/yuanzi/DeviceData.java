package com.myapp.yuanzi;

import java.io.Serializable;

public class DeviceData implements Serializable {
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

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    private int deviceOrgId;//机构id
    private int deviceGroupId;//组id
    private int deviceId;//设备id，设备连接状态接口调用
    private String deviceNumber;//设备编号,设备历史消息接口调用

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    private String deviceName;
}
