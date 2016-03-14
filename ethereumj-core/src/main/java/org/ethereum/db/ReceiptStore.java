package org.ethereum.db;

import org.ethereum.core.TransactionReceipt;
import org.ethereum.datasource.KeyValueDataSource;

import java.util.List;

/**
 * Created by Ruben on 6/1/2016.
 * Interface used for store transaction receipts
 */

public interface ReceiptStore {

    public void add(byte[] blockHash, int transactionIndex, TransactionReceipt receipt);

    public TransactionInfo get(byte[] transactionHash);

    public void saveMultiple(byte[] blockHash, List<TransactionReceipt> receipts);
}
