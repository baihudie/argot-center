package com.baihudie.backend.constants;

public interface ArgotErrorCode {

    int SUCCESS = 0;

    int SYS_ERROR = 9;

    //server
    int BANDIT_CODE_NULL = 1000;
    int BANDIT_CODE_EXIST = 1001;

    //client
    int PSEUDONYM_NULL = 2001;
    int REQ_TYPE_NOT_SUPPORT = 2002;
}
