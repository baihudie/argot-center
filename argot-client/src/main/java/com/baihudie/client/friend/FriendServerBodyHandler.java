package com.baihudie.client.friend;

import com.baihudie.api.body.TcpStep3Body;
import com.baihudie.api.body.TcpStep4Body;
import com.baihudie.client.entity.ClientTcpEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class FriendServerBodyHandler {

    public TcpStep3Body.TcpStep3ReqBody genReqTcpStep3Body(ClientTcpEntity aTcpEntity) {

        TcpStep3Body.TcpStep3ReqBody reqBody = new TcpStep3Body.TcpStep3ReqBody();

        String token = aTcpEntity.getToken();

        String aPseudonym = aTcpEntity.getAPseudonym();
        String bPseudonym = aTcpEntity.getBPseudonym();

        reqBody.setToken(token);
        reqBody.setAPseudonym(aPseudonym);
        reqBody.setBPseudonym(bPseudonym);

        return reqBody;
    }

    public TcpStep4Body.TcpStep4ReqBody genReqTcpStep4Body(ClientTcpEntity bTcpEntity) {

        TcpStep4Body.TcpStep4ReqBody reqBody = new TcpStep4Body.TcpStep4ReqBody();

        String token = bTcpEntity.getToken();

        String aPseudonym = bTcpEntity.getAPseudonym();
        String bPseudonym = bTcpEntity.getBPseudonym();

        reqBody.setToken(token);
        reqBody.setAPseudonym(aPseudonym);
        reqBody.setBPseudonym(bPseudonym);

        return reqBody;
    }
}
