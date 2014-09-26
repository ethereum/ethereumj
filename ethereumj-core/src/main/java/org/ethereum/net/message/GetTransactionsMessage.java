package org.ethereum.net.message;

import static org.ethereum.net.Command.GET_TRANSACTIONS;

import org.ethereum.net.Command;
import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 06/04/14 14:56
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
    	return "[command=" + this.getCommand().name() + "]";
    }
}

