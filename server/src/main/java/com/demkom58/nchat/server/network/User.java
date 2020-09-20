package com.demkom58.nchat.server.network;

import com.demkom58.nchat.common.network.packets.IPacket;
import com.demkom58.nchat.common.network.packets.common.AMessagePacket;
import com.demkom58.nchat.common.network.util.NetworkUtil;
import com.demkom58.nchat.server.application.Server;
import io.netty.channel.Channel;

public class User {
    private final Server server;
    private final Channel channel;
    private final String nick;

    private final String address;
    private final String ip;
    private final int port;

    private long lastSentMessageTime = 0L;

    public User(Server server, Channel channel, String nick) {
        this.server = server;
        this.channel = channel;
        this.nick = nick;

        this.address = NetworkUtil.getAddress(channel);
        this.ip = address.split(":")[0];
        this.port = Integer.parseInt(address.split(":")[1]);
    }

    public String getNick() {
        return nick;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getAddress() {
        return address;
    }

    public String getIP() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public long getLastSentMessageTime() {
        return lastSentMessageTime;
    }

    public void setLastSentMessageTime(long lastSentMessageTime) {
        this.lastSentMessageTime = lastSentMessageTime;
    }

    public void sendMessage(String message) {
        User.sendMessage(getChannel(), message);
    }

    public void sendPacket(IPacket<?> packet) {
        User.sendPacket(getChannel(), packet);
    }

    public void kick(String reason) {
        server.kickUser(this, reason);
    }

    public static void sendMessage(Channel channel, String message) {
        channel.writeAndFlush(new AMessagePacket(message));
    }

    public static void sendPacket(Channel channel, IPacket<?> packet) {
        channel.writeAndFlush(packet);
    }
}
