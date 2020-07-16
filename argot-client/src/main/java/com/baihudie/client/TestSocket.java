//package com.baihudie.backend.client;
//
//import com.alibaba.fastjson.JSON;
//import com.baihudie.api.base64.Base64Req;
//import com.baihudie.api.constants.ArgotType;
//import com.baihudie.api.proto.body.ActiveBody;
//import com.baihudie.api.utils.ApiConstants;
//import com.baihudie.backend.utils.ByteUtils;
//import com.baihudie.backend.utils.NumberUtils;
//import com.baihudie.client.MsgClient;
//import lombok.extern.slf4j.Slf4j;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.net.InetSocketAddress;
//import java.net.Socket;
//import java.net.StandardSocketOptions;
//import java.nio.ByteBuffer;
//import java.nio.channels.SocketChannel;
//
//@Slf4j
//public class TestSocket {
//
//
//    private static BufferedReader bufferedReader = null;
//
//    private static int index = 0;
//
//    public static void main(String[] args) throws IOException, InterruptedException {
//
//
////        testSocket();
//
//        testChannel();
//
//    }
//
//    private static void testSocket() {
//
//        Socket socket = null;
//        int localPort;
//
//        try {
//            // 新建一个socket通道
//            socket = new Socket();
//
//            // 设置reuseAddress为true
//            socket.setReuseAddress(true);
//
//            String ip = MsgClient.serverHost;
//            int port = MsgClient.serverPort;
//
//            socket.connect(new InetSocketAddress(ip, port));
//
//            //首次与外网服务器通信的端口
//            //这就意味着我们内网服务要与其他内网主机通信，就可以利用这个通道
//            localPort = socket.getLocalPort();
//
//            System.out.println("本地端口：" + localPort);
//
////            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
//            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//
//            boolean send = true;
//            while (true) {
//
//                if (send) {
//
//                    String content = reader.readLine();
//                    if (content.equals("quit")) {
//                        break;
//                    } else if (content.equals("wait")) {
//                        send = false;
//                        continue;
//                    }
//
//                    //把输入的命令发往服务端
//                    //     String content = scanner.next();
//
//                    Base64Req req = new Base64Req();
//
//                    ActiveBody.ActiveReqBody body = new ActiveBody.ActiveReqBody();
//                    body.setBanditCode("asdfsdfsdfsdfsf");
//                    body.setGoodName("merry");
//                    String bodyStr = JSON.toJSONString(body);
//                    index++;
//                    req.setReqSeq(index);
////                    if (pseudonym != null) {
////                        builder.setPseudonym(pseudonym);
////                    }
//                    req.setReqType(ArgotType.REQ_ACTIVE);
//                    req.setBody(bodyStr);
//
//
//                    String reqStr = JSON.toJSONString(req);
//
//                    OutputStream outputStream = socket.getOutputStream();
//
//                    Integer length = reqStr.getBytes().length;
//
//                    outputStream.write(reqStr.getBytes());
//                    outputStream.write(ApiConstants.DELIMITER.getBytes());
//                    outputStream.flush();
//
//                    send = false;
//                } else {
//
//                    String receive = bufferedReader.readLine();
//                    System.out.println(receive);
//
//                    send = true;
//                }
//
//            }
//        } catch (Exception ex) {
//            log.error("ERROR:" + ex.getMessage(), ex);
//        }
//    }
//
//    private static void testChannel() throws IOException, InterruptedException {
//
//        ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
//        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
//
//        SocketChannel socketChannel = SocketChannel.open();
//        socketChannel.configureBlocking(false);
//
//        socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE)
//                .setOption(StandardSocketOptions.TCP_NODELAY, Boolean.TRUE)
//                .setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE)
//        ;
////                        socketChannel.bind(new InetSocketAddress("host",8080));
//
//        socketChannel.connect(new InetSocketAddress(MsgClient.serverHost, MsgClient.serverPort));
//
//        if (socketChannel.finishConnect()) {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//
//            boolean send = true;
//            byte[] dst = new byte[1024];
//            while (true) {
//
//                if (send) {
//
//
////                    if (content.equals("quit")) {
////                        break;
////                    } else if (content.equals("wait")) {
////                        send = false;
////                        continue;
////                    }
//                    ActiveBody.ActiveReqBody body = new ActiveBody.ActiveReqBody();
//                    body.setBanditCode("aaaaaaaaaaaaaa");
//                    body.setGoodName("merry");
//                    String bodyStr = JSON.toJSONString(body);
//                    ArgotReqProto.ArgotReq.Builder builder = ArgotReqProto.ArgotReq.newBuilder();
//                    index++;
//                    builder.setReqSeq(index);
////                    if (pseudonym != null) {
////                        builder.setPseudonym(pseudonym);
////                    }
//                    builder.setReqType(ArgotType.REQ_ACTIVE);
//                    builder.setBody(bodyStr);
//                    ArgotReqProto.ArgotReq req = builder.build();
//
//                    byte[] data = req.toByteArray();
//                    Integer dataLength = data.length;
//
//                    byte[] head = NumberUtils.intToByte4(dataLength);
////                    Integer headLength = head.length;
//
//                    byte[] allBody = ByteUtils.mergeByteArr(head, data);
//
//                    int offset = 0;
//                    boolean end = false;
//
//                    writeBuffer.clear();
//                    while (true) {
//                        int putLength = 1024;
//                        if (allBody.length - offset < 1024) {
//                            putLength = allBody.length - offset;
//                            end = true;
//                        }
//
//                        writeBuffer.clear();
//                        writeBuffer.put(allBody, offset, putLength);
//                        writeBuffer.flip();
//                        socketChannel.write(writeBuffer);
//
//                        if (end) {
//                            break;
//                        }
//                        offset = offset + 1024;
//                    }
//
//                    send = false;
//
//                } else {
//
//                    byte[] data = new byte[0];
//
//
//                    while (true) {
//
//                        readBuffer.clear();
//                        int readLength = socketChannel.read(readBuffer);
//                        if (readLength > 0) {
//
//
//                            byte[] tempBody = new byte[readLength];
//                            readBuffer.flip();
//                            readBuffer.get(tempBody);
//                            data = ByteUtils.mergeByteArr(data, tempBody);
//                        } else {
//                            break;
//                        }
//                    }
//
//                    Integer dataLength = data.length;
//
//                    byte[] allBody = ByteUtils.trunByteArr(data, 1);
//
//                    try {
//
//                        ArgotResProto.ArgotRes argotRes = ArgotResProto.ArgotRes.parseFrom(allBody);
//                        System.out.println(argotRes);
//
//                    } catch (Exception ex) {
//
//                        log.error("ERROR:" + ex.getMessage(), ex);
//                    }
//                    send = true;
//
//                    String content = reader.readLine();
//                }
//
//
////                TimeUnit.SECONDS.sleep(1);
////                String info = "I'm " + i++ + "-th information from client";
////                buffer.clear();
////
////                buffer.put(info.getBytes());
////                buffer.flip();
////
////                while (buffer.hasRemaining()) {
////                    System.out.println(buffer);
////                    socketChannel.write(buffer);
////                }
//
//
//            }
//
//
//        }
//
////        try {
////
////        } catch (Exception ex) {
////            try {
////                if (socketChannel != null) {
////                    socketChannel.close();
////                }
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
////            log.error("ERROR:" + ex.getMessage(), ex);
////        }
//
//
//    }
//}
