package com.demkom58.nchat.common.network.handler;

import com.demkom58.nchat.common.network.packets.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.StandardCharsets;

public class PacketEncoder extends MessageToByteEncoder<IPacket<?>> {

    private static final String FRAME_SYMBOL = "\u2302";
    private static final byte[] SYMBOL_BYTES = FRAME_SYMBOL.getBytes(StandardCharsets.UTF_8);

    public static byte[] getSymbolBytes() {
        return SYMBOL_BYTES;
    }

    public static String getFrameSymbol() {
        return FRAME_SYMBOL;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, IPacket packet, ByteBuf out) {
        int packetSize = packet.getPayloadSize();
        out.writeShort(packet.getId());
        out.writeShort(packetSize);
        packet.pack(out);
        out.writeCharSequence(getFrameSymbol(), StandardCharsets.UTF_8);
        ReferenceCountUtil.release(packet);
    }
}
