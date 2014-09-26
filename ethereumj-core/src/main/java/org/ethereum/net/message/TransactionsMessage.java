package org.ethereum.net.message;

import static org.ethereum.net.Command.TRANSACTIONS;

import org.ethereum.core.Transaction;
import org.ethereum.net.Command;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;

import java.util.ArrayList;
import java.util.List;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class TransactionsMessage extends Message {
	
    private List<Transaction> transactions = new ArrayList<>();

    public TransactionsMessage(byte[] encoded) {
        super(encoded);
    }

    public TransactionsMessage(List<Transaction> transactionList) {
        this.transactions = transactionList;
    }
    
    @Override
    public byte[] getEncoded() {
    	if (encoded == null) this.encode();
    	return encoded;
    }

    private void encode() {
        byte[][] encodedTransactions = new byte[transactions.size()][];
        byte[] command = new byte[]{Command.TRANSACTIONS.asByte()};
        for (int i = 0; i < transactions.size(); ++i)
            encodedTransactions[i + 1] = transactions.get(i).getEncoded();
        byte[] encodedTxsList = RLP.encodeList(encodedTransactions);
        this.encoded = RLP.encodeList(command, encodedTxsList);
    }

    private void parse() {
		RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

		int commandByte = ((RLPItem) (paramsList).get(0)).getRLPData()[0] & 0xFF;
		if (Command.fromInt(commandByte) != TRANSACTIONS)
			throw new RuntimeException("Not a TransactionMessage: " + Integer.toHexString(commandByte));

        transactions = new ArrayList<>();
        int size = paramsList.size();
        for (int i = 1; i < size; ++i) {
            RLPList rlpTxData = (RLPList) paramsList.get(i);
            Transaction tx = new Transaction(rlpTxData.getRLPData());
            transactions.add(tx);
        }
        parsed = true;
    }

    public List<Transaction> getTransactions() {
        if (!parsed) parse();
        return transactions;
    }

	@Override
	public Command getCommand() {
		return TRANSACTIONS;
	}

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }
    
    public String toString() {
        if(!parsed) parse();
        StringBuffer sb = new StringBuffer();
        for (Transaction transaction : transactions)
            sb.append("   ").append(transaction).append("\n");
        return "Transactions Message [\n" + sb.toString() + " ]";
    }
}