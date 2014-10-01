package org.ethereum.net.message;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;

import static org.ethereum.net.message.Command.DISCONNECT;
import static org.ethereum.net.message.ReasonCode.REQUESTED;

/**
 * Wrapper around an Ethereum Disconnect message on the network 
 *
 * @see {@link org.ethereum.net.message.Command#DISCONNECT}
 */
public class DisconnectMessage extends Message {

    private ReasonCode reason;

    public DisconnectMessage(byte[] encoded) {
        super(encoded);
    }
    
    public DisconnectMessage(ReasonCode reason) {
    	this.reason = reason;
    	parsed = true;
    }

    private void parse() {

		RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        if ((((RLPItem)paramsList.get(0)).getRLPData()[0] & 0xFF) != DISCONNECT.asByte())
            throw new RuntimeException("Not a DisconnectMessage command");
		
		byte[] reasonBytes = ((RLPItem) paramsList.get(1)).getRLPData();
        if (reasonBytes == null)
            this.reason = REQUESTED;
        else
            this.reason = ReasonCode.fromInt(reasonBytes[0]);
        
        this.parsed = true;
    }
    
    private void encode() {
    	byte[] encodedCommand = RLP.encodeByte(DISCONNECT.asByte());
    	byte[] encodedReason = RLP.encodeByte(this.reason.asByte());
    	this.encoded = RLP.encodeList(encodedCommand, encodedReason);
    }

	@Override
	public Command getCommand() {
		return DISCONNECT;
	}

    @Override
    public byte[] getEncoded() {
    	if (encoded == null) this.encode();
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
        return "[" + this.getCommand().name() + " reason=" + reason + "]";
    }
}