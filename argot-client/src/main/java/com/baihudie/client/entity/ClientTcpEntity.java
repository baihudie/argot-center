package com.baihudie.client.entity;

import com.baihudie.api.constants.ArgotType;
import lombok.Data;

@Data
public class ClientTcpEntity {

    private int argotType;//TCP3还是4

    private String token;

    private int aLocalPort;

    //end - a
    private String aPseudonym;
    private String aBanditCode;

    private String aRemoteIp;
    private int aRemotePort;


    //end - b
    private String bPseudonym;
    private String bBanditCode;

    private String bRemoteIp;
    private int bRemotePort;


}
