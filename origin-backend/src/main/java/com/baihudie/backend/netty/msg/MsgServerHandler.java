package com.baihudie.backend.netty.msg;

import com.baihudie.api.proto.SubscribeReqProto;
import com.baihudie.api.proto.SubscribeRespProto;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class MsgServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        SubscribeReqProto.SubscribeReq req = (SubscribeReqProto.SubscribeReq) msg;
        if ("wyk".equalsIgnoreCase(req.getUserName())) {
            System.out.println("Service accepts client subscribe req : [" +
                    req.toString() + "]");
            ctx.writeAndFlush(resp(req.getSubReqID()));
        }
    }

    // 生成返回报文
    private SubscribeRespProto.SubscribeResp resp(int subReqId) {
        SubscribeRespProto.SubscribeResp.Builder builder =
                SubscribeRespProto.SubscribeResp.newBuilder();
        builder.setSubReqID(subReqId);
        builder.setRespCode(0);
        builder.setDesc("Books order succeed, send to the addresses 3 days later.");
        return builder.build();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}