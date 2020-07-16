package com.baihudie.backend.handler;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.body.TcpStep3Body;
import com.baihudie.api.body.TcpStep4Body;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.exception.ArgotException;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.entity.BanditEntity;
import com.baihudie.backend.entity.ConnSwitchEntity;
import com.baihudie.backend.pipe.PipeBodyAuto;
import com.baihudie.backend.pipe.PipeBodyMsg;
import com.baihudie.backend.pipe.PipeHandlerDispatcher;
import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
public class TcpStep4Handler extends PipeHandlerDispatcher {


    public PipeBodyAuto genPipeBodyAuto(Channel channel, String pseudonym, int reqType, String body) {

        TcpStep4Body.TcpStep4ReqBody reqBody = JSON.parseObject(body, TcpStep4Body.TcpStep4ReqBody.class);

        String token = reqBody.getToken();
        if (token == null || token.length() == 0) {
            throw new ArgotException(ArgotErrorCode.ERROR_0025, "rabblePseudonym is NULL.");
        }

        String aPseudonym = reqBody.getAPseudonym();
        if (aPseudonym == null || aPseudonym.length() == 0) {
            throw new ArgotException(ArgotErrorCode.ERROR_0025, "originPseudonym is NULL.");
        }

        String bPseudonym = reqBody.getBPseudonym();
        if (bPseudonym == null || bPseudonym.length() == 0) {
            throw new ArgotException(ArgotErrorCode.ERROR_0025, "originPseudonym is NULL.");
        }

        if (aPseudonym.equals(bPseudonym)) {
            throw new ArgotException(ArgotErrorCode.ERROR_0025, "rabblePseudonym is NULL.");
        }

        BanditEntity aBanditEntity = pseudonymMap.get(aPseudonym);
        if (aBanditEntity == null) {
            throw new ArgotException(ArgotErrorCode.ERROR_0025, "rabblePseudonym is NULL.");
        }

        BanditEntity bBanditEntity = pseudonymMap.get(bPseudonym);
        if (bBanditEntity == null) {
            throw new ArgotException(ArgotErrorCode.ERROR_0025, "rabblePseudonym is NULL.");
        }

        ConnSwitchEntity connSwitchEntity = connSwitchMap.get(token);
        if (connSwitchEntity == null) {
            throw new ArgotException(ArgotErrorCode.ERROR_0025, "rabblePseudonym is NULL.");
        }

        boolean valid = connSwitchEntity.validate(aBanditEntity, bBanditEntity);
        if (!valid) {
            throw new ArgotException(ArgotErrorCode.ERROR_0025, "rabblePseudonym is NULL.");
        }

        synchronized (connSwitchEntity) {

            channel.remoteAddress();
            InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();

            String bRemoteIp = remoteAddress.getAddress().getHostAddress();
            int bRemotePort = remoteAddress.getPort();
            connSwitchEntity.setBRemoteIp(bRemoteIp);
            connSwitchEntity.setBRemotePort(bRemotePort);

            int tcp3 = connSwitchEntity.getTcp3();
            if (tcp3 == ConnSwitchEntity.OK) {

                String aRemoteIp = connSwitchEntity.getARemoteIp();
                int aRemotePort = connSwitchEntity.getARemotePort();

                PipeBodyAuto pipeBody = new PipeBodyAuto(PipeBodyMsg.SEND_TO_LIST);

                //返回的通道，不是发消息的通道。
                TcpStep3Body.TcpStep3ResBody res3Body = new TcpStep3Body.TcpStep3ResBody();

                res3Body.setToken(token);
                // A
                res3Body.setAPseudonym(aPseudonym);

                res3Body.setARemoteIp(aRemoteIp);
                res3Body.setARemotePort(aRemotePort);

                // B
                res3Body.setBPseudonym(bPseudonym);

                res3Body.setBRemoteIp(bRemoteIp);
                res3Body.setBRemotePort(bRemotePort);

                pipeBody.addMessageBody(aPseudonym, ArgotType.RES_TCP_STEP_3, JSON.toJSONString(res3Body));


                TcpStep4Body.TcpStep4ResBody res4Body = new TcpStep4Body.TcpStep4ResBody();

                res4Body.setToken(token);
                // A
                res4Body.setAPseudonym(aPseudonym);

                res4Body.setARemoteIp(aRemoteIp);
                res4Body.setARemotePort(aRemotePort);

                // B
                res4Body.setBPseudonym(bPseudonym);

                res4Body.setBRemoteIp(bRemoteIp);
                res4Body.setBRemotePort(bRemotePort);

                pipeBody.addMessageBody(bPseudonym, ArgotType.RES_TCP_STEP_4, JSON.toJSONString(res4Body));

                connSwitchMap.remove(token);
                return pipeBody;
            } else {
                connSwitchEntity.setTcp4(ConnSwitchEntity.OK);
            }
        }

        PipeBodyAuto pipeBody = new PipeBodyAuto(PipeBodyMsg.SEND_TO_NULL);
        return pipeBody;

    }
}
