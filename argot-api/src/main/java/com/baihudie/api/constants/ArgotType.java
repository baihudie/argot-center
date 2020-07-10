package com.baihudie.api.constants;

public interface ArgotType {

    int REQ_TEST = 99;

    /////////////
    // REQ_TYPE
    /////////////


    //CONTROL RANGE:10000-19999
    int REQ_ACTIVE = 10000;

    int MAX_REQ_CON = 19999;

    //MESSAGE RANGE:20000-29999
    int REQ_CHAT_ALL = 20000;


    /////////////
    // RES_TYPE
    /////////////
    //CONTROL RANGE:50000-59999
    int RES_ACTIVE = 50000;

    int MAX_RES_CON = 59999;


    //MESSAGE RANGE:60000-69999
    int RES_CHAT_ALL = 70000;

}
