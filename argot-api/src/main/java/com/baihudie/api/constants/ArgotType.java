package com.baihudie.api.constants;

public interface ArgotType {

    int REQ_TEST = 99;

    /////////////
    // REQ_TYPE
    /////////////

    //CONTROL RANGE:10001-19998
    int MIN_REQ_CON = 10000; //============


    int REQ_ACTIVE = 10001;


    int MAX_REQ_CON = 19999;//==============

    //MESSAGE RANGE:20000-29999
    int REQ_CHATS = 20000;
    int REQ_QUERY_ALL = 20001;
    int REQ_INVITE_APPLY = 20002;

    /////////////
    // RES_TYPE
    /////////////
    //CONTROL RANGE:50000-59999
    int RES_ACTIVE = 50001;


    //MESSAGE RANGE:60000-69999
    int RES_CHATS = 60001;
    int RES_QUERY_ALL = 60002;
    int RES_INVITE_APPLY_FROM = 60003;
    int RES_INVITE_APPLY_TO = 60004;

    int RES_ARGOT_ERROR = 69999;

}
