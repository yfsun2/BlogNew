package com.syf.blognew.pojo.entity;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yfsun10
 * @version 1.0
 * @date 2021/5/20 14:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Blog {
    private Integer id;
    private String model;
    private String context;
    private String name;
    private Date createTime;

}
