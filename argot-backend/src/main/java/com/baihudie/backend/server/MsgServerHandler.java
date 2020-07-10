package com.baihudie.backend.server;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.proto.ArgotReqProto;
import com.baihudie.api.proto.ArgotResProto;
import com.baihudie.api.proto.body.ReqActiveBody;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.entity.ControlBody;
import com.baihudie.backend.entity.MessageBody;
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
    private MsgBodyService msgBodyService;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {

        log.info("channel read :" + msg.toString());

        ArgotReqProto.ArgotReq req = (ArgotReqProto.ArgotReq) msg;

        int reqType = req.getReqType();
        if (ArgotType.REQ_ACTIVE == reqType) {

            routeConActive(ctx, req);


        } else if (reqType > ArgotType.MAX_REQ_CON) {

            routeMsg(ctx, req);
        }

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

        MessageBody messageBody = null;

        try {
            messageBody = msgBodyService.genMessageBody(pseudonym, req.getReqType(), req.getBody());

        } catch (Exception ex) {
            log.error("message from pseudonym:" + pseudonym + "genMessageBody req:" + req + " , ERR:" + ex.getMessage(), ex);
            return;
        }


        int sendTo = messageBody.getSendTo();
        if (sendTo == MessageBody.TO_NULL) {

            return;
        } else if (sendTo == MessageBody.TO_SELF) {

            sendMesTo(pseudonym, channel, messageBody.getResType(), messageBody.getBody());

        } else if (sendTo == MessageBody.TO_ONE) {

            String pseudonymsOne = messageBody.getPseudonymsOne();
            Channel channelOne = channelMap.get(pseudonymsOne);
            sendMesTo(pseudonymsOne, channelOne, messageBody.getResType(), messageBody.getBody());

        } else if (sendTo == MessageBody.TO_LIST) {

            List<String> pseudonymsList = messageBody.getPseudonymsList();
            if (pseudonymsList != null && pseudonymsList.size() > 0) {
                for (String pseudonymsOne : pseudonymsList) {
                    Channel channelOne = channelMap.get(pseudonymsOne);
                    sendMesTo(pseudonymsOne, channelOne, messageBody.getResType(), messageBody.getBody());
                }
            }
        } else if (sendTo == MessageBody.TO_ALL) {
            Set<Map.Entry<String, Channel>> entrySet = channelMap.entrySet();
            for (Map.Entry<String, Channel> entry : entrySet) {
                String pseudonymsOne = entry.getKey();
                Channel channelOne = entry.getValue();

                sendMesTo(pseudonymsOne, channelOne, messageBody.getResType(), messageBody.getBody());
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

    private void routeConActive(ChannelHandlerContext ctx, ArgotReqProto.ArgotReq req) {

        int seq = req.getReqSeq();
        String body = req.getBody();

        //validate

        Channel channel = ctx.channel();
        if (channelMap.containsValue(ctx.channel())) {
            closeCtx(ctx);
            return;
        }

        Attribute<String> attrPseudonym = ctx.attr(ATTR_KEY_PSEUDONYM);
        Attribute<Integer> attrSeq = ctx.attr(ATTR_KEY_SEQ);

        try {

            ReqActiveBody reqBody = JSON.parseObject(body, ReqActiveBody.class);
            ControlBody controlBody = msgBodyService.genResActiveBody(reqBody);

            String pseudonym = controlBody.getControlMsg();

            channelMap.put(pseudonym, channel);
            attrPseudonym.set(pseudonym);
            attrSeq.set(0);

            ArgotResProto.ArgotRes.Builder builder = ArgotResProto.ArgotRes.newBuilder();

            builder.setResSeq(attrSeq.get());
            builder.setResCode(ArgotErrorCode.SUCCESS);
            builder.setResType(ArgotType.RES_ACTIVE);
            builder.setBody(controlBody.getBody());

            ArgotResProto.ArgotRes res = builder.build();

            log.info("channel write and flush: " + res.toString());
            ctx.writeAndFlush(res);

        } catch (Exception ex) {
            log.error("REQ_ACTIVE error:" + req + ", ERR:" + ex.getMessage(), ex);
            closeCtx(ctx);
            return;
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
            msgBodyService.removePseudonym(pseudonym);
        }
        ctx.close();
    }


}