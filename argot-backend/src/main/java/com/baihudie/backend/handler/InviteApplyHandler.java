package com.baihudie.backend.handler;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.proto.body.InviteApplyFromResBody;
import com.baihudie.api.proto.body.InviteApplyReqBody;
import com.baihudie.api.proto.body.InviteApplyToResBody;
import com.baihudie.api.utils.ApiConstants;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.constants.ArgotException;
import com.baihudie.backend.entity.BanditEntity;
import com.baihudie.backend.pipe.PipeBodyMsg;
import com.baihudie.backend.pipe.PipeHandlerDispatcher;
import org.springframework.stereotype.Component;

@Component
public class InviteApplyHandler extends PipeHandlerDispatcher {

    public PipeBodyMsg genPipeBodyMsg(String pseudonym, int reqType, String body) {


        InviteApplyReqBody reqBody = JSON.parseObject(body, InviteApplyReqBody.class);
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

            InviteApplyFromResBody resFromBody = new InviteApplyFromResBody();

            resFromBody.setRabblePseudonym(rabblePseudonym);
            resFromBody.setInviteResult(ApiConstants.ERROR);

            pipeBody.addMessageBody(pseudonym, ArgotType.RES_INVITE_APPLY_FROM, JSON.toJSONString(resFromBody));

        } else {

            InviteApplyFromResBody resFromBody = new InviteApplyFromResBody();
            resFromBody.setInviteResult(ApiConstants.SUCCESS);
            resFromBody.setRabblePseudonym(rabblePseudonym);

            pipeBody.addMessageBody(pseudonym, ArgotType.RES_INVITE_APPLY_FROM, JSON.toJSONString(resFromBody));


            InviteApplyToResBody resToBody = new InviteApplyToResBody();
            resToBody.setOriginPseudonym(pseudonym);
            resToBody.setOriginGoodName(originBanditEntity.getGoodName());
            resToBody.setNotes(reqBody.getNotes());

            pipeBody.addMessageBody(rabblePseudonym, ArgotType.RES_INVITE_APPLY_TO, JSON.toJSONString(resToBody));
        }
        return pipeBody;
    }

}
