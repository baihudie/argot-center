package com.baihudie.api.body;

import lombok.Data;

public class TcpStep2Body {
    @Data
    public static class TcpStep2ReqBody {

        private String token;
        private String aBanditCode;
        private String aPseudonym;

    }

    @Data
    public static class TcpStep2ResFromBody {

        private int result;

        private String token;
        private String aPseudonym;

    }

    @Data
    public static class TcpStep2ResToBody {

        private String token;
        private String bPseudonym;

    }
}
