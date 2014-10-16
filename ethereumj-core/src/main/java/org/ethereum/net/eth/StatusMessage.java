package org.ethereum.net.eth;

import org.ethereum.net.p2p.PongMessage;
import org.ethereum.util.RLP;

/**
 * Wrapper around an Ethereum Ping message on the network
 *
 */
public class StatusMessage extends EthMessage {

    public StatusMessage() {
        encode();
    }

    public StatusMessage(byte[] payload) {
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
        return EthMessageCodes.STATUS;
    }

	@Override
	public String toString() {
		return "[" + getCommand().name() + "]";
	}
}