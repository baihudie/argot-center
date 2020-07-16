package com.baihudie.api.body;

import lombok.Data;

public class TcpStep4Body {
    @Data
    public static class TcpStep4ReqBody {

        private String token;
        private String aPseudonym;
        private String bPseudonym;

    }

    @Data
    public static class TcpStep4ResBody {

        private String token;
        // A
        private String aPseudonym;
        // private String aBanditCode;

        private String aRemoteIp;
        private int aRemotePort;

        // B
        private String bPseudonym;
        // private String bBanditCode;

        private String bRemoteIp;
        private int bRemotePort;

    }
}
