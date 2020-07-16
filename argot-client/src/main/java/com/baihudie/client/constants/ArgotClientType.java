package com.baihudie.client.constants;

public interface ArgotClientType {

    int REQ_TEST = 98;
    int RES_TEST = 99;
    int RES_ARGOT_ERROR = 99999;

    /////////////
    // REQ_TYPE
    /////////////

    //============
    int REQ_CON_MIN = 9999;

    /**
     * CONTROL RANGE:10000-19998
     */

    int REQ_ACTIVE = 10000;

    //==============
    int REQ_CON_MAX = 19999;

    /**
     * MESSAGE RANGE:20000-29999
     */

    int REQ_CHATS = 20000;
    int REQ_CHAT = 20001;
    int REQ_QUERY_ALL = 20002;
    int REQ_INVITE = 20003;
    int REQ_ACCEPT = 20004;
    int REQ_TCP_STEP_1 = 20005;
    int REQ_TCP_STEP_2 = 20006;

    //==============
    int REQ_MSG_MAX = 29999;

    /**
     * AUTO RANGE:30000-39999
     */

    int REQ_TCP_STEP_3 = 30007;
    int REQ_TCP_STEP_4 = 30008;

    /**
     * CLIENT RANGE:40000-49999
     */

    int REQ_CLIENT_CHAT = 40000;

    /////////////
    // RES_TYPE
    /////////////

    /**
     * CONTROL RANGE:5000-59999
     */

    int RES_ACTIVE = 50000;


    /**
     * MESSAGE RANGE:60000-69999
     */

    int RES_CHATS = 60001;
    int RES_CHAT_FROM = 60002;
    int RES_CHAT_TO = 60003;
    int RES_QUERY_ALL = 60004;
    int RES_INVITE_FROM = 60005;
    int RES_INVITE_TO = 60006;
    int RES_ACCEPT_FROM = 60007;
    int RES_ACCEPT_TO = 60008;
    int RES_TCP_STEP_1_TO = 60009;
    int RES_TCP_STEP_1_FROM = 60010;
    int RES_TCP_STEP_2_TO = 60011;
    int RES_TCP_STEP_2_FROM = 60012;
    int RES_TCP_STEP_3 = 60013;
    int RES_TCP_STEP_4 = 60014;

    /**
     * CLIENT RANGE:70000-79999
     */

    int RES_CLIENT_CHAT = 70000;

}
