package com.baihudie.backend.client;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.proto.ArgotResProto;
import com.baihudie.api.proto.body.*;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.constants.ArgotException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ClientProxy {

    private String banditCode;
    private String pseudonym;
    private String goodName;

    private Map<String, Integer> inviteFromMap = new ConcurrentHashMap<>();//发出申请MAP
    private Map<String, Integer> inviteToMap = new ConcurrentHashMap<>();//接收申请MAP

    private static final Integer INVITE_APPLY_FROM = 1; //向某人申请。
    private static final Integer INVITE_APPLY_TO = 2; // 接到某人申请
    private static final Integer CONN_START = 3;


    public ClientProxy(String banditCode, String goodName) {

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

    public InviteApplyReqBody genReqConApplyBody(String toPseudonym, String notes) {
        InviteApplyReqBody body = new InviteApplyReqBody();
        body.setToPseudonym(toPseudonym);
        body.setNotes(notes);

        inviteFromMap.put(toPseudonym, INVITE_APPLY_FROM);

        return body;
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
        log.info("handleResChats:" + resBody);
        log.info("pseudonymList:" + pseudonymList);
    }

    public void handleResChats(ArgotResProto.ArgotRes res) {
        String body = res.getBody();
        ChatsResBody resBody = JSON.parseObject(body, ChatsResBody.class);
        String content = resBody.getContent();
        String pseudonym = resBody.getPseudonym();

        log.info("handleResChats:" + resBody);
        log.info("[" + pseudonym + "]:" + content);
    }

    public void handleInviteFrom(ArgotResProto.ArgotRes res) {
        String body = res.getBody();
        InviteApplyFromResBody resBody = JSON.parseObject(body, InviteApplyFromResBody.class);

        String toPseudonym = resBody.getToPseudonym();
        int inviteResult = resBody.getInviteResult();
        if (inviteResult == InviteApplyFromResBody.RESULT_NOT_SET) {
            inviteFromMap.remove(toPseudonym);
        }

        log.info("handleInviteFrom:" + resBody);

    }

    public void handleInviteTo(ArgotResProto.ArgotRes res) {

        String body = res.getBody();
        InviteApplyToResBody resBody = JSON.parseObject(body, InviteApplyToResBody.class);
        String fromPseudonym = resBody.getFromPseudonym();
        String fromGoodName = resBody.getFromGoodName();
        String notes = resBody.getNotes();

        inviteToMap.put(fromPseudonym, INVITE_APPLY_TO);
        log.info("handleInviteTo:" + resBody);

    }


}
