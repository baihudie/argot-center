package com.baihudie.backend.client;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.proto.ArgotReqProto;
import com.baihudie.api.proto.ArgotResProto;
import com.baihudie.api.proto.body.ActiveReqBody;
import com.baihudie.api.proto.body.ChatsReqBody;
import com.baihudie.api.proto.body.InviteApplyReqBody;
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

    private ClientProxy clientProxy;

    public MsgClientHandler(String banditCode, String goodName) {

        status = STATUS_INIT;

        clientProxy = new ClientProxy(banditCode, goodName);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        ArgotReqProto.ArgotReq.Builder builder = ArgotReqProto.ArgotReq.newBuilder();

        builder.setReqSeq(index);
        builder.setReqType(ArgotType.REQ_ACTIVE);

        ActiveReqBody body = clientProxy.genReqActiveBody();

        builder.setBody(JSON.toJSONString(body));

        ArgotReqProto.ArgotReq req = builder.build();
        log.info("channel write and flush: " + req.toString());
        ctx.write(req);
        ctx.flush();
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

                clientProxy.handleInviteFrom(res);

            } else if (ArgotType.RES_INVITE_APPLY_TO == resType) {

                clientProxy.handleInviteTo(res);

            }
        } catch (Exception ex) {
            log.error("ACTIVE ERROR:" + ex.getMessage(), ex);
            if (needClose) {
                ctx.close();
            }
        }
    }


    public void chats(String content, Channel channel) throws IOException {

        ArgotReqProto.ArgotReq.Builder builder = ArgotReqProto.ArgotReq.newBuilder();
        index++;
        builder.setReqSeq(index);
        builder.setPseudonym(pseudonym);
        builder.setReqType(ArgotType.REQ_CHATS);

        ChatsReqBody body = clientProxy.genReqChatAllBody(content);

        builder.setBody(JSON.toJSONString(body));
        ArgotReqProto.ArgotReq req = builder.build();

        log.info("channel write and flush: " + req.toString());
        channel.writeAndFlush(req);
    }

    public void who(Channel channel) {

        ArgotReqProto.ArgotReq.Builder builder = ArgotReqProto.ArgotReq.newBuilder();
        index++;
        builder.setReqSeq(index);
        builder.setPseudonym(pseudonym);
        builder.setReqType(ArgotType.REQ_QUERY_ALL);

        ArgotReqProto.ArgotReq req = builder.build();

        log.info("channel write and flush: " + req.toString());
        channel.writeAndFlush(req);
    }

    public void inviteApply(String toPseudonym, String notes, Channel channel) {

        ArgotReqProto.ArgotReq.Builder builder = ArgotReqProto.ArgotReq.newBuilder();
        index++;
        builder.setReqSeq(index);
        builder.setPseudonym(pseudonym);
        builder.setReqType(ArgotType.REQ_INVITE_APPLY);

        if (notes == null || notes.length() == 0) {
            notes = "hi";
        }
        InviteApplyReqBody body = clientProxy.genReqConApplyBody(toPseudonym, notes);
        builder.setBody(JSON.toJSONString(body));

        ArgotReqProto.ArgotReq req = builder.build();

        log.info("channel write and flush: " + req.toString());
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
