package com.baihudie.backend.handler;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.body.AcceptBody;
import com.baihudie.api.utils.ApiConstants;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.api.exception.ArgotException;
import com.baihudie.backend.entity.BanditEntity;
import com.baihudie.backend.pipe.PipeBodyMsg;
import com.baihudie.backend.pipe.PipeHandlerDispatcher;
import org.springframework.stereotype.Component;

@Component
public class AcceptHandler extends PipeHandlerDispatcher {

    public PipeBodyMsg genPipeBodyMsg(String pseudonym, int reqType, String body) {

        AcceptBody.AcceptReqBody reqBody = JSON.parseObject(body, AcceptBody.AcceptReqBody.class);

        String bBanditCode = reqBody.getBBanditCode();
        if (bBanditCode == null || bBanditCode.length() == 0) {
            throw new ArgotException(ArgotErrorCode.ERROR_0001, "AcceptHandler BANDIT_CODE_NULL");
        }

        String aPseudonym = reqBody.getAPseudonym();
        if (aPseudonym == null || aPseudonym.length() == 0) {
            throw new ArgotException(ArgotErrorCode.ERROR_0002, "toPseudonym is NULL.");
        }

        BanditEntity bBanditEntity = pseudonymMap.get(pseudonym);
        if (bBanditEntity == null) {
            throw new ArgotException(ArgotErrorCode.ERROR_0003, "InviteHandler PSEUDONYM_NULL");
        }
        if (!bBanditEntity.getBanditCode().equals(bBanditCode)) {
            throw new ArgotException(ArgotErrorCode.ERROR_0004, "InviteHandler BANDIT_CODE_ERROR");
        }


        PipeBodyMsg pipeBody = new PipeBodyMsg(PipeBodyMsg.SEND_TO_LIST);

        BanditEntity aBanditEntity = pseudonymMap.get(aPseudonym);

        if (aBanditEntity == null) {

            AcceptBody.AcceptFromResBody resFromBody = new AcceptBody.AcceptFromResBody();

            resFromBody.setAPseudonym(aPseudonym);
            resFromBody.setResult(ApiConstants.ERROR);

            pipeBody.addMessageBody(pseudonym, ArgotType.RES_ACCEPT_FROM, JSON.toJSONString(resFromBody));

        } else {

            AcceptBody.AcceptFromResBody resFromBody = new AcceptBody.AcceptFromResBody();
            resFromBody.setResult(ApiConstants.SUCCESS);
            resFromBody.setAPseudonym(aPseudonym);

            pipeBody.addMessageBody(pseudonym, ArgotType.RES_ACCEPT_FROM, JSON.toJSONString(resFromBody));

            AcceptBody.AcceptToResBody resToBody = new AcceptBody.AcceptToResBody();
            resToBody.setBPseudonym(pseudonym);
            resToBody.setBBanditCode(bBanditCode);
            resToBody.setBGoodName(bBanditEntity.getGoodName());

            pipeBody.addMessageBody(aPseudonym, ArgotType.RES_ACCEPT_TO, JSON.toJSONString(resToBody));
        }

        return pipeBody;

    }

}
