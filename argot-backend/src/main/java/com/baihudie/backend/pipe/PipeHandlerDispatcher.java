package com.baihudie.backend.pipe;

import com.baihudie.api.constants.ArgotType;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.constants.ArgotException;
import com.baihudie.backend.entity.BanditEntity;
import com.baihudie.backend.handler.ActiveHandler;
import com.baihudie.backend.handler.ChatsHandler;
import com.baihudie.backend.handler.InviteApplyHandler;
import com.baihudie.backend.handler.WhoHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Primary
@Component
public class PipeHandlerDispatcher implements InitializingBean {

    @Autowired
    private ChatsHandler chatsHandler;

    @Autowired
    private WhoHandler whoHandler;

    @Autowired
    private InviteApplyHandler inviteApplyHandler;

    @Autowired
    private ActiveHandler activeHandler;

    //pseudonym - banditCode
    protected static Map<String, BanditEntity> pseudonymMap = new ConcurrentHashMap<>(2000);
    protected static Map<String, String> banditCodeMap = new ConcurrentHashMap<>(2000);

    protected Map<Integer, PipeHandlerDispatcher> msgDispatcher = new ConcurrentHashMap<>(2000);
    protected Map<Integer, PipeHandlerDispatcher> conDispatcher = new ConcurrentHashMap<>(2000);

    @Override
    public void afterPropertiesSet() throws Exception {

        msgDispatcher.put(ArgotType.REQ_CHATS, chatsHandler);
        msgDispatcher.put(ArgotType.REQ_QUERY_ALL, whoHandler);
        msgDispatcher.put(ArgotType.REQ_INVITE_APPLY, inviteApplyHandler);

        conDispatcher.put(ArgotType.REQ_ACTIVE, activeHandler);
    }


    public PipeBodyMsg genMsgPipeBody(String pseudonym, int reqType, String body) {

        PipeHandlerDispatcher msgPipeBodyHandler = msgDispatcher.get(reqType);
        if (msgPipeBodyHandler == null) {
            throw new ArgotException(ArgotErrorCode.REQ_TYPE_NOT_SUPPORT, "REQ_TYPE_NOT_SUPPORT reqType:" + reqType);
        }
        return msgPipeBodyHandler.genPipeBodyMsg(pseudonym, reqType, body);
    }


    //    public ResActiveBody genResActiveBody(ReqActiveBody reqBody) {
    public PipeBodyCon genConPipeBody(String pseudonym, int reqType, String body) {

        PipeHandlerDispatcher conPipeBodyHandler = conDispatcher.get(reqType);
        if (conPipeBodyHandler == null) {
            throw new ArgotException(ArgotErrorCode.REQ_TYPE_NOT_SUPPORT, "REQ_TYPE_NOT_SUPPORT reqType:" + reqType);
        }
        return conPipeBodyHandler.genPipeBodyCon(pseudonym, reqType, body);


    }

    public PipeBodyCon genPipeBodyCon(String pseudonym, int reqType, String body) {

        throw new ArgotException(ArgotErrorCode.SYS_ERROR, "genPipeBodyCon ERROR");
    }


    public PipeBodyMsg genPipeBodyMsg(String pseudonym, int reqType, String body) {

        throw new ArgotException(ArgotErrorCode.SYS_ERROR, "genPipeBodyMsg ERROR");
    }

    public void removePseudonym(String pseudonym) {
        BanditEntity banditEntity = pseudonymMap.get(pseudonym);
        pseudonymMap.remove(pseudonym);
        if (banditEntity != null) {

            banditCodeMap.remove(banditEntity.getBanditCode());
        }
    }

}
