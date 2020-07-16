package com.baihudie.backend.handler;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.body.ChatsBody;
import com.baihudie.backend.pipe.PipeBodyMsg;
import com.baihudie.backend.pipe.PipeHandlerDispatcher;
import org.springframework.stereotype.Component;

@Component
public class ChatsHandler extends PipeHandlerDispatcher {

    public PipeBodyMsg genPipeBodyMsg(String pseudonym, int reqType, String body) {

        PipeBodyMsg pipeBody = new PipeBodyMsg(PipeBodyMsg.SEND_TO_ALL);

        ChatsBody.ChatsReqBody reqBody = JSON.parseObject(body, ChatsBody.ChatsReqBody.class);

        ChatsBody.ChatsResBody resBody = new ChatsBody.ChatsResBody();
        String content = reqBody.getContent();
        resBody.setContent(content);
        resBody.setPseudonym(pseudonym);

        pipeBody.addMessageBody(null, ArgotType.RES_CHATS, JSON.toJSONString(resBody));

        return pipeBody;

    }

}
