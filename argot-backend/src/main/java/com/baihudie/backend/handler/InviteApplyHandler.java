package com.baihudie.backend.handler;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.proto.body.InviteApplyFromResBody;
import com.baihudie.api.proto.body.InviteApplyReqBody;
import com.baihudie.api.proto.body.InviteApplyToResBody;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.constants.ArgotException;
import com.baihudie.backend.entity.BanditEntity;
import com.baihudie.backend.pipe.MessageBody;
import com.baihudie.backend.pipe.PipeBodyMsg;
import com.baihudie.backend.pipe.PipeHandlerDispatcher;
import org.springframework.stereotype.Component;

@Component
public class InviteApplyHandler extends PipeHandlerDispatcher {

    public PipeBodyMsg genPipeBodyMsg(String pseudonym, int reqType, String body) {


        InviteApplyReqBody reqBody = JSON.parseObject(body, InviteApplyReqBody.class);
        String toPseudonym = reqBody.getToPseudonym();
        if (toPseudonym == null || toPseudonym.length() == 0) {
            throw new ArgotException(ArgotErrorCode.PSEUDONYM_NULL, "toPseudonym is NULL.");
        }

        PipeBodyMsg pipeBody = new PipeBodyMsg(PipeBodyMsg.SEND_TO_LIST);

        BanditEntity fromBanditEntity = pseudonymMap.get(pseudonym);
        BanditEntity toBanditEntity = pseudonymMap.get(toPseudonym);

        if (fromBanditEntity == null) {
            throw new ArgotException(ArgotErrorCode.PSEUDONYM_NULL, "InviteApplyHandler,from pseudonym is NULL");
        }

        if (toBanditEntity == null) {

            MessageBody messageBody = new MessageBody();
            messageBody.setResType(ArgotType.RES_INVITE_APPLY_FROM);
            messageBody.setPseudonym(pseudonym);

            InviteApplyFromResBody resFromBody = new InviteApplyFromResBody();

            resFromBody.setToPseudonym(toPseudonym);
            resFromBody.setInviteResult(InviteApplyFromResBody.RESULT_NOT_SET);

            messageBody.setBody(JSON.toJSONString(resFromBody));

            pipeBody.addMessageBody(messageBody);

        } else {

            MessageBody fromMessageBody = new MessageBody();
            fromMessageBody.setResType(ArgotType.RES_INVITE_APPLY_FROM);
            fromMessageBody.setPseudonym(pseudonym);

            InviteApplyFromResBody resFromBody = new InviteApplyFromResBody();
            resFromBody.setInviteResult(InviteApplyFromResBody.RESULT_SEND_TO);
            resFromBody.setToPseudonym(toPseudonym);

            fromMessageBody.setBody(JSON.toJSONString(resFromBody));
            pipeBody.addMessageBody(fromMessageBody);

            //
            MessageBody toMessageBody = new MessageBody();
            toMessageBody.setResType(ArgotType.RES_INVITE_APPLY_TO);
            toMessageBody.setPseudonym(toPseudonym);

            InviteApplyToResBody resToBody = new InviteApplyToResBody();
            resToBody.setFromPseudonym(pseudonym);
            resToBody.setFromGoodName(fromBanditEntity.getGoodName());
            resToBody.setNotes(reqBody.getNotes());

            toMessageBody.setBody(JSON.toJSONString(resToBody));

            pipeBody.addMessageBody(toMessageBody);
        }
        return pipeBody;
    }

}
