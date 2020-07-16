package com.baihudie.api.msg;

import lombok.Data;

@Data
public class ServerRes {

    private Integer resSeq;

    private Integer resCode;
    private String resMsg;

    private Integer resType;
    private String body;

}
