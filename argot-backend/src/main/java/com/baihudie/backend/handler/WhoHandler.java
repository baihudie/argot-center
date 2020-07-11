package com.baihudie.backend.handler;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.proto.body.QueryAllResBody;
import com.baihudie.api.proto.body.QueryAllResBodySub;
import com.baihudie.backend.entity.BanditEntity;
import com.baihudie.backend.pipe.MessageBody;
import com.baihudie.backend.pipe.PipeBodyMsg;
import com.baihudie.backend.pipe.PipeHandlerDispatcher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class WhoHandler extends PipeHandlerDispatcher {


    public PipeBodyMsg genPipeBodyMsg(String pseudonym, int reqType, String body) {
        PipeBodyMsg pipeBody = new PipeBodyMsg(PipeBodyMsg.SEND_TO_SELF);
        MessageBody messageBody = new MessageBody();

        QueryAllResBody resBody = new QueryAllResBody();
        List<QueryAllResBodySub> pseudonymList = new ArrayList<>();
        resBody.setPseudonymList(pseudonymList);

        Set<Map.Entry<String, BanditEntity>> entrySet = pseudonymMap.entrySet();
        for (Map.Entry<String, BanditEntity> entry : entrySet) {
            String pseudonymOne = entry.getKey();
            BanditEntity banditEntity = entry.getValue();

            QueryAllResBodySub bodySub = new QueryAllResBodySub();
            bodySub.setPseudonym(pseudonymOne);
            bodySub.setGoodName(banditEntity.getGoodName());

            pseudonymList.add(bodySub);
        }

        messageBody.setResType(ArgotType.RES_QUERY_ALL);
        messageBody.setBody(JSON.toJSONString(resBody));
        pipeBody.addMessageBody(messageBody);

        return pipeBody;

    }
}
