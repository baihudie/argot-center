package com.baihudie.backend.handler;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.proto.body.InviteAcceptFromResBody;
import com.baihudie.api.proto.body.InviteAcceptReqBody;
import com.baihudie.api.proto.body.InviteAcceptToResBody;
import com.baihudie.api.utils.ApiConstants;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.constants.ArgotException;
import com.baihudie.backend.entity.BanditEntity;
import com.baihudie.backend.pipe.PipeBodyMsg;
import com.baihudie.backend.pipe.PipeHandlerDispatcher;
import org.springframework.stereotype.Component;

@Component
public class InviteAcceptHandler extends PipeHandlerDispatcher {

    public PipeBodyMsg genPipeBodyMsg(String pseudonym, int reqType, String body) {

        InviteAcceptReqBody reqBody = JSON.parseObject(body, InviteAcceptReqBody.class);

        String originPseudonym = reqBody.getOriginPseudonym();

        if (originPseudonym == null || originPseudonym.length() == 0) {

            throw new ArgotException(ArgotErrorCode.PSEUDONYM_NULL, "toPseudonym is NULL.");
        }

        PipeBodyMsg pipeBody = new PipeBodyMsg(PipeBodyMsg.SEND_TO_LIST);

        BanditEntity fromBanditEntity = pseudonymMap.get(pseudonym);
        BanditEntity toBanditEntity = pseudonymMap.get(originPseudonym);

        if (fromBanditEntity == null) {
            throw new ArgotException(ArgotErrorCode.PSEUDONYM_NULL, "InviteApplyHandler,from pseudonym is NULL");
        }

        if (toBanditEntity == null) {

            InviteAcceptFromResBody resFromBody = new InviteAcceptFromResBody();

            resFromBody.setOriginPseudonym(originPseudonym);
            resFromBody.setInviteResult(ApiConstants.ERROR);

            pipeBody.addMessageBody(pseudonym, ArgotType.RES_INVITE_ACCEPT_FROM, JSON.toJSONString(resFromBody));

        } else {

            InviteAcceptFromResBody resFromBody = new InviteAcceptFromResBody();
            resFromBody.setInviteResult(ApiConstants.SUCCESS);
            resFromBody.setOriginPseudonym(originPseudonym);

            pipeBody.addMessageBody(pseudonym, ArgotType.RES_INVITE_ACCEPT_FROM, JSON.toJSONString(resFromBody));


            InviteAcceptToResBody resToBody = new InviteAcceptToResBody();
            resToBody.setRabblePseudonym(pseudonym);

            pipeBody.addMessageBody(originPseudonym, ArgotType.RES_INVITE_ACCEPT_TO, JSON.toJSONString(resToBody));
        }

        return pipeBody;

    }

}
