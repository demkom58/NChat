package com.demkom58.nchat.client.network;

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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public abstract class SocketClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketClient.class.getTypeName());

    protected final EventLoopGroup eventLoopGroup;
    protected final Bootstrap bootstrap;
    protected Channel channel;

    public SocketClient(ClientInitializer clientInitializer) {
        eventLoopGroup = createEventLoopGroup();
        bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .channel(getNettyChannelClass())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(clientInitializer);
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

    protected static @NotNull EventLoopGroup createEventLoopGroup() {
        return Epoll.isAvailable()
                ? new EpollEventLoopGroup()
                : (KQueue.isAvailable() ? new KQueueEventLoopGroup() : new NioEventLoopGroup());
    }

    protected static @NotNull Class<? extends Channel> getNettyChannelClass() {
        return Epoll.isAvailable()
                ? EpollSocketChannel.class
                : (KQueue.isAvailable() ? KQueueSocketChannel.class : NioSocketChannel.class);
    }

}
