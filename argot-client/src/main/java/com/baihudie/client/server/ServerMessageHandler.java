package com.baihudie.client.server;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.msg.ServerReq;
import com.baihudie.api.msg.ServerRes;
import com.baihudie.api.body.*;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.utils.ApiConstants;
import com.baihudie.client.ClientEndPoint;
import com.baihudie.client.entity.ClientTcpEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.base64.Base64;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Data
public class ServerMessageHandler extends ChannelInboundHandlerAdapter {

    private ClientEndPoint clientEndPoint;

    public static final int INIT = 0;
    public static final int SERVER_ACTIVE = 1;

    private int index = 0;
    private String pseudonym;
    private String banditCode;
    private int status;

    private ServerBodyHandler serverBodyHandler;

    public ServerMessageHandler(String banditCode, String goodName) {
        this.banditCode = banditCode;
        status = INIT;
        serverBodyHandler = new ServerBodyHandler(banditCode, goodName);
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

            if (ArgotType.RES_ACTIVE == resType) {

                needClose = true;
                String pseudonym = serverBodyHandler.handleResActive(body);

                this.pseudonym = pseudonym;
                this.status = SERVER_ACTIVE;

            } else if (ArgotType.RES_QUERY_ALL == resType) {

                String content = serverBodyHandler.handleResQueryAll(body);
                clientEndPoint.outputContent(content);

            } else if (ArgotType.RES_CHATS == resType) {

                String content = serverBodyHandler.handleResChats(body);
                clientEndPoint.outputContent(content);

            } else if (ArgotType.RES_INVITE_FROM == resType) {

                String content = serverBodyHandler.handleInviteFrom(body);
                clientEndPoint.outputContent(content);

            } else if (ArgotType.RES_INVITE_TO == resType) {

                String content = serverBodyHandler.handleInviteTo(body);
                clientEndPoint.outputContent(content);
            } else if (ArgotType.RES_ACCEPT_FROM == resType) {

                String content = serverBodyHandler.handleAcceptFrom(body);
                clientEndPoint.outputContent(content);

            } else if (ArgotType.RES_ACCEPT_TO == resType) {

                //进入自动化流程
                String content = serverBodyHandler.handleAcceptTo(body);
                clientEndPoint.outputContent(content);

            } else if (ArgotType.RES_TCP_STEP_1_FROM == resType) {

                int result = serverBodyHandler.handleTcpStep1From(body);

            } else if (ArgotType.RES_TCP_STEP_1_TO == resType) {

                int result = serverBodyHandler.handleTcpStep1To(body);
                if (result != ApiConstants.SUCCESS) {
                    return;
                }

                tcpStep2(ctx, body);

            } else if (ArgotType.RES_TCP_STEP_2_FROM == resType) {

                ClientTcpEntity bTcpEntity = serverBodyHandler.handleTcpStep2From(body);
                if (bTcpEntity == null) {
                    return;
                }

                log.info("LOG ======== START STEP 4");

                bTcpEntity.setArgotType(ArgotType.REQ_TCP_STEP_4);
                int result = clientEndPoint.createSocket(bTcpEntity);

                log.info("LOG ======== START STEP 4 RESULT:" + result);

            } else if (ArgotType.RES_TCP_STEP_2_TO == resType) {

                ClientTcpEntity aTcpEntity = serverBodyHandler.handleTcpStep2To(body);
                if (aTcpEntity == null) {
                    return;
                }
                log.info("LOG ======== STEP CREATE SOCKET");

                aTcpEntity.setArgotType(ArgotType.REQ_TCP_STEP_3);
                int result = clientEndPoint.createSocket(aTcpEntity);

                String resultStr = "ERROR";
                if (result == 0) {
                    resultStr = "SUCCESS";
                }
                log.info("LOG ======== STEP CREATE SOCKET: " + resultStr);


            } else if (ArgotType.RES_TCP_STEP_3 == resType) {

                ClientTcpEntity aTcpEntity = serverBodyHandler.handleTcpStep3(body);
                if (aTcpEntity == null) {

                    log.info("LOG ======== STEP RECEIVE 3 RESULT: ERROR");
                    return;
                }

                log.info("LOG ======== STEP RECEIVE 3 RESULT: SUCCESS");
                int result = clientEndPoint.handleTcpStep34(aTcpEntity);

                String resultStr = "ERROR";
                if (result == 0) {
                    resultStr = "SUCCESS";
                }
                log.info("LOG ======== STEP FRIEND TCP RESULT: " + resultStr);


            } else if (ArgotType.RES_TCP_STEP_4 == resType) {

                ClientTcpEntity bTcpEntity = serverBodyHandler.handleTcpStep4(body);
                if (bTcpEntity == null) {

                    log.info("LOG ======== STEP RECEIVE 3 RESULT: ERROR");
                    return;
                }

                log.info("LOG ======== STEP RECEIVE 3 RESULT: SUCCESS");
                int result = clientEndPoint.handleTcpStep34(bTcpEntity);

                String resultStr = "ERROR";
                if (result == 0) {
                    resultStr = "SUCCESS";
                }
                log.info("LOG ======== STEP FRIEND TCP RESULT: " + resultStr);

            }
        } catch (
                Exception ex) {
            log.error("READ ERROR:" + ex.getMessage(), ex);
            if (needClose) {
                ctx.close();
            }
        }

    }

    public void tcpStep1(String bBanditCode, Channel channel) {
        TcpStep1Body.TcpStep1ReqBody reqBody = serverBodyHandler.genReqTcpStep1Body(bBanditCode);
        sendMessage(JSON.toJSONString(reqBody), channel, ArgotType.REQ_TCP_STEP_1);
    }

    private void tcpStep2(ChannelHandlerContext ctx, String body) {
        TcpStep2Body.TcpStep2ReqBody reqBody = serverBodyHandler.genReqTcpStep2Body(body);
        sendMessage(JSON.toJSONString(reqBody), ctx.channel(), ArgotType.REQ_TCP_STEP_2);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        ActiveBody.ActiveReqBody body = serverBodyHandler.genReqActiveBody();
        sendMessage(JSON.toJSONString(body), ctx.channel(), ArgotType.REQ_ACTIVE);
    }

    public void chats(String content, Channel channel) throws IOException {

        ChatsBody.ChatsReqBody body = serverBodyHandler.genReqChatsBody(content);
        sendMessage(JSON.toJSONString(body), channel, ArgotType.REQ_CHATS);
    }

    public void queryAll(Channel channel) {

        QueryAllBody.QueryAllReqBody body = serverBodyHandler.genReqQueryAllBody();
        sendMessage("", channel, ArgotType.REQ_QUERY_ALL);

    }

    public void invite(String rabblePseudonym, String notes, Channel channel) {

        if (notes == null || notes.length() == 0) {
            notes = "hi";
        }
        InviteBody.InviteReqBody body = serverBodyHandler.genReqInviteBody(rabblePseudonym, notes);
        sendMessage(JSON.toJSONString(body), channel, ArgotType.REQ_INVITE);
    }


    public void accept(String aPseudonym, Channel channel) {

        AcceptBody.AcceptReqBody body = serverBodyHandler.genReqAcceptBody(aPseudonym);
        sendMessage(JSON.toJSONString(body), channel, ArgotType.REQ_ACCEPT);
    }


    public void friends() {

        String content = serverBodyHandler.friends();
        clientEndPoint.outputContent(content);

    }


    private void sendMessage(String body, Channel channel, int reqType) {

        ServerReq req = new ServerReq();

        index++;
        req.setReqSeq(index);
        if (pseudonym != null) {
            req.setPseudonym(pseudonym);
        }
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
        System.exit(0);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
        ctx.close();
        System.exit(0);
    }


}
