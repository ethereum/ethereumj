package org.ethereum.jsonrpc;

import org.ethereum.core.Block;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.db.TransactionInfo;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.LogInfo;

import java.lang.reflect.Array;
import java.math.BigInteger;

import static org.ethereum.jsonrpc.TypeConverter.toJsonHex;

/**
 * Created by Ruben on 5/1/2016.
 */
public class TransactionReceiptDTO {

    public String transactionHash;  // hash of the transaction.
    public int transactionIndex;    // integer of the transactions index position in the block.
    public String blockHash;        // hash of the block where this transaction was in.
    public long blockNumber;         // block number where this transaction was in.
    public long cumulativeGasUsed;   // The total amount of gas used when this transaction was executed in the block.
    public long gasUsed;             //The amount of gas used by this specific transaction alone.
    public String contractAddress; // The contract address created, if the transaction was a contract creation, otherwise  null .
    public String[] logs;         // Array of log objects, which this transaction generated.

    public  TransactionReceiptDTO(Block block, TransactionInfo txInfo){
        TransactionReceipt receipt = txInfo.getReceipt();

        transactionHash = toJsonHex(receipt.getTransaction().getHash());
        transactionIndex = txInfo.getIndex();
        blockHash = toJsonHex(txInfo.getBlockHash());
        blockNumber = block.getNumber();
        cumulativeGasUsed = new BigInteger(receipt.getCumulativeGas()).longValue();
        gasUsed = new BigInteger(receipt.getGasUsed()).longValue();
        if (receipt.getTransaction().getContractAddress() != null)
            contractAddress = toJsonHex(receipt.getTransaction().getContractAddress());
        logs = new String[0];
        for (LogInfo logInfo : receipt.getLogInfoList()) {
            // TODO: Add logs to DTO
        }
    }
}
