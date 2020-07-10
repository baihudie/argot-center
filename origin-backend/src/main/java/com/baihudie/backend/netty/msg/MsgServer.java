package com.baihudie.backend.netty.msg;

import com.baihudie.api.proto.SubscribeReqProto;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MsgServer {

    @Autowired
    private MsgServerHandler msgServerHandler;

    public void startServer() {
        System.out.println("========MSG 服务端启动成功==============");
        //创建两个线程组，用于接收客户端的请求任务,创建两个线程组是因为netty采用的是反应器设计模式
        //反应器设计模式中bossGroup线程组用于接收
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //workerGroup线程组用于处理任务
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        //创建netty的启动类
        ServerBootstrap bootstrap = new ServerBootstrap();
        //创建一个通道
        ChannelFuture f = null;
        try {
            bootstrap.group(bossGroup, workerGroup) //设置线程组
                    .channel(NioServerSocketChannel.class) //设置通道为非阻塞IO
                    .option(ChannelOption.SO_BACKLOG, 128) //设置日志
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_RCVBUF, 32 * 1024)  //接收缓存
                    .childOption(ChannelOption.SO_KEEPALIVE, true)//是否保持连接
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        //设置处理请求的逻辑处理类
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //ChannelPipeline是handler的任务组，里面有多个handler
                            ChannelPipeline pipeline = ch.pipeline();
                            //逻辑处理类
                            ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            ch.pipeline().addLast(new ProtobufDecoder(
                                    SubscribeReqProto.SubscribeReq.getDefaultInstance()
                            ));
                            ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            ch.pipeline().addLast(new ProtobufEncoder());
                            ch.pipeline().addLast(msgServerHandler);
                        }
                    });

            f = bootstrap.bind(8083).sync();//阻塞端口号，以及同步策略
            f.channel().closeFuture().sync();//关闭通道
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //优雅退出
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
