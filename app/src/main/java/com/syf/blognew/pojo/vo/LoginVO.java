package com.syf.blognew.pojo.vo;

import com.syf.blognew.pojo.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO{
    private User user;
    private String token;
}
