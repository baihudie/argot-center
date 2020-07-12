package com.baihudie.backend.client;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.proto.ArgotReqProto;
import com.baihudie.api.proto.ArgotResProto;
import com.baihudie.api.proto.body.*;
import com.baihudie.api.utils.ApiConstants;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Data
public class MsgClientHandler extends ChannelInboundHandlerAdapter {

    public static final int STATUS_INIT = 0;
    public static final int STATUS_ACTIVE = 1;

    private int index = 0;
    private String pseudonym;
    private String banditCode;
    private int status;

    private ClientProxy clientProxy;
//    private P2pProxy p2pProxy;

    public MsgClientHandler(String banditCode, String goodName) {
        this.banditCode = banditCode;
        status = STATUS_INIT;

        clientProxy = new ClientProxy(banditCode, goodName);
//        p2pProxy = new P2pProxy(clientProxy);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        log.info("Receive server response : [" + msg + "]");
        boolean needClose = false;
        try {
            ArgotResProto.ArgotRes res = (ArgotResProto.ArgotRes) msg;
            int resType = res.getResType();
            String body = res.getBody();

            if (ArgotType.RES_ACTIVE == resType) {

                needClose = true;
                String pseudonym = clientProxy.handleResActive(body);

                this.pseudonym = pseudonym;
                this.status = STATUS_ACTIVE;

            } else if (ArgotType.RES_QUERY_ALL == resType) {

                clientProxy.handleResQueryAll(body);

            } else if (ArgotType.RES_CHATS == resType) {

                clientProxy.handleResChats(body);

            } else if (ArgotType.RES_INVITE_FROM == resType) {

                clientProxy.handleInviteApplyFrom(body);

            } else if (ArgotType.RES_INVITE_TO == resType) {

                clientProxy.handleInviteApplyTo(body);

            } else if (ArgotType.RES_ACCEPT_FROM == resType) {

                clientProxy.handleInviteAcceptFrom(body);

            } else if (ArgotType.RES_ACCEPT_TO == resType) {

                //进入自动化流程
                int result = clientProxy.handleInviteAcceptTo(body);
                if (result != ApiConstants.SUCCESS) {
                    return;
                }
                tcpStep1(ctx, body);

            } else if (ArgotType.RES_TCP_STEP_1_FROM == resType) {

                int result = clientProxy.handleTcpStep1From(body);

            } else if (ArgotType.RES_TCP_STEP_1_TO == resType) {

                int result = clientProxy.handleTcpStep1To(body);
                if (result != ApiConstants.SUCCESS) {
                    return;
                }
                tcpStep2(ctx, body);

            } else if (ArgotType.RES_TCP_STEP_2_FROM == resType) {

                int result = clientProxy.handleTcpStep2From(body);
                if (result != ApiConstants.SUCCESS) {
                    return;
                }
                log.info("LOG ===================== STEP 4");

            } else if (ArgotType.RES_TCP_STEP_2_TO == resType) {

                int result = clientProxy.handleTcpStep2To(body);
                if (result != ApiConstants.SUCCESS) {
                    return;
                }
                log.info("LOG ===================== STEP 3");

            }
        } catch (Exception ex) {
            log.error("READ ERROR:" + ex.getMessage(), ex);
            if (needClose) {
                ctx.close();
            }
        }
    }

    private void tcpStep1(ChannelHandlerContext cxt, String body) {

        TcpStep1ReqBody reqBody = clientProxy.genReqTcpStep1Body(body);
        sendMessage(JSON.toJSONString(reqBody), cxt.channel(), ArgotType.REQ_TCP_STEP_1);
    }

    private void tcpStep2(ChannelHandlerContext ctx, String body) {

        TcpStep2ReqBody reqBody = clientProxy.genReqTcpStep2Body(body);
        sendMessage(JSON.toJSONString(reqBody), ctx.channel(), ArgotType.REQ_TCP_STEP_2);

    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        ActiveReqBody body = clientProxy.genReqActiveBody();
        sendMessage(JSON.toJSONString(body), ctx.channel(), ArgotType.REQ_ACTIVE);
    }

    public void chats(String content, Channel channel) throws IOException {

        ChatsReqBody body = clientProxy.genReqChatsBody(content);
        sendMessage(JSON.toJSONString(body), channel, ArgotType.REQ_CHATS);
    }

    public void queryAll(Channel channel) {

        QueryAllReqBody body = clientProxy.genReqQueryAllBody();
        sendMessage("", channel, ArgotType.REQ_QUERY_ALL);
    }

    public void inviteApply(String rabblePseudonym, String notes, Channel channel) {

        if (notes == null || notes.length() == 0) {
            notes = "hi";
        }
        InviteReqBody body = clientProxy.genReqInviteApplyBody(rabblePseudonym, notes);
        sendMessage(JSON.toJSONString(body), channel, ArgotType.REQ_INVITE);
    }


    public void inviteAccept(String acceptPseudonym, Channel channel) {

        AcceptReqBody body = clientProxy.genReqInviteAcceptBody(acceptPseudonym);
        sendMessage(JSON.toJSONString(body), channel, ArgotType.REQ_ACCEPT);
    }


    private void sendMessage(String body, Channel channel, int reqType) {
        ArgotReqProto.ArgotReq.Builder builder = ArgotReqProto.ArgotReq.newBuilder();
        index++;
        builder.setReqSeq(index);
        if (pseudonym != null) {
            builder.setPseudonym(pseudonym);
        }
        builder.setReqType(reqType);
        builder.setBody(body);
        ArgotReqProto.ArgotReq req = builder.build();

        log.info("SEND: " + req.toString());
        channel.writeAndFlush(req);
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
