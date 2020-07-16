package com.baihudie.backend.server;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.msg.ServerReq;
import com.baihudie.api.msg.ServerRes;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.utils.ApiConstants;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.api.exception.ArgotException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.base64.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ChannelHandler.Sharable
public class Base64ServerHandler extends ServerHandler {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {

        log.info("channel read :" + msg.toString());

        ServerReq req = JSON.parseObject((String) msg, ServerReq.class);

        int reqType = req.getReqType();
        String pseudonym = req.getPseudonym();
        String body = req.getBody();

        channelRead(ctx, reqType, pseudonym, body);
    }


    protected void sendExceptionMessage(String pseudonym, Channel channel, Exception ex) {
        try {
            if (channel == null) {
                return;
            }

            Integer seq = getNextSeq(channel);
            int resCode = ArgotErrorCode.SYS_ERROR;
            String messsage = ex.getMessage();

            if (ex instanceof ArgotException) {
                ArgotException aex = (ArgotException) ex;
                resCode = aex.getErrorCode();
                messsage = aex.getMessage();
            }

            ServerRes res = new ServerRes();

            res.setResSeq(seq);
            res.setResCode(resCode);
            res.setResMsg(messsage.substring(0, 500));
            res.setResType(ArgotType.RES_ARGOT_ERROR);

            channelWriteAndFlush(channel, res);

        } catch (Exception exception) {
            log.error("sendExceptionMessage pseudonym:" + pseudonym + " , ERR:" + ex.getMessage(), ex);
        }
    }

    protected void sendResTo(String pseudonym, Channel channel, int resType, String body) {
        try {
            if (channel == null) {
                return;
            }

            Integer seq = getNextSeq(channel);

            ServerRes res = new ServerRes();
            res.setResSeq(seq);
            res.setResCode(ArgotErrorCode.SUCCESS);
            res.setResType(resType);
            res.setBody(body);

            String resBody = JSON.toJSONString(res);

//            ByteBuf bodyBuf = Unpooled.buffer(resBody.length());
//            bodyBuf.writeBytes(resBody.getBytes());
//            channel.writeAndFlush(bodyBuf);

            ByteBuf bodyBuf = Base64.encode(Unpooled.buffer().writeBytes(resBody.getBytes()));
            channel.writeAndFlush(bodyBuf);

            ByteBuf delimiterBuf = Unpooled.buffer(ApiConstants.DELIMITER.length());
            delimiterBuf.writeBytes(ApiConstants.DELIMITER.getBytes());
            channel.writeAndFlush(delimiterBuf);


        } catch (Exception ex) {

            log.error("sendMesTo pseudonym:" + pseudonym + ", resType:" + resType + ", body:" + body + " , ERR:" + ex.getMessage(), ex);
        }
    }

    private void channelWriteAndFlush(Channel channel, ServerRes res) {

        String resBody = JSON.toJSONString(res);
        log.info("sendExceptionMessage: " + resBody);

        ByteBuf message = Unpooled.buffer(resBody.length() + ApiConstants.DELIMITER.length());

        message.writeBytes(resBody.getBytes());
        message.writeBytes(ApiConstants.DELIMITER.getBytes());

        channel.writeAndFlush(message);
    }

}
