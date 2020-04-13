package com.myapp.yuanzi.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Msg {
    @SerializedName("current_page")
    public int nowPage;
    @SerializedName("total_page")
    public int totalPage;
    @SerializedName("total_item")
    public int totalItem;
    @SerializedName("page_limit")
    public int pageLimit;
    @SerializedName("items")
    public List<MsgItem> msgItemList;

}
