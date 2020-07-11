package com.baihudie.backend.client;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.proto.ArgotResProto;
import com.baihudie.api.proto.body.*;
import com.baihudie.api.utils.ApiConstants;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.constants.ArgotException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Data
public class ServerProxy {

    private String banditCode;
    private String pseudonym;
    private String goodName;


    //发送者看这个MAP,rabblePseudonym
    private Map<String, Integer> rabbleMap = new ConcurrentHashMap<>();
    //接收这看这个MAP,originPseudonym
    private Map<String, Integer> originMap = new ConcurrentHashMap<>();

    private static final int INVITE_APPLY = 1; //向某人申请。
    private static final int INVITE_ACCEPT = 2; // 接到某人申请


    private static final Integer ACCEPT = 0;


    public ServerProxy(String banditCode, String goodName) {

        this.banditCode = banditCode;
        this.goodName = goodName;

    }

    ////////////////
    // handle req
    ////////////////

    public ActiveReqBody genReqActiveBody() {

        ActiveReqBody body = new ActiveReqBody();
        body.setBanditCode(banditCode);
        body.setGoodName(goodName);

        return body;
    }

    public ChatsReqBody genReqChatAllBody(String content) {

        ChatsReqBody body = new ChatsReqBody();
        body.setContent(content);
        return body;

    }

    public InviteApplyReqBody genReqInviteApplyBody(String rabblePseudonym, String notes) {

        if (rabblePseudonym.equals(pseudonym)) {
            throw new ArgotException(ArgotErrorCode.CLI_PSEUDONYM_APPLY_SELF, "PSEUDONYM_APPLY_SELF");
        }

        InviteApplyReqBody body = new InviteApplyReqBody();
        body.setRabblePseudonym(rabblePseudonym);
        body.setNotes(notes);

        rabbleMap.put(rabblePseudonym, INVITE_APPLY);

        return body;
    }

    public InviteAcceptReqBody genReqInviteAcceptBody(String acceptPseudonym) {

        Integer inviteStatus = originMap.get(acceptPseudonym);
        if (inviteStatus == null) {
            throw new ArgotException(ArgotErrorCode.CLI_PSEUDONYM_ACCEPT_NULL, "genReqInviteAcceptBody acceptPseudonym NOT INVITE");
        }

        if (inviteStatus == INVITE_ACCEPT) {

            InviteAcceptReqBody body = new InviteAcceptReqBody();

            body.setOriginPseudonym(acceptPseudonym);

            return body;
        } else {
            throw new ArgotException(ArgotErrorCode.CLI_PSEUDONYM_ACCEPT_STATUS_ERROR, "genReqInviteAcceptBody inviteStatus ERROR");
        }
    }

    public TcpStep1ReqBody genReqTcpStep1Body(String rabblePseudonym) {

        TcpStep1ReqBody tcpStep1ReqBody = new TcpStep1ReqBody();
        tcpStep1ReqBody.setRabblePseudonym(rabblePseudonym);

        return tcpStep1ReqBody;
    }

    public TcpStep2ReqBody genReqTcpStep2Body(String originPseudonym) {

        TcpStep2ReqBody tcpStep2ReqBody = new TcpStep2ReqBody();
        tcpStep2ReqBody.setOriginPseudonym(originPseudonym);

        return tcpStep2ReqBody;
    }


    ////////////////
    // handle res
    ////////////////

    public String handleResActive(ArgotResProto.ArgotRes res) {

        String body = res.getBody();
        ActiveResBody resBody = JSON.parseObject(body, ActiveResBody.class);
        String pseudonym = resBody.getPseudonym();
        this.pseudonym = pseudonym;

        if (pseudonym == null) {
            throw new ArgotException(ArgotErrorCode.CLI_PSEUDONYM_NULL, "PSEUDONYM is NULL");
        }
        return pseudonym;
    }

    public void handleResQueryAll(ArgotResProto.ArgotRes res) {

        String body = res.getBody();
        QueryAllResBody resBody = JSON.parseObject(body, QueryAllResBody.class);
        List<QueryAllResBodySub> pseudonymList = resBody.getPseudonymList();
        log.info("pseudonymList:" + pseudonymList);
    }

    public void handleResChats(ArgotResProto.ArgotRes res) {
        String body = res.getBody();
        ChatsResBody resBody = JSON.parseObject(body, ChatsResBody.class);
        String content = resBody.getContent();
        String pseudonym = resBody.getPseudonym();

        log.info("[" + pseudonym + "]:" + content);
    }

    public void handleInviteApplyFrom(ArgotResProto.ArgotRes res) {
        String body = res.getBody();
        InviteApplyFromResBody resBody = JSON.parseObject(body, InviteApplyFromResBody.class);

        String rabblePseudonym = resBody.getRabblePseudonym();
        int inviteResult = resBody.getInviteResult();
        if (inviteResult == ApiConstants.ERROR) {

            rabbleMap.remove(rabblePseudonym);
        }

    }

    public void handleInviteApplyTo(ArgotResProto.ArgotRes res) {

        String body = res.getBody();
        InviteApplyToResBody resBody = JSON.parseObject(body, InviteApplyToResBody.class);
        String originPseudonym = resBody.getOriginPseudonym();
        String originGoodName = resBody.getOriginGoodName();
        String notes = resBody.getNotes();

        originMap.put(originPseudonym, INVITE_ACCEPT);

    }

    public void handleInviteAcceptFrom(ArgotResProto.ArgotRes res) {

        String body = res.getBody();
        InviteAcceptFromResBody resBody = JSON.parseObject(body, InviteAcceptFromResBody.class);
        String originPseudonym = resBody.getOriginPseudonym();
        int inviteResult = resBody.getInviteResult();

        if (inviteResult == ApiConstants.ERROR) {

            originMap.remove(originPseudonym);
        }

    }


    public String handleInviteAcceptTo(ArgotResProto.ArgotRes res) {

        String body = res.getBody();
        InviteAcceptToResBody resBody = JSON.parseObject(body, InviteAcceptToResBody.class);

        String rabblePseudonym = resBody.getRabblePseudonym();
        Integer applyStatus = rabbleMap.get(rabblePseudonym);
        if (applyStatus == null) {
            return null;
        }

        int acceptStatus = resBody.getAcceptStatus();
        if (acceptStatus == ApiConstants.ERROR) {
            rabbleMap.remove(rabblePseudonym);
            return null;

        } else if (acceptStatus == ApiConstants.SUCCESS) {
            return rabblePseudonym;
        }
        return null;
    }


    public String handleTcpStep1To(ArgotResProto.ArgotRes res) {

        String body = res.getBody();
        TcpStep1ResToBody resBody = JSON.parseObject(body, TcpStep1ResToBody.class);

        String originPseudonym = resBody.getOriginPseudonym();

        Integer applyStatus = originMap.get(originPseudonym);
        if (applyStatus == null) {
            return null;
        }

        return originPseudonym;

    }


    public String handleTcpStep2To(ArgotResProto.ArgotRes res) {

        String body = res.getBody();
        TcpStep2ResToBody resBody = JSON.parseObject(body, TcpStep2ResToBody.class);

        String rabblePseudonym = resBody.getRabblePseudonym();

        Integer applyStatus = rabbleMap.get(rabblePseudonym);
        if (applyStatus == null) {
            return null;
        }

        return rabblePseudonym;

    }
}
