package org.ethereum.net.message;

import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;

/**
 * Abstract message class for all messages on the Ethereum network
 * 
 * @author Roman Mandeleil
 * Created on: 06/04/14 14:58
 */
public abstract class Message {

    protected boolean parsed;
    protected byte[] encoded;

	public Message() {}

    public Message(byte[] encoded) {
        this.encoded = encoded;
        parsed = false;
    }
    
    protected void validateMessage(RLPList data, Command expectedCode) {
    	RLPItem item = (RLPItem) data.get(0);
		if (item.getRLPData() == null && expectedCode == Command.HELLO)
			return;
		if ((item.getRLPData()[0] & 0xFF) == expectedCode.asByte())
			return;
    	throw new RuntimeException("Expected " + expectedCode);
    }
    
    public abstract Command getCommand();
    
    /**
     * Gets the RLP encoded byte array of this message
     * 
     * @return RLP encoded byte array representation of this message
     */
	public abstract byte[] getEncoded();
	
	public abstract Class<?> getAnswerMessage();
	
	/**
	 * Returns the message in String format
	 * 
	 * @return A string with all attributes of the message
	 */
	public abstract String toString();
}
