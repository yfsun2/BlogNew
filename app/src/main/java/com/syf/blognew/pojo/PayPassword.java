package com.syf.blognew.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayPassword {
    private String oldPwd;
    private String newPwd;
}
