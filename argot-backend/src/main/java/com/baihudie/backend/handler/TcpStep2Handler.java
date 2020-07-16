package com.baihudie.backend.handler;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.body.TcpStep2Body;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.exception.ArgotException;
import com.baihudie.api.utils.ApiConstants;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.entity.BanditEntity;
import com.baihudie.backend.entity.ConnSwitchEntity;
import com.baihudie.backend.pipe.PipeBodyAuto;
import com.baihudie.backend.pipe.PipeBodyMsg;
import com.baihudie.backend.pipe.PipeHandlerDispatcher;
import org.springframework.stereotype.Component;

@Component
public class TcpStep2Handler extends PipeHandlerDispatcher {

    public PipeBodyMsg genPipeBodyMsg(String pseudonym, int reqType, String body) {

        TcpStep2Body.TcpStep2ReqBody reqBody = JSON.parseObject(body, TcpStep2Body.TcpStep2ReqBody.class);

        String token = reqBody.getToken();
        if (token == null || token.length() == 0) {
            throw new ArgotException(ArgotErrorCode.ERROR_0018, "rabblePseudonym is NULL.");
        }

        String aBanditCode = reqBody.getABanditCode();
        if (aBanditCode == null || aBanditCode.length() == 0) {
            throw new ArgotException(ArgotErrorCode.ERROR_0019, "rabblePseudonym is NULL.");
        }

        String aPseudonym = reqBody.getAPseudonym();
        if (aPseudonym == null || aPseudonym.length() == 0) {
            throw new ArgotException(ArgotErrorCode.ERROR_0020, "originPseudonym is NULL.");
        }
        if (aPseudonym.equals(pseudonym)) {
            throw new ArgotException(ArgotErrorCode.ERROR_0021, "rabblePseudonym is NULL.");
        }

        BanditEntity bBanditEntity = pseudonymMap.get(pseudonym);
        if (bBanditEntity == null) {
            throw new ArgotException(ArgotErrorCode.ERROR_0022, "rabblePseudonym is NULL.");
        }

        PipeBodyMsg pipeBody = new PipeBodyMsg(PipeBodyMsg.SEND_TO_LIST);

        BanditEntity aBanditEntity = pseudonymMap.get(aPseudonym);

        if (aBanditEntity == null) {

            addErrorMessageFrom(pipeBody, token, aBanditCode, pseudonym);
            return pipeBody;
        }

        ConnSwitchEntity connSwitchEntity = connSwitchMap.get(token);
        if (connSwitchEntity == null) {

            addErrorMessageFrom(pipeBody, token, aBanditCode, pseudonym);
            return pipeBody;
        }

        boolean valid = connSwitchEntity.validate(aBanditEntity, bBanditEntity);
        if (!valid) {
            addErrorMessageFrom(pipeBody, token, aPseudonym, pseudonym);
            return pipeBody;
        }

        connSwitchEntity.setTcp2(ConnSwitchEntity.OK);


        TcpStep2Body.TcpStep2ResFromBody resFromBody = new TcpStep2Body.TcpStep2ResFromBody();
        resFromBody.setToken(token);
        resFromBody.setAPseudonym(aPseudonym);
        resFromBody.setResult(ApiConstants.SUCCESS);
        pipeBody.addMessageBody(pseudonym, ArgotType.RES_TCP_STEP_2_FROM, JSON.toJSONString(resFromBody));


        TcpStep2Body.TcpStep2ResToBody resToBody = new TcpStep2Body.TcpStep2ResToBody();
        resToBody.setToken(token);
        resToBody.setBPseudonym(pseudonym);
        pipeBody.addMessageBody(aPseudonym, ArgotType.RES_TCP_STEP_2_TO, JSON.toJSONString(resToBody));

        return pipeBody;

    }

    private void addErrorMessageFrom(PipeBodyMsg pipeBody, String token, String aPseudonym, String pseudonym) {

        TcpStep2Body.TcpStep2ResFromBody resFromBody = new TcpStep2Body.TcpStep2ResFromBody();
        resFromBody.setToken(token);
        resFromBody.setAPseudonym(aPseudonym);
        resFromBody.setResult(ApiConstants.ERROR);
        pipeBody.addMessageBody(pseudonym, ArgotType.RES_TCP_STEP_2_FROM, JSON.toJSONString(resFromBody));

    }

}
