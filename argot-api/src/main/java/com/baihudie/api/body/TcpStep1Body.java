package com.baihudie.api.body;

import lombok.Data;

public class TcpStep1Body {

    @Data
    public static class TcpStep1ReqBody {

        //发送token，以及b-b-code
        private String token;
        private String bPseudonym;

    }

    @Data
    public static class TcpStep1ResFromBody {

        private int result;

        //收到token，以及b-b-code，b-p-code
        private String token;
        private String bBanditCode;
        private String bPseudonym;


    }

    @Data
    public static class TcpStep1ResToBody {

        //收到token，以及a-b-code，a-p-code
        private String token;
        private String aBanditCode;
        private String aPseudonym;

    }
}
