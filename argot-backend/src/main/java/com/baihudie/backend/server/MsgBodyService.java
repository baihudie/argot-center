package com.baihudie.backend.server;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.proto.body.ReqActiveBody;
import com.baihudie.api.proto.body.ReqChatAllBody;
import com.baihudie.api.proto.body.ResActiveBody;
import com.baihudie.api.proto.body.ResChatAllBody;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.constants.ArgotException;
import com.baihudie.backend.entity.ControlBody;
import com.baihudie.backend.entity.MessageBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MsgBodyService {

    //pseudonym - banditCode
    private Map<String, String> pseudonymMap = new ConcurrentHashMap<>(2000);
    private Map<String, String> banditCodeMap = new ConcurrentHashMap<>(2000);

    public void removePseudonym(String pseudonym) {
        String banditCode = pseudonymMap.get(pseudonym);
        pseudonymMap.remove(pseudonym);
        if (banditCode != null) {
            banditCodeMap.remove(banditCode);
        }
    }

    //    public ResActiveBody genResActiveBody(ReqActiveBody reqBody) {
    public ControlBody genResActiveBody(ReqActiveBody reqBody) {

        // validate
        String banditCode = reqBody.getBanditCode();
        if (banditCode == null) {
            throw new ArgotException(ArgotErrorCode.BANDIT_CODE_NULL, "BANDIT_CODE is NULL");
        }

        if (pseudonymMap.containsValue(banditCode)) {
            throw new ArgotException(ArgotErrorCode.BANDIT_CODE_EXIST, "BANDIT_CODE exist");
        }

        String pseudonym = UUID.randomUUID().toString().replaceAll("-", "");
        if (pseudonymMap.containsKey(pseudonym)) {
            throw new ArgotException(ArgotErrorCode.BANDIT_CODE_EXIST, "BANDIT_CODE exist");
        }
        //OK的情况下。

        pseudonymMap.put(pseudonym, banditCode);
        banditCodeMap.put(banditCode, pseudonym);

        //返回这种对象
        ControlBody controlBody = new ControlBody();
        controlBody.setControlType(ControlBody.CON_ACTIVE);
        controlBody.setControlMsg(pseudonym);

        ResActiveBody resBody = new ResActiveBody();
        resBody.setPseudonym(pseudonym);
        String body = JSON.toJSONString(resBody);

        controlBody.setBody(body);

        return controlBody;
    }

    public MessageBody genMessageBody(String pseudonym, int reqType, String body) {
        MessageBody messageBody = new MessageBody();

        if (reqType == ArgotType.REQ_CHAT_ALL) {

            ReqChatAllBody reqBody = JSON.parseObject(body, ReqChatAllBody.class);

            ResChatAllBody resBody = new ResChatAllBody();

            String content = reqBody.getContent();
            resBody.setContent(content);
            resBody.setPseudonym(pseudonym);

            messageBody.setSendTo(MessageBody.TO_ALL);
            messageBody.setBody(JSON.toJSONString(resBody));


        } else {
            throw new ArgotException(ArgotErrorCode.REQ_TYPE_NOT_SUPPORT, "REQ_TYPE_NOT_SUPPORT reqType:" + reqType);
        }

        return messageBody;
    }


}
