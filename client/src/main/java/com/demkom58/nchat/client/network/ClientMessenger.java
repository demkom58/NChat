package com.demkom58.nchat.client.network;

import com.demkom58.nchat.client.event.ShutdownEvent;
import com.demkom58.nchat.client.repository.User;
import com.demkom58.nchat.common.Environment;
import com.demkom58.nchat.common.network.packets.Packet;
import com.demkom58.nchat.common.network.packets.client.CAuthPacket;
import com.demkom58.nchat.common.network.packets.common.AMessagePacket;
import io.netty.channel.ChannelFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ClientMessenger extends SocketClient implements ApplicationListener<ShutdownEvent> {
    private final Queue<String> messagesQueue = new ConcurrentLinkedQueue<>();

    private final ReentrantLock senderLock = new ReentrantLock();
    private final Condition newUpdate = senderLock.newCondition();

    private final User user;

    private boolean working;
    private InetSocketAddress address;

    @Autowired
    public ClientMessenger(ClientInitializer clientInitializer, User user) {
        super(clientInitializer);
        this.user = user;
    }

    public void run(@NotNull final InetSocketAddress address) throws Exception {
        this.address = address;
        this.working = true;

        try {
            connect(address);
            // Send register packet
            sendPacket(new CAuthPacket(user.getName(), Environment.PROTOCOL_VERSION));

            while (working) {
                senderLock.lock();

                if (messagesQueue.isEmpty()) {
                    newUpdate.await();
                    senderLock.unlock();
                    continue;
                }

                String message = messagesQueue.poll().replace("╥", "");
                senderLock.unlock();
                sendPacket(new AMessagePacket(message)).syncUninterruptibly();
                user.addSentMessage();
            }
        } finally {
            super.eventLoopGroup.shutdownGracefully();
        }
    }

    public ChannelFuture sendPacket(Packet<?> packet) {
        if (channel == null)
            return null;

        return channel.writeAndFlush(packet);
    }

    public void sendMessage(String message) {
        senderLock.lock();
        messagesQueue.offer(message);
        newUpdate.signalAll();
        senderLock.unlock();
    }

    public @Nullable InetSocketAddress getAddress() {
        return address;
    }

    public void close() {
        senderLock.lock();
        working = false;
        newUpdate.signalAll();
        senderLock.unlock();
    }

    @Override
    public void onApplicationEvent(@NotNull ShutdownEvent event) {
        close();
    }
}
