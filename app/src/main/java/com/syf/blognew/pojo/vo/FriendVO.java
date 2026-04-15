package com.syf.blognew.pojo.vo;

import com.syf.blognew.pojo.entity.User;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendVO {
    private User user;
    private int unReadCount;
    private String lastMessage;
    private Date lastTime;
}
