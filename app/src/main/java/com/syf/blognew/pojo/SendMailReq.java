package com.syf.blognew.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author syf
 * @date 2020/11/4 14:24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMailReq {
    private String email;
    private String title;
    private String context;
}
