package org.ethereum.net.wire;

import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.message.Message;
import org.ethereum.net.message.MessageFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.util.encoders.Hex;

import javax.inject.Inject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The PacketDecoder parses every valid Ethereum packet to a Message object
 */
@Component
@Scope("prototype")
public class MessageDecoder extends ByteToMessageDecoder {

    private static final Logger loggerWire = LoggerFactory.getLogger("wire");
    private static final Logger loggerNet = LoggerFactory.getLogger("net");

    @Inject
    WorldManager worldManager;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if (!isValidEthereumPacket(in)) {
            return;
        }

        byte[] encoded = new byte[in.readInt()];
        in.readBytes(encoded);

        if (loggerWire.isDebugEnabled())
            loggerWire.debug("Encoded: [{}]", Hex.toHexString(encoded));

        Message msg = MessageFactory.createMessage(encoded);

        if (loggerNet.isInfoEnabled())
            loggerNet.info("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), msg);

        EthereumListener listener = worldManager.getListener();
        listener.onRecvMessage(msg);

        out.add(msg);
        in.markReaderIndex();
    }

    private boolean isValidEthereumPacket(ByteBuf in) {
        // Ethereum message is at least 8 bytes
        if (in.readableBytes() < 8)
            return false;

        long syncToken = in.readUnsignedInt();

        if (!((syncToken >> 24 & 0xFF) == 0x22 &&
                (syncToken >> 16 & 0xFF) == 0x40 &&
                (syncToken >> 8 & 0xFF) == 0x08 &&
                (syncToken & 0xFF) == 0x91)) {

            // TODO: Drop frame and continue.
            // A collision can happen (although rare)
            // If this happens too often, it's an attack.
            // In that case, drop the peer.
            loggerWire.error("Abandon garbage, wrong sync token: [{}]", syncToken);
        }

        // Don't have the full message yet
        long msgSize = in.getInt(in.readerIndex());
        if (msgSize > in.readableBytes()) {
            loggerWire.trace("msg decode: magicBytes: [{}], readBytes: [{}] / msgSize: [{}] ",
                    syncToken, in.readableBytes(), msgSize);
            in.resetReaderIndex();
            return false;
        }

        loggerWire.trace("Message fully constructed: readBytes: [{}] / msgSize: [{}]", in.readableBytes(), msgSize);
        return true;
    }
}