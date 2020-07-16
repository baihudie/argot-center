package com.baihudie.client.friend;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.body.TestBody;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.exception.ArgotException;
import com.baihudie.api.utils.ApiConstants;
import com.baihudie.client.constants.ArgotErrorCodeClient;
import com.baihudie.client.entity.ClientTcpEntity;
import com.baihudie.client.msg.ClientMsg;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.base64.Base64;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Slf4j
public class FriendClientMessageHandler extends ChannelInboundHandlerAdapter {

    private int index = 0;
    private FriendChannel friendChannel;
    private ClientTcpEntity aTcpEntity;
    private FriendClientBodyHandler friendClientBodyHandler;

    public FriendClientMessageHandler(ClientTcpEntity aTcpEntity, FriendChannel friendChannel) {

        this.friendChannel = friendChannel;
        this.aTcpEntity = aTcpEntity;
        friendClientBodyHandler = new FriendClientBodyHandler();

    }


    public void initClientChannel(Channel clientChannel) throws InterruptedException {
        int argotType = aTcpEntity.getArgotType();
        String remoteIp = null;
        int remotePort = 0;

        boolean start = false;
        if (argotType == ArgotType.REQ_TCP_STEP_3) {
            remoteIp = aTcpEntity.getBRemoteIp();
            remotePort = aTcpEntity.getBRemotePort();
            start = true;
        } else if (argotType == ArgotType.REQ_TCP_STEP_4) {
            remoteIp = aTcpEntity.getARemoteIp();
            remotePort = aTcpEntity.getARemotePort();
        }

        SocketAddress remoteAddress = new InetSocketAddress(remoteIp, remotePort);
        ChannelFuture channelFuture = clientChannel.connect(remoteAddress).sync();
        if (channelFuture.isSuccess()) {
            Thread testThread = new Thread() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            TestBody.TestReqBody reqBody = new TestBody.TestReqBody();

                            reqBody.setContent("hello kitty, " + index++);
                            String body = JSON.toJSONString(reqBody);
                            sendMessage(body, clientChannel, ArgotType.REQ_TEST);

                            Thread.sleep(1000);
                        }
                    } catch (Exception ex) {
                        log.error("ERROR:" + ex.getMessage(), ex);
                    }
                }
            };
            testThread.start();
        } else {
            throw new ArgotException(ArgotErrorCodeClient.CLI_ERROR_007, "CLI_ERROR_007");

        }

    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        log.info("Receive server response : [" + msg + "]");
        boolean needClose = false;
        try {

//            ArgotResProto.ArgotRes res = (ArgotResProto.ArgotRes) msg;
            ClientMsg clientMsg = JSON.parseObject((String) msg, ClientMsg.class);

            int msgType = clientMsg.getMsgType();
            String body = clientMsg.getBody();
            if (ArgotType.REQ_TEST == msgType) {

                String content = friendClientBodyHandler.handleReqTestBody(body);

                friendChannel.outputContent(content);
            }

        } catch (Exception ex) {
            log.error("READ ERROR:" + ex.getMessage(), ex);
            if (needClose) {
                ctx.close();
            }
        }
    }


    private void sendMessage(String body, Channel channel, int msgType) {

        ClientMsg msg = new ClientMsg();

        index++;
        msg.setSeq(index);
//        if (pseudonym != null) {
//            req.setPseudonym(pseudonym);
//        }
        msg.setMsgType(msgType);
        msg.setBody(body);

        String msgBody = JSON.toJSONString(msg);
        log.info("SEND: " + msgBody);

//        ByteBuf bodyBuf = Unpooled.buffer(reqBody.length());
//        bodyBuf.writeBytes(reqBody.getBytes());
//        channel.writeAndFlush(bodyBuf);

        ByteBuf bodyBuf = Base64.encode(Unpooled.buffer().writeBytes(msgBody.getBytes()));
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
        friendChannel.clientChannelClose();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
        ctx.close();
        friendChannel.clientChannelClose();
    }

}
