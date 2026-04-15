package com.syf.blognew.pojo.vo;

import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * @Author syf
 * @Date 2021/6/4 22:21
 */
@Data
public class BlogVO {
    private Integer id;
    private Integer userId;
    private String model;
    private String context;
    private Date createTime;
    private String name;
    private String url;
    private byte[] pic;
    private List<CommentVO> commentList;
    private List<String> supportList;
    private Integer isSupport;
    private List<String> imageList;
}
