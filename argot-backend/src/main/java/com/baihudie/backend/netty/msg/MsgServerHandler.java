package com.baihudie.backend.netty.msg;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.proto.*;
import com.baihudie.api.proto.body.ReqActiveBody;
import com.baihudie.api.proto.body.ReqChatAllBody;
import com.baihudie.api.proto.body.ResActiveBody;
import com.baihudie.api.proto.body.ResChatAllBody;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.service.MsgServerService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private MsgServerService msgServerService;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {

        log.info("channel read :" + msg.toString());

        ArgotReqProto.ArgotReq req = (ArgotReqProto.ArgotReq) msg;

        int reqType = req.getReqType();
        if (ArgotType.REQ_ACTIVE == reqType) {

            routeActive(ctx, req);
        } else if (ArgotType.REQ_CHAT_ALL == reqType) {

            routeChatAll(ctx, req);
        }
    }

    private void routeChatAll(ChannelHandlerContext ctx, ArgotReqProto.ArgotReq req) {
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


        Set<Map.Entry<String, Channel>> entrySet = channelMap.entrySet();
        for (Map.Entry<String, Channel> entry : entrySet) {
            String onePseudonym = entry.getKey();
            Channel oneChannel = entry.getValue();

            if (pseudonym.equals(onePseudonym)) {
                // DO NOTHING
            } else {
                try {

                    chatAll(req, pseudonym, oneChannel);

                } catch (Exception ex) {
                    log.error("chatAll to pseudonym:" + onePseudonym + ", ERR:" + ex.getMessage(), ex);
                }
            }
        }
    }

    private void chatAll(ArgotReqProto.ArgotReq req, String pseudonym, Channel oneChannel) {

        String body = req.getBody();
        ReqChatAllBody reqBody = JSON.parseObject(body, ReqChatAllBody.class);

        Integer seq = getNextSeq(oneChannel);

        ResChatAllBody resBody = msgServerService.genResChatAllBody(pseudonym, reqBody);
        ArgotResProto.ArgotRes.Builder builder = ArgotResProto.ArgotRes.newBuilder();


        builder.setResSeq(seq);
        builder.setResCode(ArgotErrorCode.SUCCESS);
        builder.setResType(ArgotType.RES_CHAT_ALL);
        builder.setBody(JSON.toJSONString(resBody));

        ArgotResProto.ArgotRes res = builder.build();
        log.info("channel write and flush: " + res.toString());
        oneChannel.writeAndFlush(res);

    }


    private void routeActive(ChannelHandlerContext ctx, ArgotReqProto.ArgotReq req) {
        int seq = req.getReqSeq();
        String body = req.getBody();

        Channel channel = ctx.channel();
        if (channelMap.containsValue(ctx.channel())) {
            closeCtx(ctx);
            return;
        }

        Attribute<String> attrPseudonym = ctx.attr(ATTR_KEY_PSEUDONYM);
        Attribute<Integer> attrSeq = ctx.attr(ATTR_KEY_SEQ);

        try {

            ReqActiveBody reqBody = JSON.parseObject(body, ReqActiveBody.class);
            ResActiveBody resBody = msgServerService.genResActiveBody(reqBody);

            String pseudonym = resBody.getPseudonym();

            if (channelMap.containsKey(pseudonym)) {

                channelMap.remove(pseudonym);
                msgServerService.removePseudonym(pseudonym);
                closeCtx(ctx);
                return;

            } else {

                channelMap.put(pseudonym, channel);
                attrPseudonym.set(pseudonym);
                attrSeq.set(0);

                ArgotResProto.ArgotRes.Builder builder = ArgotResProto.ArgotRes.newBuilder();

                builder.setResSeq(seq);
                builder.setResCode(ArgotErrorCode.SUCCESS);
                builder.setResType(ArgotType.RES_ACTIVE);
                builder.setBody(JSON.toJSONString(resBody));

                ArgotResProto.ArgotRes res = builder.build();

                log.info("channel write and flush: " + res.toString());
                ctx.write(res);
                ctx.flush();
            }

        } catch (Exception ex) {
            log.error("REQ_ACTIVE error:" + req + ", ERR:" + ex.getMessage(), ex);
            closeCtx(ctx);
            return;
        }
    }


    private void closeCtx(ChannelHandlerContext ctx) {

        Channel channel = ctx.channel();

        Attribute<String> channelAttr = ctx.attr(ATTR_KEY_PSEUDONYM);
        String pseudonym = channelAttr.get();
        if (pseudonym != null) {
            channelMap.remove(pseudonym);
            msgServerService.removePseudonym(pseudonym);
        }
        ctx.close();
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


}