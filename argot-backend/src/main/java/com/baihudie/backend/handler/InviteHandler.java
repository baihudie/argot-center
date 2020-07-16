package com.baihudie.backend.handler;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.body.InviteBody;
import com.baihudie.api.utils.ApiConstants;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.api.exception.ArgotException;
import com.baihudie.backend.entity.BanditEntity;
import com.baihudie.backend.pipe.PipeBodyMsg;
import com.baihudie.backend.pipe.PipeHandlerDispatcher;
import org.springframework.stereotype.Component;

@Component
public class InviteHandler extends PipeHandlerDispatcher {

    public PipeBodyMsg genPipeBodyMsg(String pseudonym, int reqType, String body) {

        InviteBody.InviteReqBody reqBody = JSON.parseObject(body, InviteBody.InviteReqBody.class);

        String aBanditCode = reqBody.getABanditCode();
        if(aBanditCode == null || aBanditCode.length() == 0){
            throw new ArgotException(ArgotErrorCode.ERROR_0009, "InviteHandler BANDIT_CODE_NULL");
        }

        String bPseudonym = reqBody.getBPseudonym();
        if (bPseudonym == null || bPseudonym.length() == 0) {
            throw new ArgotException(ArgotErrorCode.ERROR_0010, "InviteHandler PSEUDONYM_NULL");
        }

        BanditEntity aBanditEntity = pseudonymMap.get(pseudonym);
        if (aBanditEntity == null) {
            throw new ArgotException(ArgotErrorCode.ERROR_0011, "InviteHandler PSEUDONYM_NULL");
        }
        if(!aBanditEntity.getBanditCode().equals(aBanditCode)){
            throw new ArgotException(ArgotErrorCode.ERROR_0012, "InviteHandler BANDIT_CODE_ERROR");
        }


        PipeBodyMsg pipeBody = new PipeBodyMsg(PipeBodyMsg.SEND_TO_LIST);

        BanditEntity bBanditEntity = pseudonymMap.get(bPseudonym);
        if (bBanditEntity == null) {

            InviteBody.InviteFromResBody resFromBody = new InviteBody.InviteFromResBody();

            resFromBody.setBPseudonym(bPseudonym);
            resFromBody.setResult(ApiConstants.ERROR);

            pipeBody.addMessageBody(pseudonym, ArgotType.RES_INVITE_FROM, JSON.toJSONString(resFromBody));

        } else {

            InviteBody.InviteFromResBody resFromBody = new InviteBody.InviteFromResBody();
            resFromBody.setResult(ApiConstants.SUCCESS);
            resFromBody.setBPseudonym(bPseudonym);

            pipeBody.addMessageBody(pseudonym, ArgotType.RES_INVITE_FROM, JSON.toJSONString(resFromBody));


            InviteBody.InviteToResBody resToBody = new InviteBody.InviteToResBody();
            resToBody.setAPseudonym(pseudonym);
            resToBody.setABanditCode(aBanditCode);
            resToBody.setAGoodName(aBanditEntity.getGoodName());
            resToBody.setNotes(reqBody.getNotes());

            pipeBody.addMessageBody(bPseudonym, ArgotType.RES_INVITE_TO, JSON.toJSONString(resToBody));
        }
        return pipeBody;
    }

}
