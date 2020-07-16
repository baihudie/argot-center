package com.baihudie.api.body;

import lombok.Data;

public class ChatsBody {
    @Data
    public static class ChatsReqBody {

        private String content;
    }

    @Data
    public static class ChatsResBody {

        private String pseudonym;

        private String content;
    }
}
