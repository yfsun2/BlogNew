package com.syf.blognew.pojo.entity;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yfsun10
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    private Integer id;
    private String name;
    private String password;
    private String email;
    private String power;
    private String url;
    private Integer score;
    private Integer consecutiveDays;
    private LocalDate lastSignDate;
}
