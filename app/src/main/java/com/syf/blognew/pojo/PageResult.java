package com.syf.blognew.pojo;

import com.syf.blognew.pojo.vo.BlogVO;

import lombok.Data;

import java.util.List;

/**
 * @author syf
 * @date 2021/5/27 22:17
 */

@Data
public class PageResult<T> {
    private int current;
    private int size;
    private int total;
    private List<T> records;
}
