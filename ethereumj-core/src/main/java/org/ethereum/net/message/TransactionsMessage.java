package org.ethereum.net.message;

import static org.ethereum.net.Command.TRANSACTIONS;

import org.ethereum.core.Transaction;
import org.ethereum.net.Command;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class TransactionsMessage extends Message {
	
	private Logger logger = LoggerFactory.getLogger("wire");
    private List<Transaction> transactions = new ArrayList<Transaction>();

    public TransactionsMessage() {
    }

    public TransactionsMessage(List<Transaction> transactionList) {

        this.transactions = transactionList;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for (Transaction tx : transactionList) {

            byte[] txPayload = tx.getEncoded();
			try {
				baos.write(txPayload);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
        }

        byte[][] elements = new byte[transactionList.size() + 1][];
        elements[0] = new byte[]{Command.TRANSACTIONS.asByte()};
        for (int i = 0; i < transactionList.size(); ++i)
            elements[i + 1] = transactionList.get(i).getEncoded();
        payload = RLP.encodeList(elements);
    }

    public TransactionsMessage(byte[] payload) {
        super(RLP.decode2(payload));
        this.payload = payload;
    }

    public TransactionsMessage(RLPList rawData) {
        this.rawData = rawData;
        parsed = false;
    }

    @Override
    public void parseRLP() {
        RLPList paramsList = (RLPList) rawData.get(0);

		if (Command.fromInt(((RLPItem) (paramsList).get(0)).getRLPData()[0] & 0xFF) != TRANSACTIONS)
			throw new Error("TransactionMessage: parsing for mal data");

        transactions = new ArrayList<Transaction>();
        int size = paramsList.size();
        for (int i = 1; i < size; ++i) {
            RLPList rlpTxData = (RLPList) paramsList.get(i);
            Transaction tx = new Transaction(rlpTxData.getRLPData());
            transactions.add(tx);
        }
        parsed = true;
    }

    public List<Transaction> getTransactions() {
        if (!parsed) parseRLP();
        return transactions;
    }

    @Override
    public String getMessageName() {
        return "Transactions";
    }

    @Override
    public Class getAnswerMessage() {
        return null;
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }

    public String toString() {
        if(!parsed) parseRLP();
        StringBuffer sb = new StringBuffer();
        for (Transaction transaction : transactions)
            sb.append("   ").append(transaction).append("\n");
        return "Transactions Message [\n" + sb.toString() + " ]";
    }
}