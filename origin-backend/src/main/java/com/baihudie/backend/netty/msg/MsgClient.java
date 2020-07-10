package com.baihudie.backend.netty.msg;

import com.baihudie.api.proto.SubscribeReqProto;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

public class MsgClient {

    public static void main(String[] args) throws IOException {
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
                                             SubscribeReqProto.SubscribeReq.getDefaultInstance()
                                     ));
                                     pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                                     pipeline.addLast(new ProtobufEncoder());

                                     pipeline.addLast(new MsgClientHandler());
                                 }
                             }

                    )
                    .connect(new InetSocketAddress("localhost", 8083)).channel();

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            int index = 0;
            while (true) {
                index++;
                String userName = reader.readLine();
                SubscribeReqProto.SubscribeReq req = subReq(index, userName);
                channel.writeAndFlush(req);
            }
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    private static SubscribeReqProto.SubscribeReq subReq(int index, String userName) {
        SubscribeReqProto.SubscribeReq.Builder builder =
                SubscribeReqProto.SubscribeReq.newBuilder();

        builder.setSubReqID(index);
        builder.setUserName("wyk");
        builder.setProductName(userName + " book");
        builder.addAddress("Beijing");
        builder.addAddress("Hebei");
        builder.addAddress("Shanghai");
        return builder.build();
    }
}
