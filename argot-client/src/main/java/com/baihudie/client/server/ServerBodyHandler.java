package com.baihudie.client.server;

import com.alibaba.fastjson.JSON;
import com.baihudie.api.body.*;
import com.baihudie.api.exception.ArgotException;
import com.baihudie.api.utils.ApiConstants;
import com.baihudie.client.constants.ArgotErrorCodeClient;
import com.baihudie.client.entity.BanditEntity;
import com.baihudie.client.entity.ClientTcpEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Data
public class ServerBodyHandler {

    private String banditCode;
    private String pseudonym;
    private String goodName;

    private Map<String, BanditEntity> inviteStepMap = new ConcurrentHashMap<>();
    private Map<String, BanditEntity> acceptStepMap = new ConcurrentHashMap<>();
    private Map<String, BanditEntity> friendMap = new ConcurrentHashMap<>();

    private Map<String, ClientTcpEntity> connTokenMap = new ConcurrentHashMap<>();

    private static final int STEP_INVITE_SEND = 1; //A
    private static final int STEP_INVITE_SEND_TO = 2; // B

    private static final int STEP_ACCEPT_SEND = 3; // B
    private static final int STEP_ACCEPT_SEND_TO = 4; // A

    private static final int STEP_TCP_1_SEND_TO = 5;//B

    private static final int STEP_TCP_2_SEND_BACK = 6;//B
    private static final int STEP_TCP_2_SEND_RECEIVE = 7;//A


    private static final Integer ACCEPT = 0;


    public ServerBodyHandler(String banditCode, String goodName) {

        this.banditCode = banditCode;
        this.goodName = goodName;

    }

    ////////////////
    // handle req
    ////////////////

    public ActiveBody.ActiveReqBody genReqActiveBody() {

        ActiveBody.ActiveReqBody body = new ActiveBody.ActiveReqBody();
        body.setBanditCode(banditCode);
        body.setGoodName(goodName);

        return body;
    }

    public String handleResActive(String body) {

        ActiveBody.ActiveResBody resBody = JSON.parseObject(body, ActiveBody.ActiveResBody.class);
        String pseudonym = resBody.getPseudonym();
        this.pseudonym = pseudonym;

        if (pseudonym == null) {
            throw new ArgotException(ArgotErrorCodeClient.CLI_ERROR_001, "PSEUDONYM is NULL");
        }
        return pseudonym;
    }

    public ChatsBody.ChatsReqBody genReqChatsBody(String content) {

        ChatsBody.ChatsReqBody body = new ChatsBody.ChatsReqBody();
        body.setContent(content);
        return body;

    }

    public String handleResChats(String body) {

        ChatsBody.ChatsResBody resBody = JSON.parseObject(body, ChatsBody.ChatsResBody.class);
        String content = resBody.getContent();
        String pseudonym = resBody.getPseudonym();

        return "[" + pseudonym + "]:" + content;
    }

    public QueryAllBody.QueryAllReqBody genReqQueryAllBody() {
        QueryAllBody.QueryAllReqBody body = new QueryAllBody.QueryAllReqBody();
        return body;
    }

    public String handleResQueryAll(String body) {

        QueryAllBody.QueryAllResBody resBody = JSON.parseObject(body, QueryAllBody.QueryAllResBody.class);
        List<QueryAllBody.QueryAllResBodySub> pseudonymList = resBody.getPseudonymList();
        return "[SYSTEM] [pseudonymList]:" + pseudonymList.toString();

    }

    //A
    public InviteBody.InviteReqBody genReqInviteBody(String bPseudonym, String notes) {

        if (bPseudonym.equals(pseudonym)) {
            throw new ArgotException(ArgotErrorCodeClient.CLI_ERROR_004, "PSEUDONYM_APPLY_SELF");
        }

        InviteBody.InviteReqBody body = new InviteBody.InviteReqBody();
        body.setBPseudonym(bPseudonym);
        body.setABanditCode(banditCode);
//        body.setAGoodName(goodName);
        body.setNotes(notes);

        BanditEntity bBanditEntity = new BanditEntity();
        bBanditEntity.setPseudonym(bPseudonym);

        inviteStepMap.put(bPseudonym, bBanditEntity);

        return body;
    }

    //A
    public String handleInviteFrom(String body) {

        InviteBody.InviteFromResBody resBody = JSON.parseObject(body, InviteBody.InviteFromResBody.class);

        String bPseudonym = resBody.getBPseudonym();
        int result = resBody.getResult();

        if (result == ApiConstants.SUCCESS) {

            BanditEntity bBanditEntity = inviteStepMap.get(bPseudonym);
            if (bBanditEntity != null) {

                return "INVITE[" + bPseudonym + "] return SUCCESS";
            }

            return "INVITE[" + bPseudonym + "] return ERROR";

        } else {

            inviteStepMap.remove(bPseudonym);

            return "INVITE[" + bPseudonym + "] return ERROR";
        }

    }

    //B
    public String handleInviteTo(String body) {

        InviteBody.InviteToResBody resBody = JSON.parseObject(body, InviteBody.InviteToResBody.class);
        String aBanditCode = resBody.getABanditCode();
        String aPseudonym = resBody.getAPseudonym();
        String aGoodName = resBody.getAGoodName();
        String notes = resBody.getNotes();
        if (aBanditCode.equals(banditCode)) {
            throw new ArgotException(ArgotErrorCodeClient.CLI_ERROR_006, "handleInviteApplyTo CLI_BANDIT_CODE_ERROR");
        }

        BanditEntity banditEntity = new BanditEntity();
        banditEntity.setBanditCode(aBanditCode);
        banditEntity.setPseudonym(aPseudonym);
        banditEntity.setGoodName(aGoodName);

        acceptStepMap.put(aPseudonym, banditEntity);

        return "INVITE FROM[" + aPseudonym + "] return SUCCESS";

    }

    //B
    public AcceptBody.AcceptReqBody genReqAcceptBody(String aPseudonym) {

        BanditEntity aBanditEntity = acceptStepMap.get(aPseudonym);
        if (aBanditEntity == null) {
            throw new ArgotException(ArgotErrorCodeClient.CLI_ERROR_002, "genReqInviteAcceptBody acceptPseudonym NOT INVITE");
        }

        AcceptBody.AcceptReqBody body = new AcceptBody.AcceptReqBody();

        body.setAPseudonym(aPseudonym);
        body.setBBanditCode(banditCode);
        body.setBGoodName(goodName);

        return body;
    }

    //B
    public String handleAcceptFrom(String body) {


        AcceptBody.AcceptFromResBody resBody = JSON.parseObject(body, AcceptBody.AcceptFromResBody.class);
        String aPseudonym = resBody.getAPseudonym();
        int inviteResult = resBody.getResult();

        if (inviteResult == ApiConstants.SUCCESS) {

            BanditEntity aBanditEntity = acceptStepMap.get(aPseudonym);
            if (aBanditEntity == null) {

                return "ACCEPT[" + aPseudonym + "] return ERROR";
            } else {

                friendMap.put(aBanditEntity.getBanditCode(), aBanditEntity);

                return "ACCEPT[" + aPseudonym + "] return SUCCESS";
            }
        } else {
            acceptStepMap.remove(aPseudonym);

            return "ACCEPT[" + aPseudonym + "] return ERROR";
        }
    }

    //A
    public String handleAcceptTo(String body) {

        AcceptBody.AcceptToResBody resBody = JSON.parseObject(body, AcceptBody.AcceptToResBody.class);

        String bPseudonym = resBody.getBPseudonym();

        BanditEntity bBanditEntity = inviteStepMap.get(bPseudonym);

        if (bBanditEntity == null) {
            //校验失败
            return "ACCEPT[" + bPseudonym + "] return ERROR";
        }

        //通过好友申请

        String bBanditCode = resBody.getBBanditCode();
        String bGoodName = resBody.getBGoodName();

        bBanditEntity.setBanditCode(bBanditCode);
        bBanditEntity.setGoodName(bGoodName);

        friendMap.put(bBanditCode, bBanditEntity);

        inviteStepMap.remove(bPseudonym);

        return "ACCEPT[" + bPseudonym + "] return SUCCESS";
    }

    public String friends() {

        Set<Map.Entry<String, BanditEntity>> entrySet = friendMap.entrySet();
        StringBuffer friends = new StringBuffer();
        for (Map.Entry<String, BanditEntity> entry : entrySet) {

            String banditCode = entry.getKey();
            BanditEntity banditEntity = entry.getValue();
            friends.append("FRIEND:[" + banditEntity.toString() + "]\r\n");

        }

        return friends.toString();
    }

    //A
    public TcpStep1Body.TcpStep1ReqBody genReqTcpStep1Body(String bBanditCode) {

        BanditEntity bBanditEntity = friendMap.get(bBanditCode);
        if (bBanditEntity == null) {
            throw new ArgotException(ArgotErrorCodeClient.CLI_ERROR_006, "genReqInviteAcceptBody acceptPseudonym NOT INVITE");
        }

        TcpStep1Body.TcpStep1ReqBody tcpStep1ReqBody = new TcpStep1Body.TcpStep1ReqBody();

        String token = UUID.randomUUID().toString().replaceAll("-", "");
//        String token = "CHANNEL_ID" + UUID.randomUUID().toString().replaceAll("-", "");

        tcpStep1ReqBody.setToken(token);
        tcpStep1ReqBody.setBPseudonym(bBanditEntity.getPseudonym());

        ClientTcpEntity tcpEntity = initATcpEntity(token, bBanditEntity);
        connTokenMap.put(token, tcpEntity);

        return tcpStep1ReqBody;
    }


    //A
    public int handleTcpStep1From(String body) {

        TcpStep1Body.TcpStep1ResFromBody resBody = JSON.parseObject(body, TcpStep1Body.TcpStep1ResFromBody.class);
        String bPseudonym = resBody.getBPseudonym();
        String bBanditCode = resBody.getBBanditCode();
        String token = resBody.getToken();

        int result = resBody.getResult();
        if (result == ApiConstants.SUCCESS) {

            ClientTcpEntity tcpEntity = connTokenMap.get(token);
            if (tcpEntity == null) {

                return ApiConstants.ERROR;
            } else {
                //四码齐全
                tcpEntity.setBPseudonym(bPseudonym);
                return ApiConstants.SUCCESS;
            }

        } else {

            connTokenMap.remove(token);
            return ApiConstants.ERROR;
        }

    }

    //B
    public int handleTcpStep1To(String body) {

        TcpStep1Body.TcpStep1ResToBody resBody = JSON.parseObject(body, TcpStep1Body.TcpStep1ResToBody.class);

        String token = resBody.getToken();
        String aBanditCode = resBody.getABanditCode();
        String aPseudonym = resBody.getAPseudonym();

        BanditEntity bBanditEntity = friendMap.get(aBanditCode);
        if (bBanditEntity == null) {

            return ApiConstants.ERROR;

        } else {

            //四码齐全
            ClientTcpEntity tcpEntity = initBTcpEntity(token, aBanditCode, aPseudonym);

            connTokenMap.put(token, tcpEntity);
            return ApiConstants.SUCCESS;
        }

    }

    //B
    public TcpStep2Body.TcpStep2ReqBody genReqTcpStep2Body(String body) {

        TcpStep1Body.TcpStep1ResToBody resBody = JSON.parseObject(body, TcpStep1Body.TcpStep1ResToBody.class);

        String token = resBody.getToken();
        String aBanditCode = resBody.getABanditCode();
        String aPseudonym = resBody.getAPseudonym();

        TcpStep2Body.TcpStep2ReqBody tcpStep2ReqBody = new TcpStep2Body.TcpStep2ReqBody();

        tcpStep2ReqBody.setToken(token);
        tcpStep2ReqBody.setABanditCode(aBanditCode);
        tcpStep2ReqBody.setAPseudonym(aPseudonym);

        return tcpStep2ReqBody;
    }

    //B
    public ClientTcpEntity handleTcpStep2From(String body) {

        TcpStep2Body.TcpStep2ResFromBody resBody = JSON.parseObject(body, TcpStep2Body.TcpStep2ResFromBody.class);
        int result = resBody.getResult();
        String aPseudonym = resBody.getAPseudonym();
        String token = resBody.getToken();

        ClientTcpEntity bTcpEntity = connTokenMap.get(token);

        if (result == ApiConstants.SUCCESS) {

            if (bTcpEntity == null) {

                return null;

            } else {
                return bTcpEntity;
            }

        } else {
            return null;
        }
    }

    //A
    public ClientTcpEntity handleTcpStep2To(String body) {

        TcpStep2Body.TcpStep2ResToBody resBody = JSON.parseObject(body, TcpStep2Body.TcpStep2ResToBody.class);
        String token = resBody.getToken();
//        String bPseudonym = resBody.getBPseudonym();

        ClientTcpEntity aTcpEntity = connTokenMap.get(token);

        if (aTcpEntity == null) {

            return null;

        } else {
            return aTcpEntity;
        }

    }

    public ClientTcpEntity handleTcpStep3(String body) {

        TcpStep3Body.TcpStep3ResBody resBody = JSON.parseObject(body, TcpStep3Body.TcpStep3ResBody.class);
        String token = resBody.getToken();
        ClientTcpEntity aTcpEntity = connTokenMap.get(token);
        if (aTcpEntity == null) {

            return null;

        } else {

            aTcpEntity.setARemoteIp(resBody.getARemoteIp());
            aTcpEntity.setARemotePort(resBody.getARemotePort());
            aTcpEntity.setBRemoteIp(resBody.getBRemoteIp());
            aTcpEntity.setBRemotePort(resBody.getBRemotePort());

            return aTcpEntity;
        }

    }

    public ClientTcpEntity handleTcpStep4(String body) {

        TcpStep4Body.TcpStep4ResBody resBody = JSON.parseObject(body, TcpStep4Body.TcpStep4ResBody.class);
        String token = resBody.getToken();
        ClientTcpEntity aTcpEntity = connTokenMap.get(token);
        if (aTcpEntity == null) {

            return null;

        } else {

            aTcpEntity.setARemoteIp(resBody.getARemoteIp());
            aTcpEntity.setARemotePort(resBody.getARemotePort());
            aTcpEntity.setBRemoteIp(resBody.getBRemoteIp());
            aTcpEntity.setBRemotePort(resBody.getBRemotePort());

            return aTcpEntity;
        }
    }


    private ClientTcpEntity initATcpEntity(String token, BanditEntity bBanditEntity) {

        ClientTcpEntity tcpEntity = new ClientTcpEntity();

        tcpEntity.setToken(token);

        tcpEntity.setABanditCode(banditCode);
        tcpEntity.setAPseudonym(pseudonym);

        tcpEntity.setBBanditCode(bBanditEntity.getBanditCode());
        //TCPSTEP1，b-p-code无，返回成功则有。
//        tcpEntity.setBPseudonym(bBanditEntity.getPseudonym());

        return tcpEntity;

    }


    private ClientTcpEntity initBTcpEntity(String token, String aBanditCode, String aPseudonym) {

        ClientTcpEntity tcpEntity = new ClientTcpEntity();

        tcpEntity.setToken(token);

        tcpEntity.setABanditCode(aBanditCode);
        tcpEntity.setAPseudonym(aPseudonym);

        tcpEntity.setBBanditCode(banditCode);
        tcpEntity.setBPseudonym(pseudonym);

        return tcpEntity;
    }


}
