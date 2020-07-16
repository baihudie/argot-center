package com.baihudie.api.body;

import lombok.Data;

public class InviteBody {
    @Data
    public static class InviteFromResBody {

        private String bPseudonym;

        private int result;

    }

    @Data
    public static class InviteReqBody {

        private String aBanditCode;

//        private String aGoodName;

        private String bPseudonym;

        private String notes;

    }

    @Data
    public static class InviteToResBody {

        private String aBanditCode;

        private String aPseudonym;

        private String aGoodName;

        private String notes;

    }
}
