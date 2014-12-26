package org.ethereum.net.p2p;

import org.spongycastle.util.encoders.Hex;

/**
 * Wrapper around an Ethereum GetPeers message on the network
 *
 * @see org.ethereum.net.p2p.P2pMessageCodes#GET_PEERS
 */
public class GetPeersMessage extends P2pMessage {

    /**
     * GetPeers message is always a the same single command payload
     */
    private final static byte[] FIXED_PAYLOAD = Hex.decode("C104");

    @Override
    public byte[] getEncoded() {
        return FIXED_PAYLOAD;
    }

    @Override
    public P2pMessageCodes getCommand() {
        return P2pMessageCodes.GET_PEERS;
    }

    @Override
    public Class<PeersMessage> getAnswerMessage() {
        return null;
    }

    @Override
    public String toString() {
        return "[" + this.getCommand().name() + "]";
    }
}