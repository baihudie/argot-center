package com.baihudie.backend.handler;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.proto.body.ActiveReqBody;
import com.baihudie.api.proto.body.ActiveResBody;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.constants.ArgotException;
import com.baihudie.backend.entity.BanditEntity;
import com.baihudie.backend.pipe.*;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ActiveHandler extends PipeHandlerDispatcher {

    public PipeBodyCon genPipeBodyCon(String nullPseudonym, int reqType, String body) {

        ActiveReqBody reqActiveBody = JSON.parseObject(body, ActiveReqBody.class);

        if (nullPseudonym != null && nullPseudonym.length() > 0) {
            throw new ArgotException(ArgotErrorCode.PSEUDONYM_NOT_NULL, "PSEUDONYM is NOT NULL");
        }

        // validate
        String banditCode = reqActiveBody.getBanditCode();
        if (banditCode == null) {
            throw new ArgotException(ArgotErrorCode.BANDIT_CODE_NULL, "BANDIT_CODE is NULL");
        }

        if (pseudonymMap.containsValue(banditCode)) {
            throw new ArgotException(ArgotErrorCode.BANDIT_CODE_EXIST, "BANDIT_CODE exist");
        }

        String newPseudonym = UUID.randomUUID().toString().replaceAll("-", "");
        if (pseudonymMap.containsKey(newPseudonym)) {
            throw new ArgotException(ArgotErrorCode.BANDIT_CODE_EXIST, "BANDIT_CODE exist");
        }

        //OK的情况下。

        String goodName = reqActiveBody.getGoodName();

        BanditEntity banditEntity = new BanditEntity();
        banditEntity.setBanditCode(banditCode);
        banditEntity.setGoodName(goodName);

        pseudonymMap.put(newPseudonym, banditEntity);
        banditCodeMap.put(banditCode, newPseudonym);

        //建立控制平面消息
        PipeBodyCon pipeBodyCon = new PipeBodyCon(PipeBody.SEND_TO_SELF);
        pipeBodyCon.setConType(PipeBodyCon.CON_ACTIVE);

        //控制平面消息，内部
        ControlBody controlBody = new ControlBody();
        controlBody.setPseudonym(newPseudonym);
        pipeBodyCon.setControlBody(controlBody);

        //信息平面，外部
        MessageBody messageBody = new MessageBody();
        messageBody.setPseudonym(newPseudonym);

        //发送消息
        ActiveResBody resBody = new ActiveResBody();
        resBody.setPseudonym(newPseudonym);
        messageBody.setBody(JSON.toJSONString(resBody));
        messageBody.setResType(ArgotType.RES_ACTIVE);

        pipeBodyCon.addMessageBody(messageBody);

        return pipeBodyCon;
    }

}
