package com.myapp.yuanzi.db;

import org.litepal.crud.DataSupport;

public class Device extends DataSupport {
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getDeviceShowId() {
        return deviceShowId;
    }

    public void setDeviceShowId(int deviceShowId) {
        this.deviceShowId = deviceShowId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    private int id;
    private int deviceGroupId;
    private int deviceId;
    private int deviceShowId;
    private String deviceName;
    private String deviceNumber;

}
