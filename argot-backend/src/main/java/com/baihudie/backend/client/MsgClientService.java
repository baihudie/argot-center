package com.baihudie.backend.client;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.proto.*;
import com.baihudie.api.proto.body.ReqActiveBody;
import com.baihudie.api.proto.body.ReqChatAllBody;
import com.baihudie.api.proto.body.ResActiveBody;
import com.baihudie.api.proto.body.ResChatAllBody;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.constants.ArgotException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MsgClientService {

    private String banditCode;

    public MsgClientService(String banditCode) {
        this.banditCode = banditCode;
    }

    public ReqActiveBody genReqActiveBody() {

        ReqActiveBody body = new ReqActiveBody();
        body.setBanditCode(banditCode);

        return body;
    }

    public ReqChatAllBody genReqChatBody(String content) {

        ReqChatAllBody body = new ReqChatAllBody();
        body.setContent(content);
        return body;

    }


    public String handleResActive(ArgotResProto.ArgotRes res) {

        String body = res.getBody();
        ResActiveBody resBody = JSON.parseObject(body, ResActiveBody.class);
        String pseudonym = resBody.getPseudonym();
        if (pseudonym == null) {
            throw new ArgotException(ArgotErrorCode.PSEUDONYM_NULL, "PSEUDONYM is NULL");
        }
        return pseudonym;
    }


    public void handleResChat(ArgotResProto.ArgotRes res) {
        String body = res.getBody();
        ResChatAllBody resBody = JSON.parseObject(body, ResChatAllBody.class);
        String content = resBody.getContent();
        String pseudonym = resBody.getPseudonym();
        log.info("[" + pseudonym + "]:" + content);
    }
}
