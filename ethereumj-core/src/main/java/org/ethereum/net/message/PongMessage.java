package org.ethereum.net.message;

import static org.ethereum.net.Command.PONG;

import org.ethereum.net.Command;
import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 06/04/14 14:56
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
    	return "[command=PONG]";
    }
}