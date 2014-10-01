package org.ethereum.net.message;

import static org.ethereum.net.message.Command.GET_PEERS;

import org.spongycastle.util.encoders.Hex;

/**
 * Wrapper around an Ethereum GetPeers message on the network
 *
 * @see {@link org.ethereum.net.message.Command#GET_PEERS}
 */
public class GetPeersMessage extends Message {

	/** GetPeers message is always a the same single command payload */
	private final static byte[] FIXED_PAYLOAD = Hex.decode("C104");

	@Override
	public byte[] getEncoded() {
		return FIXED_PAYLOAD;
	}

	@Override
	public Command getCommand() {
		return GET_PEERS;
	}

	@Override
	public Class<PeersMessage> getAnswerMessage() {
		return PeersMessage.class;
	}

	@Override
	public String toString() {
		return "[" + this.getCommand().name() + "]";
	}
}