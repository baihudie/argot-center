package com.baihudie.client;

import com.baihudie.api.utils.ApiConstants;
import com.baihudie.client.constants.ClientConstants;
import com.baihudie.client.server.ServerMessageHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.base64.Base64Decoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.UUID;

@Slf4j
public class MsgClient {


    public static void main(String[] args) throws IOException {

        String banditCode = new StringBuffer(UUID.randomUUID().toString().replaceAll("-", ""))
                .append(UUID.randomUUID().toString().replaceAll("-", ""))
                .toString();

        log.info("banditCode:" + banditCode);

        String goodName = "kitty";

        ServerMessageHandler serverMessageHandler = new ServerMessageHandler(banditCode, goodName);
        ClientEndPoint clientEndPoint = new ClientEndPoint(serverMessageHandler);


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

                                     ByteBuf delimiter = Unpooled.copiedBuffer(ApiConstants.DELIMITER.getBytes());
                                     pipeline.addLast(new DelimiterBasedFrameDecoder(ApiConstants.MAX_FRAME_LENGTH, delimiter));

                                     pipeline.addLast(new Base64Decoder());
                                     pipeline.addLast(new StringDecoder());

                                     pipeline.addLast(new StringEncoder());
                                     pipeline.addLast(serverMessageHandler);

                                 }
                             }
                    ).connect(new InetSocketAddress(ClientConstants.SERVER_HOST, ClientConstants.SERVER_PORT)).channel();

            simulateConsole(channel, clientEndPoint);

        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }


    private static void simulateConsole(Channel channel, ClientEndPoint clientEndPoint) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            String content = reader.readLine();
            boolean end = clientEndPoint.console(channel, content);
            if (end) {
                break;
            }
        }
    }


}
