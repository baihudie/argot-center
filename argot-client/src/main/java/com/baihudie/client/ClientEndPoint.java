package com.baihudie.client;

import com.baihudie.api.utils.ApiConstants;
import com.baihudie.client.entity.ClientTcpEntity;
import com.baihudie.client.friend.FriendChannel;
import com.baihudie.client.server.ServerMessageHandler;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Data
public class ClientEndPoint {

    //server连接
    private ServerMessageHandler base64ClientHandler;

    private Map<String, FriendChannel> friendChannelMap = new ConcurrentHashMap<>();

    public ClientEndPoint(ServerMessageHandler base64ClientHandler) {

        this.base64ClientHandler = base64ClientHandler;
        base64ClientHandler.setClientEndPoint(this);

    }

    public int createSocket(ClientTcpEntity aTcpEntity) {

        String token = aTcpEntity.getToken();

        try {

            FriendChannel friendChannel = new FriendChannel(aTcpEntity, this);
            friendChannelMap.put(token, friendChannel);

            return ApiConstants.SUCCESS;

        } catch (Exception ex) {
            log.error("createSocket3 ERROR:" + ex.getMessage(), ex);
            return ApiConstants.ERROR;
        }
    }

    public void closeFriendChannel(FriendChannel friendChannel) {

        ClientTcpEntity aTcpEntity = friendChannel.getTcpEntity();

        String token = aTcpEntity.getToken();
        friendChannelMap.remove(token);
    }

    public void outputContent(String content) {
        log.info("OUTPUT START: ====================");
        log.info(content);
        log.info("OUTPUT END: ======================");
    }


    public int handleTcpStep34(ClientTcpEntity tcpEntity) {

        String token = tcpEntity.getToken();
        FriendChannel friendChannel = friendChannelMap.get(token);
        if (friendChannel == null) {
            return ApiConstants.ERROR;
        }

        try {
            friendChannel.connect(tcpEntity);
        } catch (InterruptedException ex) {
            log.error("ERROR:" + ex.getMessage(), ex);
            return ApiConstants.ERROR;
        }

        return ApiConstants.SUCCESS;
    }


    public boolean console(Channel channel, String content) {

        try {
            boolean boo = validateActive(base64ClientHandler);
            if (!boo) {
                log.info("status is STATUS_INIT");
                return true;
            }

            if (content.startsWith("query")) {

                base64ClientHandler.queryAll(channel);

            } else if (content.startsWith("chats ")) {

                base64ClientHandler.chats(content.substring("chats ".length()), channel);

            } else if (content.startsWith("invite ")) {

                String commandLine = content.substring("invite ".length()).trim();
                if (commandLine.length() == 0) {
                    log.info("invite NO ONE");

                    return false;
                }

                String rabblePseudonym = commandLine;
                String notes = null;

                int index = commandLine.indexOf(" ");
                if (index == -1) {

                } else {
                    rabblePseudonym = commandLine.substring(0, index);
                    notes = commandLine.substring(index + " ".length());
                }

                base64ClientHandler.invite(rabblePseudonym, notes, channel);

            } else if (content.startsWith("accept ")) {

                String commandLine = content.substring("accept ".length()).trim();
                if (commandLine.length() == 0) {
                    log.info("accept NO ONE");
                    return false;
                }

                String aPseudonym = commandLine;
                String notes = null;

                int index = commandLine.indexOf(" ");
                if (index == -1) {

                } else {
                    aPseudonym = commandLine.substring(0, index);
                    notes = commandLine.substring(index + " ".length());
                }

                base64ClientHandler.accept(aPseudonym, channel);

            } else if (content.startsWith("friends")) {

                base64ClientHandler.friends();

            } else if (content.startsWith("conn ")) {
                String commandLine = content.substring("conn ".length()).trim();
                if (commandLine.length() == 0) {
                    log.info("conn NO ONE");
                    return false;
                }

                String bBanditCode = commandLine;
                String notes = null;

                int index = commandLine.indexOf(" ");
                if (index == -1) {

                } else {
                    bBanditCode = commandLine.substring(0, index);
                    notes = commandLine.substring(index + " ".length());
                }
                base64ClientHandler.tcpStep1(bBanditCode, channel);

            }
        } catch (Exception ex) {
            log.error("ERROR:" + ex.getMessage(), ex);
        }

        return false;
    }

    private boolean validateActive(ServerMessageHandler base64ClientHandler) {

        boolean boo = false;

        int status = base64ClientHandler.getStatus();
        if (status == ServerMessageHandler.SERVER_ACTIVE) {
            boo = true;
        }

        return boo;
    }


}
