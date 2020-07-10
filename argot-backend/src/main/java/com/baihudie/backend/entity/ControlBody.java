package com.baihudie.backend.entity;

import lombok.Data;

@Data
public class ControlBody {

    public static final int CON_ACTIVE = 0;

    private String pseudonym;

    private int controlType;
    private String controlMsg;

    private String body;

}
