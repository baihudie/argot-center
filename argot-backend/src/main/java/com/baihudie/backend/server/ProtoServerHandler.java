//package com.baihudie.backend.server;
//
//import com.baihudie.api.constants.ArgotType;
//import com.baihudie.api.proto.ArgotReqProto;
//import com.baihudie.api.proto.ArgotResProto;
//import com.baihudie.backend.constants.ArgotErrorCode;
//import com.baihudie.api.exception.ArgotException;
//import com.baihudie.backend.pipe.PipeHandlerDispatcher;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelHandler;
//import io.netty.channel.ChannelHandlerContext;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@ChannelHandler.Sharable
//public class ProtoServerHandler extends ServerHandler {
//
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg)
//            throws Exception {
//
//        log.info("channel read :" + msg.toString());
//
//        ArgotReqProto.ArgotReq req = (ArgotReqProto.ArgotReq) msg;
//        int reqType = req.getReqType();
//
//        String pseudonym = req.getPseudonym();
//
//        String body = req.getBody();
//
//        channelRead(ctx, reqType, pseudonym, body);
//
//    }
//
//
//    protected void sendExceptionMessage(String pseudonym, Channel channel, Exception ex) {
//
//        try {
//            if (channel == null) {
//                return;
//            }
//
//            Integer seq = getNextSeq(channel);
//
//            int resCode = ArgotErrorCode.SYS_ERROR;
//            String messsage = ex.getMessage();
//
//            if (ex instanceof ArgotException) {
//                ArgotException aex = (ArgotException) ex;
//                resCode = aex.getErrorCode();
//                messsage = aex.getMessage();
//            }
//
//            ArgotResProto.ArgotRes.Builder builder = ArgotResProto.ArgotRes.newBuilder();
//
//            builder.setResSeq(seq);
//            builder.setResCode(resCode);
//            builder.setResMsg(messsage);
//            builder.setResType(ArgotType.RES_ARGOT_ERROR);
//
//            ArgotResProto.ArgotRes res = builder.build();
//            log.info("channel write and flush: " + res.toString());
//            channel.writeAndFlush(res);
//
//        } catch (Exception exception) {
//
//            log.error("sendExceptionMessage to pseudonym:" + channel + ", ERR:" + ex.getMessage(), ex);
//        }
//
//    }
//
//
//    protected void sendResTo(String pseudonyms, Channel channel, int resType, String body) {
//        try {
//            if (channel == null) {
//                return;
//            }
//
//            Integer seq = getNextSeq(channel);
//
//            ArgotResProto.ArgotRes.Builder builder = ArgotResProto.ArgotRes.newBuilder();
//
//            builder.setResSeq(seq);
//            builder.setResCode(ArgotErrorCode.SUCCESS);
//            builder.setResType(resType);
//            builder.setBody(body);
//
//            ArgotResProto.ArgotRes res = builder.build();
//            log.info("channel write and flush: " + res.toString());
//            channel.writeAndFlush(res);
//
//        } catch (Exception ex) {
//
//            log.error("chatAll to pseudonym:" + channel + ", ERR:" + ex.getMessage(), ex);
//        }
//    }
//
//
//}