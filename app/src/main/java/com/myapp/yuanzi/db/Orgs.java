package com.myapp.yuanzi.db;

import org.litepal.crud.DataSupport;

public class Orgs extends DataSupport {
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int id;


    public int getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(int orgCode) {
        this.orgCode = orgCode;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public int getDeviceLimit() {
        return deviceLimit;
    }

    public void setDeviceLimit(int deviceLimit) {
        this.deviceLimit = deviceLimit;
    }

    public int getDeviceCounter() {
        return deviceCounter;
    }

    public void setDeviceCounter(int deviceCounter) {
        this.deviceCounter = deviceCounter;
    }

    private int orgCode;
    private String orgName;
    private int deviceLimit;



    private int deviceCounter;
}
