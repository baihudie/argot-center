package com.baihudie.backend.netty.msg;

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

    public static void main(String[] args) throws IOException {

        String banditCode = new StringBuffer(UUID.randomUUID().toString().replaceAll("-", ""))
                .append("-")
                .append(UUID.randomUUID().toString().replaceAll("-", ""))
                .toString();

        log.info("banditCode:" + banditCode);

        MsgClientHandler msgClientHandler = new MsgClientHandler(banditCode);

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
                    .connect(new InetSocketAddress("localhost", 8083)).channel();

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String content = reader.readLine();
                msgClientHandler.chat(content, channel);
            }
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

}
