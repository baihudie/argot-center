package com.baihudie.api.msg;

import lombok.Data;

@Data
public class ServerReq {

    private Integer reqSeq;

    private Integer reqType;
    private String pseudonym;

    private String body;

}
