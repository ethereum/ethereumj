package org.ethereum.net.eth;

import org.ethereum.net.p2p.PongMessage;
import org.ethereum.util.RLP;

/**
 * Wrapper around an Ethereum Ping message on the network
 *
 */
public class PacketCountMessage extends EthMessage {

    public PacketCountMessage() {
        encode();
    }

    public PacketCountMessage(byte[] payload) {
        this.encoded = payload;
    }


    public byte[] getEncoded() {
        return this.encoded;
    }

    private void encode() {
        this.encoded = RLP.encodeList(new byte[] {EthMessageCodes.STATUS.asByte()} );
    }


    @Override
    public Class<PongMessage> getAnswerMessage() {
        return PongMessage.class;
    }

    @Override
    public EthMessageCodes getCommand(){
        return EthMessageCodes.PACKET_COUNT;
    }

    @Override
    public String toString() {
        return "[" + getCommand().name() + "]";
    }
}