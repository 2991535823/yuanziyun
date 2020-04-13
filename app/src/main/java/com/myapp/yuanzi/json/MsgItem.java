package com.myapp.yuanzi.json;

import com.google.gson.annotations.SerializedName;

public class MsgItem {
    @SerializedName("time")
    public String dtuArriveCloudTime;
    @SerializedName("hex_packet")
    public String dtuSendCloudMsg;
    @SerializedName("length")
    public int msgLength;
    @SerializedName("number")
    public String deviceNumber;
}
