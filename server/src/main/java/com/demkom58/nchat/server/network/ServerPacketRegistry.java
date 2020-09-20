package com.demkom58.nchat.server.network;

import com.demkom58.nchat.common.network.PacketRegistry;
import com.demkom58.nchat.common.network.packets.client.CAuthPacket;
import com.demkom58.nchat.common.network.packets.common.ADisconnectPacket;
import com.demkom58.nchat.common.network.packets.common.AMessagePacket;
import org.springframework.stereotype.Component;

@Component
public class ServerPacketRegistry extends PacketRegistry {
    public ServerPacketRegistry() {
        registerPacket(CAuthPacket.class);
        registerPacket(AMessagePacket.class);
        registerPacket(ADisconnectPacket.class);
    }
}
