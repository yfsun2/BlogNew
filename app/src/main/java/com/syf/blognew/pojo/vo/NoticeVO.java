package com.syf.blognew.pojo.vo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoticeVO {
    // 通知类型
    public static final int TYPE_EXCHANGE_GIFT = 1;  // 兑换礼物
    public static final int TYPE_TRANSFER_OUT  = 2;  // 主动转账
    public static final int TYPE_TRANSFER_IN   = 3;  // 接收转账
    public static final int TYPE_ADD_FRIEND   = 4;  // 好友申请
    public static final int TYPE_SYSTEM        = 5;  // 系统通知

    private Integer id;
    private int type;
    private String title;
    private String content;
    private String userName;
    private LocalDateTime time;
    private int isRead; // 1已读 / 0未读

    // 兑换礼物专用状态：0待发货 1已发货 2已完成，3拒绝
    private int goodsStatus;
    // 好友专用状态：0未同意 1已同意，2已拒绝
    private int friendStatus;
}