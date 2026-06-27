package com.syf.blognew.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageVO {
    private Integer id;
    private Integer fromUid;
    private Integer toUid;
    private String content;
    private LocalDateTime createTime;
    //1为以读，0为未读
    private int isRead;
    private int msgType;
    private int isReceive;
    private int isWithdraw;
    private String userName;
    private String url;

    private int isMeSend;
}
