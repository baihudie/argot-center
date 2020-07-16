package com.baihudie.api.body;

import lombok.Data;

public class AcceptBody {


    @Data
    public static class AcceptReqBody {

        //同意接收人
        private String aPseudonym;

        private String bBanditCode;

        private String bGoodName;

    }

    @Data
    public static class AcceptFromResBody {

        private int result;

        private String aPseudonym;

    }


    @Data
    public static class AcceptToResBody {

        //通过好友申请
        private String bPseudonym;

        private String bBanditCode;

        private String bGoodName;

    }
}
