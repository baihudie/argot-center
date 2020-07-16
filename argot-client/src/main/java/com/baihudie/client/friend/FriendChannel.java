package com.baihudie.client.friend;

import com.baihudie.api.constants.ArgotType;
import com.baihudie.api.utils.ApiConstants;
import com.baihudie.client.ClientEndPoint;
import com.baihudie.client.constants.ClientConstants;
import com.baihudie.client.entity.ClientTcpEntity;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.base64.Base64Decoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Date;

@Slf4j
@Data
public class FriendChannel {

    public static final int STATUS_INIT = 0;
    public static final int STATUS_TCP_STEP34 = 1;
    public static final int STATUS_FRIEND_CONN = 2;

    private ClientEndPoint clientEndPoint;
    private ClientTcpEntity tcpEntity;
    private Date createTime;
    private int status;
    private FriendServerMessageHandler friendServerMessageHandler;

    private NioEventLoopGroup serverEventLoopGroup;
    private Channel serverChannel;

    private NioEventLoopGroup friendEventLoopGroup;
    private Channel clientChannel;

    public FriendChannel(ClientTcpEntity tcpEntity, ClientEndPoint clientEndPoint) throws InterruptedException {

        this.clientEndPoint = clientEndPoint;
        this.tcpEntity = tcpEntity;
        this.createTime = new Date();
        this.status = STATUS_INIT;

        FriendServerMessageHandler friendServerMessageHandler = new FriendServerMessageHandler(tcpEntity, this);

        NioEventLoopGroup serverEventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(serverEventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_REUSEADDR, true) //这里要重用端口
                    .handler(new ChannelInitializer<SocketChannel>() {

                                 @Override
                                 protected void initChannel(SocketChannel socketChannel) throws Exception {

                                     ChannelPipeline pipeline = socketChannel.pipeline();

                                     ByteBuf delimiter = Unpooled.copiedBuffer(ApiConstants.DELIMITER.getBytes());
                                     pipeline.addLast(new DelimiterBasedFrameDecoder(ApiConstants.MAX_FRAME_LENGTH, delimiter));

                                     pipeline.addLast(new Base64Decoder());

                                     pipeline.addLast(new StringDecoder());
                                     pipeline.addLast(new StringEncoder());

                                     pipeline.addLast(friendServerMessageHandler);

                                 }
                             }
                    );

            final ChannelFuture future = bootstrap.connect(new InetSocketAddress(ClientConstants.SERVER_HOST, ClientConstants.SERVER_PORT)).sync();

            Channel channel = future.channel();
            InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
            tcpEntity.setALocalPort(localAddress.getPort());

            this.friendServerMessageHandler = friendServerMessageHandler;
            this.serverChannel = channel;
            this.serverEventLoopGroup = serverEventLoopGroup;

        } catch (Exception ex) {

            serverEventLoopGroup.shutdownGracefully();
            throw ex;
        }

    }


    public void connect(ClientTcpEntity tcpEntity) throws InterruptedException {
        InetSocketAddress localAddress = (InetSocketAddress) serverChannel.localAddress();
//        String localHost = localAddress.getAddress().getHostAddress();
//        int localPort = localAddress.getPort();

        FriendClientMessageHandler friendClientMessageHandler = new FriendClientMessageHandler(tcpEntity, this);

        NioEventLoopGroup friendEventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(friendEventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_REUSEADDR, true) //这里要重用端口
                    .handler(new ChannelInitializer<SocketChannel>() {

                                 @Override
                                 protected void initChannel(SocketChannel socketChannel) throws Exception {

                                     ChannelPipeline pipeline = socketChannel.pipeline();

                                     ByteBuf delimiter = Unpooled.copiedBuffer(ApiConstants.DELIMITER.getBytes());
                                     pipeline.addLast(new DelimiterBasedFrameDecoder(ApiConstants.MAX_FRAME_LENGTH, delimiter));

                                     pipeline.addLast(new Base64Decoder());

                                     pipeline.addLast(new StringDecoder());
                                     pipeline.addLast(new StringEncoder());

                                     pipeline.addLast(friendClientMessageHandler);

                                 }
                             }
                    );
            final ChannelFuture future = bootstrap.bind(localAddress).sync();
            Channel clientChannel = future.channel();

            friendClientMessageHandler.initClientChannel(clientChannel);

            this.clientChannel = clientChannel;
            this.friendEventLoopGroup = friendEventLoopGroup;
        } catch (Exception ex) {

            friendEventLoopGroup.shutdownGracefully();
            throw ex;
        }
    }


    public void serverChannelClose() {
        try {
            serverEventLoopGroup.shutdownGracefully();
        } catch (Exception ex) {
            log.error("ERROR:" + ex.getMessage(), ex);
        } finally {
            if (status == STATUS_INIT) {
                clientEndPoint.closeFriendChannel(this);
            }
        }
    }

    public void clientChannelClose() {
        try {
            if (serverEventLoopGroup != null) {
                if (!serverEventLoopGroup.isShutdown()) {
                    serverEventLoopGroup.shutdownGracefully();
                }
            }
        } catch (Exception ex) {
            log.error("ERROR:" + ex.getMessage(), ex);
        }
        try {
            if (friendEventLoopGroup != null) {
                if (!friendEventLoopGroup.isShutdown()) {
                    friendEventLoopGroup.shutdownGracefully();
                }
            }
        } catch (Exception ex) {
            log.error("ERROR:" + ex.getMessage(), ex);
        }

        clientEndPoint.closeFriendChannel(this);

    }

    public void outputContent(String content) {
        ClientTcpEntity tcpEntity = this.getTcpEntity();

        String banditCode = null;

        int argot = tcpEntity.getArgotType();
        if (argot == ArgotType.REQ_TCP_STEP_3) {
            banditCode = tcpEntity.getBBanditCode();
        } else {
            banditCode = tcpEntity.getABanditCode();
        }
        StringBuffer allContent = new StringBuffer("RECEIVE MESSAGE FROM :")
                .append("[banditCode:").append(banditCode).append(", TOKEN:").append(tcpEntity.getToken()).append("]")
                .append("\r\n").append(content);
        clientEndPoint.outputContent(allContent.toString());

    }
}
