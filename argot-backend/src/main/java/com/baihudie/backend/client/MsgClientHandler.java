package com.baihudie.backend.client;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.proto.ArgotReqProto;
import com.baihudie.api.proto.ArgotResProto;
import com.baihudie.api.proto.body.*;
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
    private int status;

    private ServerProxy clientProxy;
    private P2pProxy p2pProxy;

    public MsgClientHandler(String banditCode, String goodName) {

        status = STATUS_INIT;

        clientProxy = new ServerProxy(banditCode, goodName);

        p2pProxy = new P2pProxy(clientProxy);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        log.info("Receive server response : [" + msg + "]");
        boolean needClose = false;
        try {
            ArgotResProto.ArgotRes res = (ArgotResProto.ArgotRes) msg;
            int resType = res.getResType();
            if (ArgotType.RES_ACTIVE == resType) {

                needClose = true;
                String pseudonym = clientProxy.handleResActive(res);

                this.pseudonym = pseudonym;
                this.status = STATUS_ACTIVE;

            } else if (ArgotType.RES_QUERY_ALL == resType) {

                clientProxy.handleResQueryAll(res);

            } else if (ArgotType.RES_CHATS == resType) {

                clientProxy.handleResChats(res);

            } else if (ArgotType.RES_INVITE_APPLY_FROM == resType) {

                clientProxy.handleInviteApplyFrom(res);

            } else if (ArgotType.RES_INVITE_APPLY_TO == resType) {

                clientProxy.handleInviteApplyTo(res);

            } else if (ArgotType.RES_INVITE_ACCEPT_FROM == resType) {

                clientProxy.handleInviteAcceptFrom(res);

            } else if (ArgotType.RES_INVITE_ACCEPT_TO == resType) {

                //进入自动化流程

                String rabblePseudonym = clientProxy.handleInviteAcceptTo(res);
                if (rabblePseudonym != null && rabblePseudonym.length() > 0) {
                    //TCP连接
                    p2pProxy.initStep1(rabblePseudonym);
                    tcpStep1(ctx, rabblePseudonym);
                }

            } else if (ArgotType.RES_TCP_STEP_1_FROM == resType) {

                log.info(" create new SOCKET.");
                //TODO create new SOCKET.

            } else if (ArgotType.RES_TCP_STEP_1_TO == resType) {

                String originPseudonym = clientProxy.handleTcpStep1To(res);
                if (originPseudonym != null && originPseudonym.length() > 0) {
                    //TCP连接
                    p2pProxy.step1to(originPseudonym);
                    tcpStep2(ctx, originPseudonym);
                }

            } else if (ArgotType.RES_TCP_STEP_2_FROM == resType) {

                log.info(" create new SOCKET.");
                //TODO create new SOCKET.

            } else if (ArgotType.RES_TCP_STEP_2_TO == resType) {

                String rabblePseudonym = clientProxy.handleTcpStep2To(res);

                p2pProxy.step2to(rabblePseudonym);

            }
        } catch (Exception ex) {
            log.error("READ ERROR:" + ex.getMessage(), ex);
            if (needClose) {
                ctx.close();
            }
        }
    }

    private void tcpStep2(ChannelHandlerContext ctx, String originPseudonym) {
        TcpStep2ReqBody body = clientProxy.genReqTcpStep2Body(originPseudonym);
        sendMessage(JSON.toJSONString(body), ctx.channel(), ArgotType.REQ_TCP_STEP_2);

    }

    private void tcpStep1(ChannelHandlerContext cxt, String rabblePseudonym) {

        TcpStep1ReqBody body = clientProxy.genReqTcpStep1Body(rabblePseudonym);
        sendMessage(JSON.toJSONString(body), cxt.channel(), ArgotType.REQ_TCP_STEP_1);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        ActiveReqBody body = clientProxy.genReqActiveBody();
        sendMessage(JSON.toJSONString(body), ctx.channel(), ArgotType.REQ_ACTIVE);
    }

    public void chats(String content, Channel channel) throws IOException {

        ChatsReqBody body = clientProxy.genReqChatAllBody(content);
        sendMessage(JSON.toJSONString(body), channel, ArgotType.REQ_CHATS);
    }

    public void who(Channel channel) {

        sendMessage("", channel, ArgotType.REQ_QUERY_ALL);
    }

    public void inviteApply(String rabblePseudonym, String notes, Channel channel) {

        if (notes == null || notes.length() == 0) {
            notes = "hi";
        }
        InviteApplyReqBody body = clientProxy.genReqInviteApplyBody(rabblePseudonym, notes);
        sendMessage(JSON.toJSONString(body), channel, ArgotType.REQ_INVITE_APPLY);
    }


    public void inviteAccept(String acceptPseudonym, Channel channel) {

        InviteAcceptReqBody body = clientProxy.genReqInviteAcceptBody(acceptPseudonym);
        sendMessage(JSON.toJSONString(body), channel, ArgotType.REQ_INVITE_ACCEPT);
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
