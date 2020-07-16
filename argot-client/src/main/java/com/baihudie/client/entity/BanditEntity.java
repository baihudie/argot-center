package com.baihudie.client.entity;

import lombok.Data;

@Data
public class BanditEntity {

    //唯一标识
    private String banditCode;

    //临时身份
    private String pseudonym;

    //名字
    private String goodName;

//    // ip
//    private String hostIp;
//
//    // port
//    private Integer hostPort;
//
//    //状态
//    private String status;

}
