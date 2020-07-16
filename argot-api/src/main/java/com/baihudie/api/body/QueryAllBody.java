package com.baihudie.api.body;

import lombok.Data;

import java.util.List;

public class QueryAllBody {
    @Data
    public static class QueryAllReqBody {
    }

    @Data
    public static class QueryAllResBody {

        private List<QueryAllResBodySub> pseudonymList;

    }

    @Data
    public static class QueryAllResBodySub {

        private String pseudonym;

        private String goodName;

    }
}
