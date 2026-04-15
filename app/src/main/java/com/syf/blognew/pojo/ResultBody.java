package com.syf.blognew.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author syf
 * @date 2020/11/6 22:52
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultBody<T> {
    private int code;
    private String msg;
    private T data;
}
