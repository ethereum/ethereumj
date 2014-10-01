package org.ethereum.net.message;

import static org.ethereum.net.message.Command.PONG;

import org.spongycastle.util.encoders.Hex;

/**
 * Wrapper around an Ethereum Pong message on the network 
 *
 * @see {@link org.ethereum.net.message.Command#PONG}
 */
public class PongMessage extends Message {

	/** Pong message is always a the same single command payload */ 
	private static byte[] FIXED_PAYLOAD = Hex.decode("C103");

	@Override
	public byte[] getEncoded() {
		return FIXED_PAYLOAD;
	}

	@Override
	public Command getCommand() {
		return PONG;
	}

	@Override
	public Class<?> getAnswerMessage() {
		return null;
	}
	
    @Override
    public String toString() {
    	return "[" + this.getCommand().name() + "]";
    }
}