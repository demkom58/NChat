package com.demkom58.nchat.client.network;

import com.demkom58.nchat.common.network.PacketRegistry;
import com.demkom58.nchat.common.network.packets.client.CAuthPacket;
import com.demkom58.nchat.common.network.packets.common.ADisconnectPacket;
import com.demkom58.nchat.common.network.packets.common.AMessagePacket;
import org.springframework.stereotype.Component;

@Component
public class ClientPacketRegistry extends PacketRegistry {
    public ClientPacketRegistry() {
        registerPacket(CAuthPacket.class);
        registerPacket(AMessagePacket.class);
        registerPacket(ADisconnectPacket.class);
    }
}
