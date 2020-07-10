package com.baihudie.backend.entity;

import lombok.Data;

import java.util.List;

@Data
public class MessageBody {

    public static final int TO_NULL = 0;
    public static final int TO_SELF = 1;
    public static final int TO_ONE = 2;
    public static final int TO_LIST = 3;
    public static final int TO_ALL = 4;

    private int sendTo;

    private String pseudonymsOne;

    private List<String> pseudonymsList;

    private int resType;

    private String body;

}
