package com.baihudie.backend.pipe;

import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.exception.ArgotException;
import com.baihudie.backend.constants.ArgotErrorCode;
import com.baihudie.backend.entity.BanditEntity;
import com.baihudie.backend.entity.ConnSwitchEntity;
import com.baihudie.backend.handler.*;
import io.netty.channel.Channel;
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
    private QueryAllHandler whoHandler;

    @Autowired
    private InviteHandler inviteApplyHandler;

    @Autowired
    private AcceptHandler inviteAcceptHandler;

    @Autowired
    private TcpStep1Handler tcpStep1Handler;

    @Autowired
    private TcpStep2Handler tcpStep2Handler;

    @Autowired
    private TcpStep3Handler tcpStep3Handler;

    @Autowired
    private TcpStep4Handler tcpStep4Handler;

    @Autowired
    private ActiveHandler activeHandler;

    protected Map<Integer, PipeHandlerDispatcher> conDispatcher = new ConcurrentHashMap<>();
    protected Map<Integer, PipeHandlerDispatcher> msgDispatcher = new ConcurrentHashMap<>();
    protected Map<Integer, PipeHandlerDispatcher> autoDispatcher = new ConcurrentHashMap<>();

    //pseudonym - banditCode
    protected static Map<String, BanditEntity> pseudonymMap = new ConcurrentHashMap<>(2000);
    protected static Map<String, String> banditCodeMap = new ConcurrentHashMap<>(2000);
    protected static Map<String, ConnSwitchEntity> connSwitchMap = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {

        msgDispatcher.put(ArgotType.REQ_CHATS, chatsHandler);
        msgDispatcher.put(ArgotType.REQ_QUERY_ALL, whoHandler);
        msgDispatcher.put(ArgotType.REQ_INVITE, inviteApplyHandler);
        msgDispatcher.put(ArgotType.REQ_ACCEPT, inviteAcceptHandler);
        msgDispatcher.put(ArgotType.REQ_TCP_STEP_1, tcpStep1Handler);
        msgDispatcher.put(ArgotType.REQ_TCP_STEP_2, tcpStep2Handler);

        autoDispatcher.put(ArgotType.REQ_TCP_STEP_3, tcpStep3Handler);
        autoDispatcher.put(ArgotType.REQ_TCP_STEP_4, tcpStep4Handler);

        conDispatcher.put(ArgotType.REQ_ACTIVE, activeHandler);
    }

    public PipeBodyAuto genAutoPipeBody(Channel channel, String pseudonym, int reqType, String body) {

        PipeHandlerDispatcher autoPipeBodyHandler = autoDispatcher.get(reqType);
        if (autoPipeBodyHandler == null) {
            throw new ArgotException(ArgotErrorCode.ERROR_0023, "REQ_TYPE_NOT_SUPPORT reqType:" + reqType);
        }
        return autoPipeBodyHandler.genPipeBodyAuto(channel, pseudonym, reqType, body);
    }


    public PipeBodyMsg genMsgPipeBody(String pseudonym, int reqType, String body) {

        PipeHandlerDispatcher msgPipeBodyHandler = msgDispatcher.get(reqType);
        if (msgPipeBodyHandler == null) {
            throw new ArgotException(ArgotErrorCode.ERROR_0024, "REQ_TYPE_NOT_SUPPORT reqType:" + reqType);
        }
        return msgPipeBodyHandler.genPipeBodyMsg(pseudonym, reqType, body);
    }


    public PipeBodyCon genConPipeBody(String pseudonym, int reqType, String body) {

        PipeHandlerDispatcher conPipeBodyHandler = conDispatcher.get(reqType);
        if (conPipeBodyHandler == null) {
            throw new ArgotException(ArgotErrorCode.ERROR_0025, "REQ_TYPE_NOT_SUPPORT reqType:" + reqType);
        }
        return conPipeBodyHandler.genPipeBodyCon(pseudonym, reqType, body);

    }

    public PipeBodyCon genPipeBodyCon(String pseudonym, int reqType, String body) {

        throw new ArgotException(ArgotErrorCode.SYS_ERROR, "genPipeBodyCon ERROR");
    }

    public PipeBodyMsg genPipeBodyMsg(String pseudonym, int reqType, String body) {

        throw new ArgotException(ArgotErrorCode.SYS_ERROR, "genPipeBodyMsg ERROR");
    }

    public PipeBodyAuto genPipeBodyAuto(Channel channel, String pseudonym, int reqType, String body) {

        throw new ArgotException(ArgotErrorCode.SYS_ERROR, "genPipeBodyAuto ERROR");
    }

    public void removePseudonym(String pseudonym) {
        BanditEntity banditEntity = pseudonymMap.get(pseudonym);
        pseudonymMap.remove(pseudonym);
        if (banditEntity != null) {

            banditCodeMap.remove(banditEntity.getBanditCode());
        }
    }


}
