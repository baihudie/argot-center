package com.baihudie.backend.entity;

import lombok.Data;

@Data
public class TcpEntity {

    private int flag;

    private String originPseudonym;
    private String originHost;
    private int originPort;

    private String rabblePseudonym;
    private String rabbleHost;
    private int rabblePort;


}
