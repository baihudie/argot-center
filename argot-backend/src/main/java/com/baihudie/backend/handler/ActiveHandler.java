package com.baihudie.backend.handler;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.body.ActiveBody;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.api.exception.ArgotException;
import com.baihudie.backend.entity.BanditEntity;
import com.baihudie.backend.pipe.ControlBody;
import com.baihudie.backend.pipe.PipeBody;
import com.baihudie.backend.pipe.PipeBodyCon;
import com.baihudie.backend.pipe.PipeHandlerDispatcher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ActiveHandler extends PipeHandlerDispatcher {

    public PipeBodyCon genPipeBodyCon(String nullPseudonym, int reqType, String body) {

        ActiveBody.ActiveReqBody reqActiveBody = JSON.parseObject(body, ActiveBody.ActiveReqBody.class);

        if (nullPseudonym != null && nullPseudonym.length() > 0) {
            throw new ArgotException(ArgotErrorCode.ERROR_0005, "PSEUDONYM is NOT NULL");
        }

        // validate
        String banditCode = reqActiveBody.getBanditCode();
        if (banditCode == null) {
            throw new ArgotException(ArgotErrorCode.ERROR_0006, "BANDIT_CODE is NULL");
        }

        if (pseudonymMap.containsValue(banditCode)) {
            throw new ArgotException(ArgotErrorCode.ERROR_0007, "BANDIT_CODE exist");
        }

        String newPseudonym = UUID.randomUUID().toString().replaceAll("-", "");
        if (pseudonymMap.containsKey(newPseudonym)) {
            throw new ArgotException(ArgotErrorCode.ERROR_0008, "BANDIT_CODE exist");
        }

        //OK的情况下。

        String goodName = reqActiveBody.getGoodName();

        BanditEntity banditEntity = new BanditEntity();
        banditEntity.setPseudonym(newPseudonym);
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
        //发送消息
        ActiveBody.ActiveResBody resBody = new ActiveBody.ActiveResBody();
        resBody.setPseudonym(newPseudonym);

        pipeBodyCon.addMessageBody(newPseudonym, ArgotType.RES_ACTIVE, JSON.toJSONString(resBody));

        return pipeBodyCon;
    }

}
