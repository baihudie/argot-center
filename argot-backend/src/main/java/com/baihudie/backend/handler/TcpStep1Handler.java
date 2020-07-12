package com.baihudie.backend.handler;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.proto.body.TcpStep1ReqBody;
import com.baihudie.api.proto.body.TcpStep1ResFromBody;
import com.baihudie.api.proto.body.TcpStep1ResToBody;
import com.baihudie.api.utils.ApiConstants;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.constants.ArgotException;
import com.baihudie.backend.entity.BanditEntity;
import com.baihudie.backend.entity.TcpEntity;
import com.baihudie.backend.pipe.PipeBodyAuto;
import com.baihudie.backend.pipe.PipeBodyMsg;
import com.baihudie.backend.pipe.PipeHandlerDispatcher;
import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

@Component
public class TcpStep1Handler extends PipeHandlerDispatcher {


    public PipeBodyAuto genPipeBodyAuto(Channel channel, String pseudonym, int reqType, String body) {

        TcpStep1ReqBody reqBody = JSON.parseObject(body, TcpStep1ReqBody.class);

        String rabblePseudonym = reqBody.getRabblePseudonym();

        if (rabblePseudonym == null || rabblePseudonym.length() == 0) {
            throw new ArgotException(ArgotErrorCode.PSEUDONYM_NULL, "rabblePseudonym is NULL.");
        }

        BanditEntity rabbleBanditEntity = pseudonymMap.get(rabblePseudonym);

        PipeBodyAuto pipeBody = new PipeBodyAuto(PipeBodyMsg.SEND_TO_LIST);

        if (rabbleBanditEntity == null) {

            TcpStep1ResFromBody resFromBody = new TcpStep1ResFromBody();

            resFromBody.setRabblePseudonym(rabblePseudonym);
            resFromBody.setStepResult(ApiConstants.ERROR);
            pipeBody.addMessageBody(pseudonym, ArgotType.RES_TCP_STEP_1_FROM, JSON.toJSONString(resFromBody));
            return pipeBody;

        }

        //pseudonym 是申请者，被同意者，发起连接者。rabblePseudonym是被申请者，同意者，被连接者
        TcpEntity tcpEntity = new TcpEntity();
        tcpEntity.setFlag(FLAG_1);
        tcpEntity.setOriginPseudonym(pseudonym);
        rabbleMap.put(pseudonym + SPLIT + rabblePseudonym, tcpEntity);

        TcpStep1ResFromBody resFromBody = new TcpStep1ResFromBody();
        resFromBody.setRabblePseudonym(rabblePseudonym);
        resFromBody.setStepResult(ApiConstants.SUCCESS);

        pipeBody.addMessageBody(pseudonym, ArgotType.RES_TCP_STEP_1_FROM, JSON.toJSONString(resFromBody));

        TcpStep1ResToBody resToBody = new TcpStep1ResToBody();
        resToBody.setOriginPseudonym(pseudonym);

        pipeBody.addMessageBody(rabblePseudonym, ArgotType.RES_TCP_STEP_1_TO, JSON.toJSONString(resToBody));

        return pipeBody;


    }
}
