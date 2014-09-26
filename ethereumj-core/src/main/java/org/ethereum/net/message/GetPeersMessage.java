package org.ethereum.net.message;

import static org.ethereum.net.Command.GET_PEERS;

import org.ethereum.net.Command;
import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class GetPeersMessage extends Message {

	/** GetPeers message is always a the same single command payload */ 
	private final static byte[] FIXED_PAYLOAD = Hex.decode("C104");
	
    @Override
    public byte[] getEncoded() {
        return FIXED_PAYLOAD;
    }

    @Override
    public Command getCommand(){
        return GET_PEERS;
    }

    @Override
    public Class<PeersMessage> getAnswerMessage() {
        return PeersMessage.class;
    }

	@Override
	public String toString() {
		return "[command=" + this.getCommand().name() + "]";
	}
}

