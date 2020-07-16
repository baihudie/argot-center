package com.baihudie.api.body;

import lombok.Data;

@Data
public class TestBody {

    @Data
    public static class TestReqBody {

        private String content;

    }

    @Data
    public static class TestResBody {

        private String content;

    }

}
