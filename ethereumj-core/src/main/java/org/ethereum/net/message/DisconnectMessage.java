package org.ethereum.net.message;

import org.ethereum.net.Command;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;

import static org.ethereum.net.Command.DISCONNECT;
import static org.ethereum.net.message.ReasonCode.REQUESTED;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class DisconnectMessage extends Message {

    private ReasonCode reason;

    public DisconnectMessage(byte[] encoded) {
        super(encoded);
    }

    private void parse() {

		RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

		byte[] reasonB = ((RLPItem) paramsList.get(1)).getRLPData();
        if (reasonB == null)
            this.reason = REQUESTED;
        else
            this.reason = ReasonCode.fromInt(reasonB[0]);
        
        this.parsed = true;
    }

	@Override
	public Command getCommand() {
		return DISCONNECT;
	}

    @Override
    public byte[] getEncoded() {
        return encoded;
    }
    
    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public ReasonCode getReason() {
        if (!parsed) parse();
        return reason;
    }

    public String toString() {
        if (!parsed) parse();
        return "[command=" + this.getCommand().name() + " reason=" + reason + "]";
    }
}