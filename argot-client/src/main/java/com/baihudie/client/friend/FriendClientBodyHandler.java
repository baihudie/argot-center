package com.baihudie.client.friend;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.body.TestBody;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class FriendClientBodyHandler {

    public String handleReqTestBody(String body) {

        TestBody.TestReqBody reqBody = JSON.parseObject(body, TestBody.TestReqBody.class);

        return reqBody.getContent();
    }
}
