package com.syf.blognew.pojo.req;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author syf
 * @Date 2021/5/22 11:47
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogAddReq {
    private String model;
    private String context;
    private Integer userId;
    private List<String> images;
}
