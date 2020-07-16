package com.baihudie.api.body;

import lombok.Data;

public class ActiveBody {
    @Data
    public static class ActiveReqBody {

        private String banditCode;

        private String goodName;

    }

    @Data
    public static class ActiveResBody {

        private String pseudonym;

    }
}
