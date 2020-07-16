package com.baihudie.backend.server;


import com.baihudie.api.constants.ArgotType;
import com.baihudie.backend.pipe.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public abstract class ServerHandler extends ChannelInboundHandlerAdapter {

    protected static final AttributeKey<String> ATTR_KEY_PSEUDONYM = AttributeKey.valueOf("pseudonym");
    protected static final AttributeKey<Integer> ATTR_KEY_SEQ = AttributeKey.valueOf("seq");

    //pseudonym - channel
    protected Map<String, Channel> channelMap = new ConcurrentHashMap<>(2000);

    @Autowired
    protected PipeHandlerDispatcher pipeHandlerDispatcher;


    public void channelRead(ChannelHandlerContext ctx, int reqType, String pseudonym, String body)
            throws Exception {

        log.info("SERVER READ ==== " +
                "reqType:" + reqType + ", pseudonym:" + pseudonym + ", body" + body);

        if (reqType > ArgotType.REQ_CON_MIN
                && reqType < ArgotType.REQ_CON_MAX) {

            routeCon(ctx, reqType, pseudonym, body);

        } else if (reqType > ArgotType.REQ_CON_MAX
                && reqType < ArgotType.REQ_MSG_MAX
        ) {

            routeMsg(ctx, reqType, pseudonym, body);

        } else if (reqType > ArgotType.REQ_MSG_MAX) {
            //这里，都是新通道，不绑定p-code。
            routeAuto(ctx, reqType, pseudonym, body);
        }

    }


    private void routeCon(ChannelHandlerContext ctx, int reqType, String pseudonym, String body) {

        Channel channel = ctx.channel();

        Attribute<String> attrPseudonym = ctx.attr(ATTR_KEY_PSEUDONYM);
        Attribute<Integer> attrSeq = ctx.attr(ATTR_KEY_SEQ);

        PipeBodyCon handlerBody = null;

        try {
            handlerBody = pipeHandlerDispatcher.genConPipeBody(pseudonym, reqType, body);

        } catch (Exception ex) {
            log.error("REQ_CON error:" +
                    "reqType:" + reqType + ", pseudonym:" + pseudonym + ", body" + body +
                    ", ERR:" + ex.getMessage(), ex);
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

    private void routeMsg(ChannelHandlerContext ctx, int reqType, String pseudonym, String body) {

        Channel channel = ctx.channel();
        Attribute<String> channelAttr = ctx.attr(ATTR_KEY_PSEUDONYM);
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
            handlerBody = pipeHandlerDispatcher.genMsgPipeBody(pseudonym, reqType, body);
            sendTo = handlerBody.getSendTo();
            messageBodyList = handlerBody.getMessageBodyList();

        } catch (Exception ex) {
            log.error("REQ_CON error:" +
                    "reqType:" + reqType + ", pseudonym:" + pseudonym + ", body" + body +
                    ", ERR:" + ex.getMessage(), ex);
            sendExceptionMessage(pseudonym, channel, ex);
            return;
        }

        sendMessageBodyList(pseudonym, channel, sendTo, messageBodyList);
    }

    private void routeAuto(ChannelHandlerContext ctx, int reqType, String pseudonym, String body) {

        Channel channel = ctx.channel();

        PipeBodyAuto handlerBody = null;
        int sendTo = PipeBodyMsg.SEND_TO_NULL;
        List<MessageBody> messageBodyList = null;

        try {
            handlerBody = pipeHandlerDispatcher.genAutoPipeBody(channel, pseudonym, reqType, body);
            sendTo = handlerBody.getSendTo();
            messageBodyList = handlerBody.getMessageBodyList();

        } catch (Exception ex) {
            log.error("REQ_AUTO error:" +
                    "reqType:" + reqType + ", pseudonym:" + pseudonym + ", body" + body +
                    ", ERR:" + ex.getMessage(), ex);
            sendExceptionMessage(pseudonym, channel, ex);

            //出现异常，则直接进行关闭。
            closeCtx(ctx);

            return;
        }

        sendMessageBodyList(pseudonym, channel, sendTo, messageBodyList);

    }


    protected abstract void sendExceptionMessage(String pseudonym, Channel channel, Exception ex);


    private void sendMessageBodyList(String pseudonym, Channel channel, int sendTo, List<MessageBody> messageBodyList) {

        if (sendTo == PipeBodyMsg.SEND_TO_NULL) {

            return;
        } else if (sendTo == PipeBodyMsg.SEND_TO_SELF) {

            if (messageBodyList != null && messageBodyList.size() > 0) {
                for (MessageBody messageBody : messageBodyList) {
                    sendResTo(pseudonym, channel, messageBody.getResType(), messageBody.getBody());
                }
            }

        } else if (sendTo == PipeBodyMsg.SEND_TO_LIST) {

            if (messageBodyList != null && messageBodyList.size() > 0) {
                for (MessageBody messageBody : messageBodyList) {
                    String pseudonymOne = messageBody.getPseudonym();
                    Channel channelOne = channelMap.get(pseudonymOne);
                    sendResTo(pseudonym, channelOne, messageBody.getResType(), messageBody.getBody());
                }
            }

        } else if (sendTo == PipeBodyMsg.SEND_TO_ALL) {

            if (messageBodyList != null && messageBodyList.size() > 0) {

                Set<Map.Entry<String, Channel>> entrySet = channelMap.entrySet();
                for (Map.Entry<String, Channel> entry : entrySet) {

                    String pseudonymOne = entry.getKey();
                    Channel channelOne = entry.getValue();

                    for (MessageBody messageBody : messageBodyList) {
                        sendResTo(pseudonymOne, channelOne, messageBody.getResType(), messageBody.getBody());
                    }
                }
            }
        }

    }

    abstract protected void sendResTo(String pseudonyms, Channel channel, int resType, String body);


    protected Integer getNextSeq(Channel oneChannel) {

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
            pipeHandlerDispatcher.removePseudonym(pseudonym);
        }
        ctx.close();
    }

}
