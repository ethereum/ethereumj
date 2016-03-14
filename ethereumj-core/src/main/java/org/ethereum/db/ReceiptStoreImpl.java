package org.ethereum.db;

import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.datasource.KeyValueDataSource;

import java.util.List;

/**
 * Created by Ruben on 6/1/2016.
 * Class used to store transaction receipts
 */

public class ReceiptStoreImpl implements ReceiptStore {

    private KeyValueDataSource receiptsDS;

    public ReceiptStoreImpl(KeyValueDataSource receiptsDS){
        this.receiptsDS = receiptsDS;
    }

    public void add(byte[] blockHash, int transactionIndex, TransactionReceipt receipt){
        TransactionInfo txInfo = new TransactionInfo(receipt, blockHash, transactionIndex);
        receiptsDS.put(receipt.getTransaction().getHash(), txInfo.getEncoded());
    }

    public TransactionInfo get(byte[] transactionHash){
        byte[] txInfo = receiptsDS.get(transactionHash);
        if (txInfo == null)
            return null;
        return new TransactionInfo(txInfo);
    }

    public void saveMultiple(byte[] blockHash, List<TransactionReceipt> receipts) {
        int i = 0;
        for (TransactionReceipt receipt : receipts) {
            this.add(blockHash, i++, receipt);
        }
    }
}
