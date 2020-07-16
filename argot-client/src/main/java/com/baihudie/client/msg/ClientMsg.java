package com.baihudie.client.msg;

import lombok.Data;

@Data
public class ClientMsg {

    private int seq;

    private int msgType;

    private String body;

}
