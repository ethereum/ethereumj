package org.ethereum.net.wire;

import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.message.Message;
import org.ethereum.net.message.MessageFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import org.ethereum.net.rlpx.FrameCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.util.encoders.Hex;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.ethereum.net.rlpx.FrameCodec.*;

/**
 * The PacketDecoder parses every valid Ethereum packet to a Message object
 */
@Component
@Scope("prototype")
public class MessageDecoder extends ByteToMessageDecoder {

    private static final Logger loggerWire = LoggerFactory.getLogger("wire");
    private static final Logger loggerNet = LoggerFactory.getLogger("net");

    private boolean active = false;
    private FrameCodec frameCodec;

    public void setFrameCodec(FrameCodec frameCodec) {
        this.active = true;
        this.frameCodec = frameCodec;
    }

    @Autowired
    WorldManager worldManager;


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if (!active){
            out.add(in);
            return;
        }

        int readableBytes = in.readableBytes();
        Frame frame = frameCodec.readFrame(in);
        if (frame == null) return;  // here we check if the buffer was fully read
                                    // the return means read more !!!

        int size = frame.getPayload().available();
        byte[] payload =  new byte[size];
        frame.getPayload().read(payload);

        if (loggerWire.isDebugEnabled())
            loggerWire.debug("Encoded: [{}]", Hex.toHexString(payload));

        Message msg = MessageFactory.createMessage(payload);

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