package com.baihudie.client.friend;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.msg.ServerReq;
import com.baihudie.api.msg.ServerRes;
import com.baihudie.api.body.TcpStep3Body;
import com.baihudie.api.body.TcpStep4Body;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.utils.ApiConstants;
import com.baihudie.client.entity.ClientTcpEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.base64.Base64;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class FriendServerMessageHandler extends ChannelInboundHandlerAdapter {

    private int index = 0;
    private FriendChannel friendChannel;
    private ClientTcpEntity clientTcpEntity;
    private FriendServerBodyHandler friendBodyHandler;

    public FriendServerMessageHandler(ClientTcpEntity aTcpEntity, FriendChannel friendChannel) {
        this.clientTcpEntity = aTcpEntity;
        this.friendChannel = friendChannel;
        friendBodyHandler = new FriendServerBodyHandler();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        log.info("Receive server response : [" + msg + "]");
        boolean needClose = false;
        try {

//            ArgotResProto.ArgotRes res = (ArgotResProto.ArgotRes) msg;
            ServerRes res = JSON.parseObject((String) msg, ServerRes.class);

            int resType = res.getResType();
            String body = res.getBody();

        } catch (Exception ex) {
            log.error("READ ERROR:" + ex.getMessage(), ex);
            if (needClose) {
                ctx.close();
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        int argotType = clientTcpEntity.getArgotType();

        if (argotType == ArgotType.REQ_TCP_STEP_3) {

            TcpStep3Body.TcpStep3ReqBody body = friendBodyHandler.genReqTcpStep3Body(clientTcpEntity);
            sendMessage(JSON.toJSONString(body), ctx.channel(), ArgotType.REQ_TCP_STEP_3);

        }else if(argotType == ArgotType.REQ_TCP_STEP_4) {

            TcpStep4Body.TcpStep4ReqBody body = friendBodyHandler.genReqTcpStep4Body(clientTcpEntity);
            sendMessage(JSON.toJSONString(body), ctx.channel(), ArgotType.REQ_TCP_STEP_4);
        }

    }


    private void sendMessage(String body, Channel channel, int reqType) {

        ServerReq req = new ServerReq();

        index++;
        req.setReqSeq(index);
//        if (pseudonym != null) {
//            req.setPseudonym(pseudonym);
//        }
        req.setReqType(reqType);
        req.setBody(body);

        String reqBody = JSON.toJSONString(req);
        log.info("SEND: " + reqBody);

//        ByteBuf bodyBuf = Unpooled.buffer(reqBody.length());
//        bodyBuf.writeBytes(reqBody.getBytes());
//        channel.writeAndFlush(bodyBuf);

        ByteBuf bodyBuf = Base64.encode(Unpooled.buffer().writeBytes(reqBody.getBytes()));
        channel.writeAndFlush(bodyBuf);

        ByteBuf delimiterBuf = Unpooled.buffer(ApiConstants.DELIMITER.length());
        delimiterBuf.writeBytes(ApiConstants.DELIMITER.getBytes());
        channel.writeAndFlush(delimiterBuf);

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
            throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.flush();
        ctx.close();
        friendChannel.serverChannelClose();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
        ctx.close();
        friendChannel.serverChannelClose();
    }

}
