package com.baihudie.backend.service;

import com.baihudie.api.proto.body.ReqActiveBody;
import com.baihudie.api.proto.body.ReqChatAllBody;
import com.baihudie.api.proto.body.ResActiveBody;
import com.baihudie.api.proto.body.ResChatAllBody;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.constants.ArgotException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MsgServerService {

    //pseudonym - banditCode
    private Map<String, String> pseudonymMap = new ConcurrentHashMap<>(2000);
    private Map<String, String> banditCodeMap = new ConcurrentHashMap<>(2000);


    public ResActiveBody genResActiveBody(ReqActiveBody reqBody) {

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

        ResActiveBody resBody = new ResActiveBody();
        resBody.setPseudonym(pseudonym);
        return resBody;
    }


    public void removePseudonym(String pseudonym) {
        String banditCode = pseudonymMap.get(pseudonym);
        pseudonymMap.remove(pseudonym);
        if (banditCode != null) {
            banditCodeMap.remove(banditCode);
        }

    }

    public ResChatAllBody genResChatAllBody(String pseudonym, ReqChatAllBody reqBody) {

        ResChatAllBody resBody = new ResChatAllBody();

        String content = reqBody.getContent();
        resBody.setContent(content);
        resBody.setPseudonym(pseudonym);

        return resBody;

    }
}
