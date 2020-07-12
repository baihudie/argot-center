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
    int REQ_INVITE = 20002;
    int REQ_ACCEPT = 20003;

    int MAX_REQ_MSG = 29999;//==============

    //AUTO RANGE:30000-39999
    int REQ_TCP_STEP_1 = 30000;
    int REQ_TCP_STEP_2 = 30001;
    int REQ_TCP_STEP_3 = 30002;
    int REQ_TCP_STEP_4 = 30003;
    int REQ_TCP_STEP_5 = 30004;


    /////////////
    // RES_TYPE
    /////////////

    //CONTROL RANGE:50000-59999
    int RES_ACTIVE = 50001;


    //MESSAGE RANGE:60000-69999
    int RES_CHATS = 60001;
    int RES_QUERY_ALL = 60002;
    int RES_INVITE_FROM = 60003;
    int RES_INVITE_TO = 60004;
    int RES_ACCEPT_FROM = 60005;
    int RES_ACCEPT_TO = 60006;
    int RES_TCP_STEP_1_TO = 60007;
    int RES_TCP_STEP_1_FROM = 60008;
    int RES_TCP_STEP_2_TO = 60009;
    int RES_TCP_STEP_2_FROM = 60010;
    int RES_TCP_STEP_3 = 60000;
    int RES_TCP_STEP_4 = 60010;
    int RES_TCP_STEP_5 = 60011;

    int RES_ARGOT_ERROR = 69999;

}
