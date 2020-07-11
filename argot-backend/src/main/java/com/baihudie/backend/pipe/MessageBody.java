package com.baihudie.backend.pipe;

import lombok.Data;

@Data
public class MessageBody {

    //收消息的人
    private String pseudonym;

    //收消息类型
    private int resType;

    //收消息体
    private String body;

}
