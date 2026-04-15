package com.syf.blognew.pojo.entity;

import lombok.Data;

//Chat 相当于是Message和Friend 的封装类<一个朋友+最后一条信息>，专门用于列表渲染的
@Data
public class Chat {

    private int friendId;
    private String friendName;
    private String friendImage;
    private String lastMessage;
    private boolean isRead;
    private String time;
}
