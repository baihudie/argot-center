package com.baihudie.backend.handler;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.proto.body.AcceptFromResBody;
import com.baihudie.api.proto.body.AcceptReqBody;
import com.baihudie.api.proto.body.AcceptToResBody;
import com.baihudie.api.utils.ApiConstants;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.constants.ArgotException;
import com.baihudie.backend.entity.BanditEntity;
import com.baihudie.backend.pipe.PipeBodyMsg;
import com.baihudie.backend.pipe.PipeHandlerDispatcher;
import org.springframework.stereotype.Component;

@Component
public class AcceptHandler extends PipeHandlerDispatcher {

    public PipeBodyMsg genPipeBodyMsg(String pseudonym, int reqType, String body) {

        AcceptReqBody reqBody = JSON.parseObject(body, AcceptReqBody.class);

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

            AcceptFromResBody resFromBody = new AcceptFromResBody();

            resFromBody.setOriginPseudonym(originPseudonym);
            resFromBody.setInviteResult(ApiConstants.ERROR);

            pipeBody.addMessageBody(pseudonym, ArgotType.RES_ACCEPT_FROM, JSON.toJSONString(resFromBody));

        } else {

            AcceptFromResBody resFromBody = new AcceptFromResBody();
            resFromBody.setInviteResult(ApiConstants.SUCCESS);
            resFromBody.setOriginPseudonym(originPseudonym);

            pipeBody.addMessageBody(pseudonym, ArgotType.RES_ACCEPT_FROM, JSON.toJSONString(resFromBody));


            AcceptToResBody resToBody = new AcceptToResBody();
            resToBody.setRabblePseudonym(pseudonym);

            pipeBody.addMessageBody(originPseudonym, ArgotType.RES_ACCEPT_TO, JSON.toJSONString(resToBody));
        }

        return pipeBody;

    }

}
