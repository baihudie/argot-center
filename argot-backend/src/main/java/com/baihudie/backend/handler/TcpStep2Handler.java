package com.baihudie.backend.handler;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.proto.body.TcpStep1ResFromBody;
import com.baihudie.api.proto.body.TcpStep2ReqBody;
import com.baihudie.api.proto.body.TcpStep2ResFromBody;
import com.baihudie.api.proto.body.TcpStep2ResToBody;
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
public class TcpStep2Handler extends PipeHandlerDispatcher {

    public PipeBodyAuto genPipeBodyAuto(Channel channel, String pseudonym, int reqType, String body) {

        TcpStep2ReqBody reqBody = JSON.parseObject(body, TcpStep2ReqBody.class);

        String originPseudonym = reqBody.getOriginPseudonym();
        if (originPseudonym == null || originPseudonym.length() == 0) {
            throw new ArgotException(ArgotErrorCode.PSEUDONYM_NULL, "originPseudonym is NULL.");
        }

        BanditEntity originBanditEntity = pseudonymMap.get(originPseudonym);

        PipeBodyAuto pipeBody = new PipeBodyAuto(PipeBodyMsg.SEND_TO_LIST);
        if (originBanditEntity == null) {

            TcpStep1ResFromBody resFromBody = new TcpStep1ResFromBody();
            resFromBody.setStepResult(ApiConstants.ERROR);
            pipeBody.addMessageBody(pseudonym, ArgotType.RES_TCP_STEP_2_FROM, JSON.toJSONString(resFromBody));
            return pipeBody;
        }


        //pseudonym 是申请者，被同意者，发起连接者。rabblePseudonym是被申请者，同意者，被连接者
        String key = originPseudonym + SPLIT + pseudonym;
        TcpEntity tcpEntity = rabbleMap.get(key);

        if (tcpEntity == null) {

            TcpStep1ResFromBody resFromBody = new TcpStep1ResFromBody();
            resFromBody.setStepResult(ApiConstants.ERROR);
            pipeBody.addMessageBody(pseudonym, ArgotType.RES_TCP_STEP_2_FROM, JSON.toJSONString(resFromBody));
            return pipeBody;

        }

//        String originHost = tcpEntity.getOriginHost();
//        int originPort = tcpEntity.getOriginPort();
//
//        InetSocketAddress rabbleAddress = (InetSocketAddress) channel.remoteAddress();
//        String rabbleHost = tcpEntity.getRabbleHost();
//        int rabblePort = tcpEntity.getRabblePort();


        TcpStep2ResFromBody resFromBody = new TcpStep2ResFromBody();
        resFromBody.setStepResult(ApiConstants.SUCCESS);
        resFromBody.setOriginPseudonym(originPseudonym);
        pipeBody.addMessageBody(pseudonym, ArgotType.RES_TCP_STEP_2_FROM, JSON.toJSONString(resFromBody));


        TcpStep2ResToBody resToBody = new TcpStep2ResToBody();
        resToBody.setRabblePseudonym(pseudonym);
        pipeBody.addMessageBody(originPseudonym, ArgotType.RES_TCP_STEP_2_TO, JSON.toJSONString(resToBody));

        return pipeBody;


    }

}
