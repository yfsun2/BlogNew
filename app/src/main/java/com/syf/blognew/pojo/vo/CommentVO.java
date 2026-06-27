package com.syf.blognew.pojo.vo;

import java.time.LocalDateTime;
import java.util.Date;

import lombok.Data;

@Data
public class CommentVO {

    private String fromUser;
    private Integer fromUid;
    private String toUser;
    private Integer toUid;
    private String content;
    private LocalDateTime createTime;
}
