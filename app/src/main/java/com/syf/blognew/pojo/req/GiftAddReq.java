package com.syf.blognew.pojo.req;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GiftAddReq {
    private String name;

    private String url;

    private Integer count;

    private Integer needScore;
}
