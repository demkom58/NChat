package com.demkom58.nchat.client.network;

import com.demkom58.nchat.client.Client;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public abstract class SocketClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketClient.class.getTypeName());
    private static Channel channel;

    private final EventLoopGroup eventLoopGroup;
    private final Bootstrap bootstrap;

    public SocketClient(Client client) {
        final boolean epoll = Epoll.isAvailable();
        final boolean kQueue = KQueue.isAvailable();

        eventLoopGroup = epoll ? new EpollEventLoopGroup() :
                (kQueue ? new KQueueEventLoopGroup() : new NioEventLoopGroup());

        bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .channel(epoll ? EpollSocketChannel.class :
                        (kQueue ? KQueueSocketChannel.class : NioSocketChannel.class))
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ClientInitializer(client));
    }

    public void connect(InetSocketAddress address) {
        try {
            channel = bootstrap.connect(address).sync().channel();
        } catch (InterruptedException e) {
            LOGGER.error("Channel sync was interrupted.");
        }
    }

    public void shutdown() {
        this.eventLoopGroup.shutdownGracefully();

        try {
            this.bootstrap.config().group().terminationFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("Client shutdown process was interrupted!");
        }

    }

    public static Channel getChannel() {
        return channel;
    }

    public static void setChannel(Channel channel) {
        SocketClient.channel = channel;
    }

    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public static Logger getLOGGER() {
        return LOGGER;
    }

}
