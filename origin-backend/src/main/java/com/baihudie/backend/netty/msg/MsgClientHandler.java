package com.baihudie.backend.netty.msg;

import com.baihudie.api.proto.SubscribeReqProto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class MsgClientHandler extends ChannelInboundHandlerAdapter {

//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        for (int i = 0; i < 3; i++) {
//            ctx.write(subReq(i));
//        }
//        ctx.flush();
//    }

    private SubscribeReqProto.SubscribeReq subReq(int i) {
        SubscribeReqProto.SubscribeReq.Builder builder =
                SubscribeReqProto.SubscribeReq.newBuilder();

        builder.setSubReqID(i);
        builder.setUserName("wyk");
        builder.setProductName("book");
        builder.addAddress("Beijing");
        builder.addAddress("Hebei");
        builder.addAddress("Shanghai");
        return builder.build();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        System.out.println("Receive server response : [" + msg + "]");
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
    }
}
