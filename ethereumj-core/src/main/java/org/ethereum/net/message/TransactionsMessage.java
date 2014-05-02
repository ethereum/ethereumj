package org.ethereum.net.message;

import org.ethereum.net.rlp.RLPItem;
import org.ethereum.net.rlp.RLPList;
import org.ethereum.net.vo.TransactionData;

import java.util.ArrayList;
import java.util.List;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class TransactionsMessage extends Message {

    private final byte commandCode = 0x12;
    private List<TransactionData> transactions = new ArrayList<TransactionData>();

    public TransactionsMessage() {
    }

    public TransactionsMessage(RLPList rawData) {
        super(rawData);
    }

    @Override
    public void parseRLP() {

        RLPList paramsList = (RLPList) rawData.getElement(0);

        if ((((RLPItem)(paramsList).getElement(0)).getData()[0] & 0xFF) != commandCode){

            throw new Error("TransactionMessage: parsing for mal data");
        }

        transactions = new ArrayList<TransactionData>();
        int size = paramsList.getList().size();
        for (int i = 1; i < size; ++i){

            RLPList rlpTxData = (RLPList) paramsList.getElement(i);
            TransactionData tx = new TransactionData(rlpTxData);
            transactions.add(tx);
        }

        parsed = true;
    }

    public List<TransactionData> getTransactions() {

        if (!parsed) parseRLP();
        return transactions;
    }

    @Override
    public byte[] getPayload() {
        return null;
    }

    public String toString(){

        if(!parsed)parseRLP();

        StringBuffer sb = new StringBuffer();

        for (TransactionData transactionData : transactions){

            sb.append("   ").append(transactionData).append("\n");
        }

        return "Transactions Message [\n" + sb.toString() + " ]";
    }
}
