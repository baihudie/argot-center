package com.baihudie.backend.pipe;

import lombok.Data;

import java.util.List;

@Data
public class ControlBody{

    //收消息的人
    private String pseudonym;

    //收消息类型
    private int resType;

    //收消息体
    private String body;


}
