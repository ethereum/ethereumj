package org.ethereum.net.rlpx.discover;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.rlpx.Message;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PacketDecoder extends MessageToMessageDecoder<DatagramPacket> {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("discover");

    @Override
    public void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) throws Exception {
        ByteBuf buf = packet.content();
        byte[] encoded = new byte[buf.readableBytes()];
        buf.readBytes(encoded);
        Message msg = Message.decode(encoded);
        DiscoveryEvent event = new DiscoveryEvent(msg, packet.sender());
        out.add(event);
    }
}
