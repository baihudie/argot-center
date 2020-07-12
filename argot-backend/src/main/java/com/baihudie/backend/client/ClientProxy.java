package com.baihudie.backend.client;

import com.alibaba.fastjson.JSON;
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
public class ClientProxy {


    private String banditCode;
    private String pseudonym;
    private String goodName;


    private Map<String, Integer> rabbleStepMap = new ConcurrentHashMap<>(); //A-1,2,6,7
    private Map<String, Integer> originStepMap = new ConcurrentHashMap<>(); //B-3,4,5,8

    private static final int STEP_APPLY_SEND = 1; //A
    private static final int STEP_APPLY_SEND_TO = 2; // B

    private static final int STEP_ACCEPT_SEND = 3; // B
    private static final int STEP_ACCEPT_SEND_TO = 4; // A

    private static final int STEP_TCP_1_SEND_TO = 5;//B

    private static final int STEP_TCP_2_SEND_BACK = 6;//B
    private static final int STEP_TCP_2_SEND_RECEIVE = 7;//A


    private static final Integer ACCEPT = 0;


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

    public String handleResActive(String body) {


        ActiveResBody resBody = JSON.parseObject(body, ActiveResBody.class);
        String pseudonym = resBody.getPseudonym();
        this.pseudonym = pseudonym;

        if (pseudonym == null) {
            throw new ArgotException(ArgotErrorCode.CLI_PSEUDONYM_NULL, "PSEUDONYM is NULL");
        }
        return pseudonym;
    }

    public ChatsReqBody genReqChatsBody(String content) {

        ChatsReqBody body = new ChatsReqBody();
        body.setContent(content);
        return body;

    }

    public void handleResChats(String body) {

        ChatsResBody resBody = JSON.parseObject(body, ChatsResBody.class);
        String content = resBody.getContent();
        String pseudonym = resBody.getPseudonym();

        log.info("[" + pseudonym + "]:" + content);
    }

    public QueryAllReqBody genReqQueryAllBody() {
        QueryAllReqBody body = new QueryAllReqBody();
        return body;
    }

    public void handleResQueryAll(String body) {


        QueryAllResBody resBody = JSON.parseObject(body, QueryAllResBody.class);
        List<QueryAllResBodySub> pseudonymList = resBody.getPseudonymList();
        log.info("pseudonymList:" + pseudonymList);
    }

    //A
    public InviteReqBody genReqInviteApplyBody(String rabblePseudonym, String notes) {

        if (rabblePseudonym.equals(pseudonym)) {
            throw new ArgotException(ArgotErrorCode.CLI_PSEUDONYM_APPLY_SELF, "PSEUDONYM_APPLY_SELF");
        }

        InviteReqBody body = new InviteReqBody();
        body.setRabblePseudonym(rabblePseudonym);
        body.setNotes(notes);

        rabbleStepMap.put(rabblePseudonym, STEP_APPLY_SEND);

        return body;
    }

    //A
    public void handleInviteApplyFrom(String body) {

        InviteFromResBody resBody = JSON.parseObject(body, InviteFromResBody.class);

        String rabblePseudonym = resBody.getRabblePseudonym();
        int inviteResult = resBody.getInviteResult();

        if (inviteResult == ApiConstants.SUCCESS) {

            Integer stepStatus = rabbleStepMap.get(rabblePseudonym);
            if (stepStatus == null) {

                return;
            } else if (stepStatus == STEP_APPLY_SEND) {


            } else {
                rabbleStepMap.remove(rabblePseudonym);
            }
        } else {
            rabbleStepMap.remove(rabblePseudonym);
        }

    }

    //B
    public void handleInviteApplyTo(String body) {


        InviteToResBody resBody = JSON.parseObject(body, InviteToResBody.class);
        String originPseudonym = resBody.getOriginPseudonym();
        String originGoodName = resBody.getOriginGoodName();
        String notes = resBody.getNotes();

        originStepMap.put(originPseudonym, STEP_APPLY_SEND_TO);

    }

    //B
    public AcceptReqBody genReqInviteAcceptBody(String acceptPseudonym) {

        Integer inviteStatus = originStepMap.get(acceptPseudonym);
        if (inviteStatus == null) {

            throw new ArgotException(ArgotErrorCode.CLI_PSEUDONYM_ACCEPT_NULL, "genReqInviteAcceptBody acceptPseudonym NOT INVITE");
        }

        if (inviteStatus == STEP_APPLY_SEND_TO) {

            originStepMap.put(acceptPseudonym, STEP_ACCEPT_SEND);

            AcceptReqBody body = new AcceptReqBody();

            body.setOriginPseudonym(acceptPseudonym);

            return body;

        } else {

            originStepMap.remove(acceptPseudonym);

            throw new ArgotException(ArgotErrorCode.CLI_PSEUDONYM_ACCEPT_STATUS_ERROR, "genReqInviteAcceptBody inviteStatus ERROR");
        }
    }

    //B
    public void handleInviteAcceptFrom(String body) {


        AcceptFromResBody resBody = JSON.parseObject(body, AcceptFromResBody.class);
        String originPseudonym = resBody.getOriginPseudonym();
        int inviteResult = resBody.getInviteResult();

        if (inviteResult == ApiConstants.SUCCESS) {

            Integer stepStatus = originStepMap.get(originPseudonym);
            if (stepStatus == null) {

                return;

            } else if (stepStatus == STEP_ACCEPT_SEND) {


            } else {
                originStepMap.remove(originPseudonym);
            }
        } else {
            originStepMap.remove(originPseudonym);
        }

    }

    //A
    public int handleInviteAcceptTo(String body) {

        AcceptToResBody resBody = JSON.parseObject(body, AcceptToResBody.class);

        String rabblePseudonym = resBody.getRabblePseudonym();

        int acceptStatus = resBody.getAcceptStatus();
        if (acceptStatus == ApiConstants.SUCCESS) {
            Integer applyStatus = rabbleStepMap.get(rabblePseudonym);

            if (applyStatus == null) {
                //校验失败
                return ApiConstants.ERROR;

            } else if (applyStatus == STEP_APPLY_SEND) {

                rabbleStepMap.put(rabblePseudonym, STEP_ACCEPT_SEND_TO);
                return ApiConstants.SUCCESS;

            } else {
                rabbleStepMap.remove(rabblePseudonym);
                return ApiConstants.ERROR;
            }
        } else {
            rabbleStepMap.remove(rabblePseudonym);
            return ApiConstants.ERROR;
        }

    }

    //A
    public TcpStep1ReqBody genReqTcpStep1Body(String body) {

        AcceptToResBody resBody = JSON.parseObject(body, AcceptToResBody.class);
        String rabblePseudonym = resBody.getRabblePseudonym();

        TcpStep1ReqBody tcpStep1ReqBody = new TcpStep1ReqBody();
        tcpStep1ReqBody.setRabblePseudonym(rabblePseudonym);

        return tcpStep1ReqBody;
    }

    //A
    public int handleTcpStep1From(String body) {

        TcpStep1ResFromBody resBody = JSON.parseObject(body, TcpStep1ResFromBody.class);
        String rabblePseudonym = resBody.getRabblePseudonym();

        int result = resBody.getStepResult();
        if (result == ApiConstants.SUCCESS) {

            Integer status = rabbleStepMap.get(rabblePseudonym);
            if (status == null) {

                return ApiConstants.ERROR;
            } else if (status == STEP_ACCEPT_SEND_TO) {

                return ApiConstants.SUCCESS;

            } else {
                rabbleStepMap.remove(rabblePseudonym);
                return ApiConstants.ERROR;
            }

        } else {
            rabbleStepMap.remove(rabblePseudonym);
            return ApiConstants.ERROR;
        }

    }

    //B
    public int handleTcpStep1To(String body) {

        TcpStep1ResToBody resBody = JSON.parseObject(body, TcpStep1ResToBody.class);
        String originPseudonym = resBody.getOriginPseudonym();

        Integer stepStatus = originStepMap.get(originPseudonym);

        if (stepStatus == null) {

            return ApiConstants.ERROR;
        } else if (stepStatus == STEP_ACCEPT_SEND) {

            originStepMap.put(originPseudonym, STEP_TCP_1_SEND_TO);
            return ApiConstants.SUCCESS;

        } else {
            originStepMap.remove(originPseudonym);
            return ApiConstants.ERROR;
        }

    }

    //B
    public TcpStep2ReqBody genReqTcpStep2Body(String body) {

        TcpStep1ResToBody resBody = JSON.parseObject(body, TcpStep1ResToBody.class);
        String originPseudonym = resBody.getOriginPseudonym();

        TcpStep2ReqBody tcpStep2ReqBody = new TcpStep2ReqBody();
        tcpStep2ReqBody.setOriginPseudonym(originPseudonym);

        return tcpStep2ReqBody;
    }

    //B
    public int handleTcpStep2From(String body) {


        TcpStep2ResFromBody resBody = JSON.parseObject(body, TcpStep2ResFromBody.class);
        String originPseudonym = resBody.getOriginPseudonym();

        Integer stepStatus = originStepMap.get(originPseudonym);

        int stepResult = resBody.getStepResult();
        if (stepResult == ApiConstants.SUCCESS) {

            if (stepStatus == null) {

                return ApiConstants.ERROR;

            } else if (stepStatus == STEP_TCP_1_SEND_TO) {

                originStepMap.put(originPseudonym, STEP_TCP_2_SEND_BACK);
                return ApiConstants.SUCCESS;

            } else {
                originStepMap.remove(originPseudonym);
                return ApiConstants.ERROR;
            }

        } else {
            originStepMap.remove(originPseudonym);
            return ApiConstants.ERROR;
        }

    }

    //A
    public int handleTcpStep2To(String body) {

        TcpStep2ResToBody resBody = JSON.parseObject(body, TcpStep2ResToBody.class);
        String rabblePseudonym = resBody.getRabblePseudonym();

        Integer status = rabbleStepMap.get(rabblePseudonym);
        if (status == null) {

            return ApiConstants.ERROR;
        } else if (status == STEP_ACCEPT_SEND_TO) {

            rabbleStepMap.put(rabblePseudonym, STEP_TCP_2_SEND_RECEIVE);
            return ApiConstants.SUCCESS;

        } else {
            rabbleStepMap.remove(rabblePseudonym);
            return ApiConstants.ERROR;
        }
    }

}
