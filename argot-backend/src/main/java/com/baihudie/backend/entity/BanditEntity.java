package com.baihudie.backend.entity;

import lombok.Data;

@Data
public class BanditEntity {

    //唯一标识
    private String banditCode;

    //临时身份
    private String pseudonym;

    //名字
    private String goodName;


}
