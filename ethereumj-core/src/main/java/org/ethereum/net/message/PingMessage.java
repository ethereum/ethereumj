package org.ethereum.net.message;

import static org.ethereum.net.message.Command.PING;

import org.spongycastle.util.encoders.Hex;

/**
 * Wrapper around an Ethereum Ping message on the network
 *
 * @see {@link org.ethereum.net.message.Command#PING}
 */
public class PingMessage extends Message {

	/** Ping message is always a the same single command payload */
	private static byte[] FIXED_PAYLOAD = Hex.decode("C102");

	public byte[] getEncoded() {
		return FIXED_PAYLOAD;
	}

	@Override
	public Command getCommand() {
		return PING;
	}

	@Override
	public Class<PongMessage> getAnswerMessage() {
		return PongMessage.class;
	}

	@Override
	public String toString() {
		return "[" + getCommand().name() + "]";
	}
}