package com.baihudie.backend.handler;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.body.TcpStep1Body;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.exception.ArgotException;
import com.baihudie.api.utils.ApiConstants;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.entity.BanditEntity;
import com.baihudie.backend.entity.ConnSwitchEntity;
import com.baihudie.backend.pipe.PipeBodyMsg;
import com.baihudie.backend.pipe.PipeHandlerDispatcher;
import org.springframework.stereotype.Component;

@Component
public class TcpStep1Handler extends PipeHandlerDispatcher {


    public PipeBodyMsg genPipeBodyMsg(String pseudonym, int reqType, String body) {

        TcpStep1Body.TcpStep1ReqBody reqBody = JSON.parseObject(body, TcpStep1Body.TcpStep1ReqBody.class);

        String token = reqBody.getToken();
        if (token == null || token.length() == 0) {
            throw new ArgotException(ArgotErrorCode.ERROR_0013, "rabblePseudonym is NULL.");
        }

        String bPseudonym = reqBody.getBPseudonym();

        if (bPseudonym == null || bPseudonym.length() == 0) {
            throw new ArgotException(ArgotErrorCode.ERROR_0014, "rabblePseudonym is NULL.");
        }
//        String bPseudonym = banditCodeMap.get(bBanditCode);
//        if (bPseudonym == null || bPseudonym.length() == 0) {
//            throw new ArgotException(ArgotErrorCode.ERROR_0015, "rabblePseudonym is NULL.");
//        }

        if (bPseudonym.equals(pseudonym)) {
            throw new ArgotException(ArgotErrorCode.ERROR_0016, "rabblePseudonym is NULL.");
        }

        BanditEntity aBanditEntity = pseudonymMap.get(pseudonym);
        if (aBanditEntity == null) {
            throw new ArgotException(ArgotErrorCode.ERROR_0017, "rabblePseudonym is NULL.");
        }

        PipeBodyMsg pipeBody = new PipeBodyMsg(PipeBodyMsg.SEND_TO_LIST);

        BanditEntity bBanditEntity = pseudonymMap.get(bPseudonym);
        if (bBanditEntity == null) {

            TcpStep1Body.TcpStep1ResFromBody resFromBody = new TcpStep1Body.TcpStep1ResFromBody();

            resFromBody.setBPseudonym(pseudonym);
            resFromBody.setToken(token);
            resFromBody.setResult(ApiConstants.ERROR);
            pipeBody.addMessageBody(pseudonym, ArgotType.RES_TCP_STEP_1_FROM, JSON.toJSONString(resFromBody));
            return pipeBody;

        }

        //pseudonym 是申请者，被同意者，发起连接者。rabblePseudonym是被申请者，同意者，被连接者
        ConnSwitchEntity connSwitchEntity = new ConnSwitchEntity();
        connSwitchEntity.setTcp1(ConnSwitchEntity.OK);
        connSwitchEntity.setToken(token);
        connSwitchEntity.setABanditCode(aBanditEntity.getBanditCode());
        connSwitchEntity.setAPseudonym(pseudonym);
        connSwitchEntity.setBBanditCode(bBanditEntity.getBanditCode());
        connSwitchEntity.setBPseudonym(bBanditEntity.getPseudonym());

        connSwitchMap.put(token, connSwitchEntity);

        TcpStep1Body.TcpStep1ResFromBody resFromBody = new TcpStep1Body.TcpStep1ResFromBody();
        resFromBody.setBPseudonym(pseudonym);
        resFromBody.setBPseudonym(bPseudonym);
        resFromBody.setToken(token);
        resFromBody.setResult(ApiConstants.SUCCESS);

        pipeBody.addMessageBody(pseudonym, ArgotType.RES_TCP_STEP_1_FROM, JSON.toJSONString(resFromBody));


        TcpStep1Body.TcpStep1ResToBody resToBody = new TcpStep1Body.TcpStep1ResToBody();
        resToBody.setABanditCode(aBanditEntity.getBanditCode());
        resToBody.setAPseudonym(pseudonym);
        resToBody.setToken(token);
        pipeBody.addMessageBody(bPseudonym, ArgotType.RES_TCP_STEP_1_TO, JSON.toJSONString(resToBody));

        return pipeBody;


    }
}
