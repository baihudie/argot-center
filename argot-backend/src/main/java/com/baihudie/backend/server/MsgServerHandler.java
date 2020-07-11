package com.baihudie.backend.server;

import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.proto.ArgotReqProto;
import com.baihudie.api.proto.ArgotResProto;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.constants.ArgotException;
import com.baihudie.backend.pipe.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ChannelHandler.Sharable
public class MsgServerHandler extends ChannelInboundHandlerAdapter {

    private static final AttributeKey<String> ATTR_KEY_PSEUDONYM = AttributeKey.valueOf("pseudonym");
    private static final AttributeKey<Integer> ATTR_KEY_SEQ = AttributeKey.valueOf("seq");

    //pseudonym - channel
    private Map<String, Channel> channelMap = new ConcurrentHashMap<>(2000);

    @Autowired
    private PipeHandlerDispatcher bodyService;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {

        log.info("channel read :" + msg.toString());

        ArgotReqProto.ArgotReq req = (ArgotReqProto.ArgotReq) msg;
        int reqType = req.getReqType();

        if (reqType > ArgotType.MIN_REQ_CON
                && reqType < ArgotType.MAX_REQ_CON) {

            routeCon(ctx, req);


        } else if (reqType > ArgotType.MAX_REQ_CON) {

            routeMsg(ctx, req);
        }

    }


    private void routeCon(ChannelHandlerContext ctx, ArgotReqProto.ArgotReq req) {

//        int seq = req.getReqSeq();
//        String body = req.getBody();
        Channel channel = ctx.channel();

        Attribute<String> attrPseudonym = ctx.attr(ATTR_KEY_PSEUDONYM);
        Attribute<Integer> attrSeq = ctx.attr(ATTR_KEY_SEQ);


        String pseudonym = req.getPseudonym();

        PipeBodyCon handlerBody = null;

        try {
            handlerBody = bodyService.genConPipeBody(pseudonym, req.getReqType(), req.getBody());

        } catch (Exception ex) {
            log.error("REQ_CON error:" + req + ", ERR:" + ex.getMessage(), ex);
            closeCtx(ctx);
            return;
        }

        //控制平面
        int conType = handlerBody.getConType();
        if (conType == PipeBodyCon.CON_ACTIVE) {

            if (channelMap.containsValue(ctx.channel())) {
                closeCtx(ctx);
                return;
            }

            ControlBody controlBody = handlerBody.getControlBody();
            String newPseudonym = controlBody.getPseudonym();
            Attribute<String> channelAttr = ctx.attr(ATTR_KEY_PSEUDONYM);
            channelAttr.set(newPseudonym);

            channelMap.put(newPseudonym, channel);
            pseudonym = newPseudonym;
        }

        //信息平面
        int sendTo = handlerBody.getSendTo();
        List<MessageBody> messageBodyList = handlerBody.getMessageBodyList();
        sendMessageBodyList(pseudonym, channel, sendTo, messageBodyList);

    }

    private void routeMsg(ChannelHandlerContext ctx, ArgotReqProto.ArgotReq req) {
        Channel channel = ctx.channel();
        Attribute<String> channelAttr = ctx.attr(ATTR_KEY_PSEUDONYM);

        String pseudonym = req.getPseudonym();
        if (pseudonym == null) {
            closeCtx(ctx);
            return;
        }
        String attrPseudonym = channelAttr.get();
        if (attrPseudonym == null) {
            closeCtx(ctx);
            return;
        }
        if (!attrPseudonym.equals(pseudonym)) {
            closeCtx(ctx);
            return;
        }

        PipeBodyMsg handlerBody = null;
        int sendTo = PipeBodyMsg.SEND_TO_NULL;
        List<MessageBody> messageBodyList = null;

        try {
            handlerBody = bodyService.genMsgPipeBody(pseudonym, req.getReqType(), req.getBody());
            sendTo = handlerBody.getSendTo();
            messageBodyList = handlerBody.getMessageBodyList();

        } catch (Exception ex) {
            log.error("message from pseudonym:" + pseudonym + "genMessageBody req:" + req + " , ERR:" + ex.getMessage(), ex);
            sendExceptionMessage(pseudonym, channel, ex);
            return;
        }

        sendMessageBodyList(pseudonym, channel, sendTo, messageBodyList);
    }

    private void sendExceptionMessage(String pseudonym, Channel channel, Exception ex) {

        try {
            if (channel == null) {
                return;
            }

            Integer seq = getNextSeq(channel);

            int resCode = ArgotErrorCode.SYS_ERROR;
            String messsage = ex.getMessage();

            if (ex instanceof ArgotException) {
                ArgotException aex = (ArgotException) ex;
                resCode = aex.getErrorCode();
                messsage = aex.getMessage();
            }

            ArgotResProto.ArgotRes.Builder builder = ArgotResProto.ArgotRes.newBuilder();

            builder.setResSeq(seq);
            builder.setResCode(resCode);
            builder.setResMsg(messsage);
            builder.setResType(ArgotType.RES_ARGOT_ERROR);

            ArgotResProto.ArgotRes res = builder.build();
            log.info("channel write and flush: " + res.toString());
            channel.writeAndFlush(res);

        } catch (Exception exception) {

            log.error("sendExceptionMessage to pseudonym:" + channel + ", ERR:" + ex.getMessage(), ex);
        }

    }

    private void sendMessageBodyList(String pseudonym, Channel channel, int sendTo, List<MessageBody> messageBodyList) {

        if (sendTo == PipeBodyMsg.SEND_TO_NULL) {

            return;
        } else if (sendTo == PipeBodyMsg.SEND_TO_SELF) {

            if (messageBodyList != null && messageBodyList.size() > 0) {
                for (MessageBody messageBody : messageBodyList) {
                    sendMesTo(pseudonym, channel, messageBody.getResType(), messageBody.getBody());
                }
            }

        } else if (sendTo == PipeBodyMsg.SEND_TO_LIST) {

            if (messageBodyList != null && messageBodyList.size() > 0) {
                for (MessageBody messageBody : messageBodyList) {
                    String pseudonymOne = messageBody.getPseudonym();
                    Channel channelOne = channelMap.get(pseudonymOne);
                    sendMesTo(pseudonym, channelOne, messageBody.getResType(), messageBody.getBody());
                }
            }

        } else if (sendTo == PipeBodyMsg.SEND_TO_ALL) {

            if (messageBodyList != null && messageBodyList.size() > 0) {

                Set<Map.Entry<String, Channel>> entrySet = channelMap.entrySet();
                for (Map.Entry<String, Channel> entry : entrySet) {

                    String pseudonymOne = entry.getKey();
                    Channel channelOne = entry.getValue();

                    for (MessageBody messageBody : messageBodyList) {
                        sendMesTo(pseudonymOne, channelOne, messageBody.getResType(), messageBody.getBody());
                    }
                }
            }
        }

    }

    private void sendMesTo(String pseudonyms, Channel channel, int resType, String body) {
        try {
            if (channel == null) {
                return;
            }

            Integer seq = getNextSeq(channel);

            ArgotResProto.ArgotRes.Builder builder = ArgotResProto.ArgotRes.newBuilder();

            builder.setResSeq(seq);
            builder.setResCode(ArgotErrorCode.SUCCESS);
            builder.setResType(resType);
            builder.setBody(body);

            ArgotResProto.ArgotRes res = builder.build();
            log.info("channel write and flush: " + res.toString());
            channel.writeAndFlush(res);

        } catch (Exception ex) {

            log.error("chatAll to pseudonym:" + channel + ", ERR:" + ex.getMessage(), ex);
        }
    }


    private Integer getNextSeq(Channel oneChannel) {

        Attribute<Integer> attrSeq = oneChannel.attr(ATTR_KEY_SEQ);
        Integer seq = attrSeq.get();
        if (seq == null) {
            seq = 0;
        } else {
            seq++;
        }
        attrSeq.set(seq);
        return seq;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        closeCtx(ctx);
    }

    private void closeCtx(ChannelHandlerContext ctx) {

        Channel channel = ctx.channel();
        Attribute<String> channelAttr = ctx.attr(ATTR_KEY_PSEUDONYM);
        String pseudonym = channelAttr.get();
        if (pseudonym != null) {
            channelMap.remove(pseudonym);
            bodyService.removePseudonym(pseudonym);
        }
        ctx.close();
    }


}