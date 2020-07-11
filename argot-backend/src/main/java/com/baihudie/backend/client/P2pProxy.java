package com.baihudie.backend.client;

import lombok.Data;

import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class P2pProxy {

    private ServerProxy clientProxy;

    private Map<String, Object> rabbleMap = new ConcurrentHashMap<>();

    private Map<String, Integer> rabbleStatusMap = new ConcurrentHashMap<>();
    private Map<String, Integer> originStatusMap = new ConcurrentHashMap<>();

    private static final int STATUS_INIT = 0;

    public P2pProxy(ServerProxy clientProxy) {

        this.clientProxy = clientProxy;
    }

    public void initStep1(String rabblePseudonym) {

        rabbleStatusMap.put(rabblePseudonym, STATUS_INIT);

    }

    public void step1to(String originPseudonym) {

        originStatusMap.put(originPseudonym, STATUS_INIT);
    }

    public void step2to(String rabblePseudonym) throws SocketException {

        Socket socket = new Socket();
        socket.setReuseAddress(true);
//        socket.


    }
}
