package com.syf.blognew.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author yfsun2
 * @since 2026-04-20
 */
@Data
@AllArgsConstructor
public class Gift {
    private Integer id;

    private String name;

    private String url;

    private Integer needScore;

    private Integer count;
}
