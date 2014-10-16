package org.ethereum.net.p2p;

import org.ethereum.net.p2p.P2pMessage;
import org.spongycastle.util.encoders.Hex;

/**
 * Wrapper around an Ethereum Pong message on the network
 *
 */
public class PongMessage extends P2pMessage {

	/** Pong message is always a the same single command payload */
	private static byte[] FIXED_PAYLOAD = Hex.decode("C103");

	@Override
	public byte[] getEncoded() {
		return FIXED_PAYLOAD;
	}

	@Override
	public Class<?> getAnswerMessage() {
		return null;
	}

    @Override
    public P2pMessageCodes getCommand(){
        return P2pMessageCodes.PONG;
    }

	@Override
	public String toString() {
		return "[" + this.getCommand().name() + "]";
	}
}