package org.ethereum.net.p2p;

import org.spongycastle.util.encoders.Hex;

/**
 * Wrapper around an Ethereum Ping message on the network
 *
 * @see org.ethereum.net.p2p.P2pMessageCodes#PING
 */
public class PingMessage extends P2pMessage {

    /**
     * Ping message is always a the same single command payload
     */
    private final static byte[] FIXED_PAYLOAD = Hex.decode("C0");

    public byte[] getEncoded() {
        return FIXED_PAYLOAD;
    }

    @Override
    public Class<PongMessage> getAnswerMessage() {
        return PongMessage.class;
    }

    @Override
    public P2pMessageCodes getCommand() {
        return P2pMessageCodes.PING;
    }

    @Override
    public String toString() {
        return "[" + getCommand().name() + "]";
    }
}