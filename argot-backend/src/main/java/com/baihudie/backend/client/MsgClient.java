package com.baihudie.backend.client;

import com.baihudie.api.proto.ArgotResProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.UUID;

@Slf4j
public class MsgClient {

//    public static String serverHost = "110ceee9.nat123.fun";
//    public static int serverPort = 25574;

    public static String serverHost = "localhost";
    public static int serverPort = 4685;

    public static void main(String[] args) throws IOException {

        String banditCode = new StringBuffer(UUID.randomUUID().toString().replaceAll("-", ""))
                .append("-")
                .append(UUID.randomUUID().toString().replaceAll("-", ""))
                .toString();

        log.info("banditCode:" + banditCode);
        String goodName = "kitty";

        MsgClientHandler msgClientHandler = new MsgClientHandler(banditCode, goodName);

        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            Channel channel = bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {

                                 @Override
                                 protected void initChannel(SocketChannel socketChannel) throws Exception {
                                     ChannelPipeline pipeline = socketChannel.pipeline();
                                     pipeline.addLast(new ProtobufVarint32FrameDecoder());
                                     pipeline.addLast(new ProtobufDecoder(
                                             ArgotResProto.ArgotRes.getDefaultInstance()
                                     ));
                                     pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                                     pipeline.addLast(new ProtobufEncoder());

                                     pipeline.addLast(msgClientHandler);
                                 }
                             }
                    )
                    .connect(new InetSocketAddress(serverHost, serverPort)).channel();
//                    .connect(new InetSocketAddress("110ceee9.nat123.fun", 25574)).channel();

            simulateConsole(channel, msgClientHandler);

        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    private static void simulateConsole(Channel channel, MsgClientHandler msgClientHandler) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {

            try {


                String content = reader.readLine();

                boolean boo = validateActive(msgClientHandler);
                if (!boo) {
                    log.info("status is STATUS_INIT");
                    break;
                }

                if (content.startsWith("query")) {

                    msgClientHandler.queryAll(channel);

                } else if (content.startsWith("chats ")) {

                    msgClientHandler.chats(content.substring("chats ".length()), channel);

                } else if (content.startsWith("invite ")) {

                    String commandLine = content.substring("invite ".length()).trim();
                    if (commandLine.length() == 0) {
                        log.info("invite NO ONE");
                        continue;
                    }

                    String rabblePseudonym = commandLine;
                    String notes = null;

                    int index = commandLine.indexOf(" ");
                    if (index == -1) {

                    } else {
                        rabblePseudonym = commandLine.substring(0, index);
                        notes = commandLine.substring(index + " ".length());
                    }

                    msgClientHandler.inviteApply(rabblePseudonym, notes, channel);

                } else if (content.startsWith("accept ")) {

                    String commandLine = content.substring("accept ".length()).trim();
                    if (commandLine.length() == 0) {
                        log.info("invite_accept NO ONE");
                        continue;
                    }

                    String acceptPseudonym = commandLine;
                    String notes = null;

                    int index = commandLine.indexOf(" ");
                    if (index == -1) {

                    } else {
                        acceptPseudonym = commandLine.substring(0, index);
                        notes = commandLine.substring(index + " ".length());
                    }

                    msgClientHandler.inviteAccept(acceptPseudonym, channel);

                }
            } catch (Exception ex) {
                
                log.error("ERROR:" + ex.getMessage(), ex);
            }
        }
    }

    private static boolean validateActive(MsgClientHandler msgClientHandler) {

        boolean boo = false;

        int status = msgClientHandler.getStatus();
        if (status == MsgClientHandler.STATUS_ACTIVE) {
            boo = true;
        }

        return boo;
    }

}
