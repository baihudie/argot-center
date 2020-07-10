package com.baihudie.backend.netty.msg;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.proto.ArgotReqProto;
import com.baihudie.api.proto.ArgotResProto;
import com.baihudie.api.proto.body.ReqActiveBody;
import com.baihudie.api.proto.body.ReqChatAllBody;
import com.baihudie.backend.service.MsgClientService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class MsgClientHandler extends ChannelInboundHandlerAdapter {

    private int STATUS_INIT = 0;
    private int STATUS_ACTIVE = 1;

    private int index = 0;
    private String pseudonym;
    private int status;
    private ArgotReqProto.ArgotReq.Builder builder;

    private MsgClientService msgClientService;

    public MsgClientHandler(String banditCode) {
        status = STATUS_INIT;

        builder = ArgotReqProto.ArgotReq.newBuilder();

        msgClientService = new MsgClientService(banditCode);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        builder.setReqSeq(index);
        builder.setReqType(ArgotType.REQ_ACTIVE);

        ReqActiveBody body = msgClientService.genReqActiveBody();

        builder.setBody(JSON.toJSONString(body));

        ArgotReqProto.ArgotReq req = builder.build();
        log.info("channel write and flush: "+req.toString());
        ctx.write(req);
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.exit(0);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        log.info("Receive server response : [" + msg + "]");
        try {
            ArgotResProto.ArgotRes res = (ArgotResProto.ArgotRes) msg;
            int resType = res.getResType();
            if (ArgotType.RES_ACTIVE == resType) {

                String pseudonym = msgClientService.handleResActive(res);
                this.pseudonym = pseudonym;
                this.status = STATUS_ACTIVE;

            } else if (ArgotType.RES_CHAT_ALL == resType) {

                msgClientService.handleResChat(res);
            }
        } catch (Exception ex) {
            log.error("ACTIVE ERROR:" + ex.getMessage(), ex);
            ctx.close();
        }
    }




    public void chat(String content, Channel channel) throws IOException {
        if (status == STATUS_INIT) {
            log.info("status is STATUS_INIT");
            return;
        }

        index++;
        builder.setReqSeq(index);
        builder.setPseudonym(pseudonym);
        builder.setReqType(ArgotType.REQ_CHAT_ALL);

        ReqChatAllBody body = msgClientService.genReqChatBody(content);

        builder.setBody(JSON.toJSONString(body));
        ArgotReqProto.ArgotReq req = builder.build();

        log.info("channel write and flush: "+req.toString());
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

}
