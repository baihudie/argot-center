package com.baihudie.backend.handler;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.proto.body.ChatsReqBody;
import com.baihudie.api.proto.body.ChatsResBody;
import com.baihudie.backend.pipe.PipeBodyMsg;
import com.baihudie.backend.pipe.PipeHandlerDispatcher;
import org.springframework.stereotype.Component;

@Component
public class ChatsHandler extends PipeHandlerDispatcher {

    public PipeBodyMsg genPipeBodyMsg(String pseudonym, int reqType, String body) {

        PipeBodyMsg pipeBody = new PipeBodyMsg(PipeBodyMsg.SEND_TO_ALL);

        ChatsReqBody reqBody = JSON.parseObject(body, ChatsReqBody.class);

        ChatsResBody resBody = new ChatsResBody();
        String content = reqBody.getContent();
        resBody.setContent(content);
        resBody.setPseudonym(pseudonym);

        pipeBody.addMessageBody(null, ArgotType.RES_CHATS, JSON.toJSONString(resBody));

        return pipeBody;

    }

}
