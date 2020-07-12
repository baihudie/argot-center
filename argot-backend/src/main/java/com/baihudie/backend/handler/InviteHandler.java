package com.baihudie.backend.handler;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.proto.body.InviteFromResBody;
import com.baihudie.api.proto.body.InviteReqBody;
import com.baihudie.api.proto.body.InviteToResBody;
import com.baihudie.api.utils.ApiConstants;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.constants.ArgotException;
import com.baihudie.backend.entity.BanditEntity;
import com.baihudie.backend.pipe.PipeBodyMsg;
import com.baihudie.backend.pipe.PipeHandlerDispatcher;
import org.springframework.stereotype.Component;

@Component
public class InviteHandler extends PipeHandlerDispatcher {

    public PipeBodyMsg genPipeBodyMsg(String pseudonym, int reqType, String body) {


        InviteReqBody reqBody = JSON.parseObject(body, InviteReqBody.class);
        String rabblePseudonym = reqBody.getRabblePseudonym();
        if (rabblePseudonym == null || rabblePseudonym.length() == 0) {
            throw new ArgotException(ArgotErrorCode.PSEUDONYM_NULL, "toPseudonym is NULL.");
        }

        BanditEntity originBanditEntity = pseudonymMap.get(pseudonym);
        BanditEntity rabbleBanditEntity = pseudonymMap.get(rabblePseudonym);

        if (originBanditEntity == null) {
            throw new ArgotException(ArgotErrorCode.PSEUDONYM_NULL, "InviteApplyHandler,from pseudonym is NULL");
        }


        PipeBodyMsg pipeBody = new PipeBodyMsg(PipeBodyMsg.SEND_TO_LIST);

        if (rabbleBanditEntity == null) {

            InviteFromResBody resFromBody = new InviteFromResBody();

            resFromBody.setRabblePseudonym(rabblePseudonym);
            resFromBody.setInviteResult(ApiConstants.ERROR);

            pipeBody.addMessageBody(pseudonym, ArgotType.RES_INVITE_FROM, JSON.toJSONString(resFromBody));

        } else {

            InviteFromResBody resFromBody = new InviteFromResBody();
            resFromBody.setInviteResult(ApiConstants.SUCCESS);
            resFromBody.setRabblePseudonym(rabblePseudonym);

            pipeBody.addMessageBody(pseudonym, ArgotType.RES_INVITE_FROM, JSON.toJSONString(resFromBody));


            InviteToResBody resToBody = new InviteToResBody();
            resToBody.setOriginPseudonym(pseudonym);
            resToBody.setOriginGoodName(originBanditEntity.getGoodName());
            resToBody.setNotes(reqBody.getNotes());

            pipeBody.addMessageBody(rabblePseudonym, ArgotType.RES_INVITE_TO, JSON.toJSONString(resToBody));
        }
        return pipeBody;
    }

}
