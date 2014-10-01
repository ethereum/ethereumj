package org.ethereum.net.message;

import static org.ethereum.net.message.Command.GET_TRANSACTIONS;

import org.spongycastle.util.encoders.Hex;

/**
 * Wrapper around an Ethereum GetTransactions message on the network 
 *
 * @see {@link org.ethereum.net.message.Command#GET_TRANSACTIONS}
 */
public class GetTransactionsMessage extends Message {

	/** GetTransactions message is always a the same single command payload */ 
	private static byte[] FIXED_PAYLOAD = Hex.decode("C116");

    public byte[] getEncoded() {
        return FIXED_PAYLOAD;
    }

	@Override
	public Command getCommand() {
		return GET_TRANSACTIONS;
	}

    @Override
    public Class<TransactionsMessage> getAnswerMessage() {
        return TransactionsMessage.class;
    }
    
    @Override
    public String toString() {
    	return "[" + this.getCommand().name() + "]";
    }
}

