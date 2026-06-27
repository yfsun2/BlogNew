package com.syf.blognew.pojo.req;

import lombok.Data;

@Data
public class TransferReq {
    private String password;
    private Integer toId;
    private Integer score;
}
