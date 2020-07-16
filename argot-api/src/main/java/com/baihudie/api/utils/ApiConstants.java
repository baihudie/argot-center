package com.baihudie.api.utils;


public interface ApiConstants {

    int SUCCESS = 0;
    int ERROR = 1;

    int MSG_TYPE_PROTO = 0;
    int MSG_TYPE_BASE64_STRING = 1;

    String DELIMITER = "-$_$-";
    //    String DELIMITER = "AAA";
    int MAX_FRAME_LENGTH = 8 * 1024;
}
