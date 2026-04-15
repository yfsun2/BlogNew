package com.syf.blognew.pojo.vo;

import java.util.Date;

import lombok.Data;

/**
 * @author yfsun10
 * @version 1.0
 * @date 2021/6/11 10:25
 */
@Data
public class ChatMessage {
    private String content;
    private Date time;
    private int isMeSend;//0是对方发送 1是自己发送
    private int isRead;//是否已读（0未读 1已读），2正在发送，3发送失败
    private String userName;
    private String url;
}