package org.ethereum.core;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.vm.LogInfo;

import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import java.util.ArrayList;
import java.util.List;

import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;

/**
 * The transaction receipt is a tuple of three items
 * comprising the transaction, together with the post-transaction state,
 * and the cumulative gas used in the block containing the transaction receipt
 * as of immediately after the transaction has happened,
 */
public class TransactionReceipt {

    private Transaction transaction;

    private byte[] postTxState = EMPTY_BYTE_ARRAY;
    private byte[] cumulativeGas = EMPTY_BYTE_ARRAY;
    private Bloom bloomFilter = new Bloom();
    private List<LogInfo> logInfoList = new ArrayList<>();

    private byte[] gasUsed = EMPTY_BYTE_ARRAY;
    private byte[] executionResult = EMPTY_BYTE_ARRAY;

    /* Tx Receipt in encoded form */
    private byte[] rlpEncoded;

    public TransactionReceipt() {
    }

    public TransactionReceipt(byte[] rlp) {

        RLPList params = RLP.decode2(rlp);
        RLPList receipt = (RLPList) params.get(0);

        RLPItem postTxStateRLP = (RLPItem) receipt.get(0);
        RLPItem cumulativeGasRLP = (RLPItem) receipt.get(1);
        RLPItem bloomRLP = (RLPItem) receipt.get(2);
        RLPList logs = (RLPList) receipt.get(3);
        RLPItem gasUsedRLP = (RLPItem) receipt.get(4);
        RLPItem result = (RLPItem) receipt.get(5);

        postTxState = postTxStateRLP.getRLPData();
        cumulativeGas = cumulativeGasRLP.getRLPData();
        bloomFilter = new Bloom(bloomRLP.getRLPData());
        gasUsed = gasUsedRLP.getRLPData();
        executionResult = result.getRLPData();

        for (RLPElement log : logs) {
            LogInfo logInfo = new LogInfo(log.getRLPData());
            logInfoList.add(logInfo);
        }

        rlpEncoded = rlp;
    }


    public TransactionReceipt(byte[] postTxState, byte[] cumulativeGas,
                              Bloom bloomFilter, List<LogInfo> logInfoList) {
        this.postTxState = postTxState;
        this.cumulativeGas = cumulativeGas;
        this.bloomFilter = bloomFilter;
        this.logInfoList = logInfoList;
    }

    public byte[] getPostTxState() {
        return postTxState;
    }

    public byte[] getCumulativeGas() {
        return cumulativeGas;
    }

    public byte[] getGasUsed() {
        return gasUsed;
    }

    public byte[] getExecutionResult() {
        return executionResult;
    }

    public long getCumulativeGasLong() {
        return new BigInteger(1, cumulativeGas).longValue();
    }


    public Bloom getBloomFilter() {
        return bloomFilter;
    }

    public List<LogInfo> getLogInfoList() {
        return logInfoList;
    }


    /**
     *  Used for Receipt trie hash calculation. Should contain only the following items encoded:
     *  [postTxState, cumulativeGas, bloomFilter, logInfoList]
     */
    public byte[] getReceiptTrieEncoded() {
        return getEncoded(true);
    }

    /**
     * Used for serialization, contains all the receipt data encoded
     */
    public byte[] getEncoded() {
        if (rlpEncoded == null) {
            rlpEncoded = getEncoded(false);
        }

        return rlpEncoded;
    }

    private byte[] getEncoded(boolean receiptTrie) {

        byte[] postTxStateRLP = RLP.encodeElement(this.postTxState);
        byte[] cumulativeGasRLP = RLP.encodeElement(this.cumulativeGas);
        byte[] bloomRLP = RLP.encodeElement(this.bloomFilter.data);

        final byte[] logInfoListRLP;
        if (logInfoList != null) {
            byte[][] logInfoListE = new byte[logInfoList.size()][];

            int i = 0;
            for (LogInfo logInfo : logInfoList) {
                logInfoListE[i] = logInfo.getEncoded();
                ++i;
            }
            logInfoListRLP = RLP.encodeList(logInfoListE);
        } else {
            logInfoListRLP = RLP.encodeList();
        }

        return receiptTrie ?
                RLP.encodeList(postTxStateRLP, cumulativeGasRLP, bloomRLP, logInfoListRLP):
                RLP.encodeList(postTxStateRLP, cumulativeGasRLP, bloomRLP, logInfoListRLP,
                        RLP.encodeElement(gasUsed), RLP.encodeElement(executionResult));

    }

    public void setPostTxState(byte[] postTxState) {
        this.postTxState = postTxState;
        rlpEncoded = null;
    }

    public void setCumulativeGas(long cumulativeGas) {
        this.cumulativeGas = BigIntegers.asUnsignedByteArray(BigInteger.valueOf(cumulativeGas));
        rlpEncoded = null;
    }

    public void setCumulativeGas(byte[] cumulativeGas) {
        this.cumulativeGas = cumulativeGas;
        rlpEncoded = null;
    }

    public void setGasUsed(byte[] gasUsed) {
        this.gasUsed = gasUsed;
        rlpEncoded = null;
    }

    public void setGasUsed(long gasUsed) {
        this.gasUsed = BigIntegers.asUnsignedByteArray(BigInteger.valueOf(gasUsed));
        rlpEncoded = null;
    }

    public void setExecutionResult(byte[] executionResult) {
        this.executionResult = executionResult;
        rlpEncoded = null;
    }

    public void setLogInfoList(List<LogInfo> logInfoList) {
        if (logInfoList == null) return;
        this.logInfoList = logInfoList;

        for (LogInfo loginfo : logInfoList) {
            bloomFilter.or(loginfo.getBloom());
        }
        rlpEncoded = null;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    @Override
    public String toString() {

        // todo: fix that

        return "TransactionReceipt[" +
                "\n  , postTxState=" + Hex.toHexString(postTxState) +
                "\n  , cumulativeGas=" + Hex.toHexString(cumulativeGas) +
                "\n  , cumulativeGas=" + Hex.toHexString(gasUsed) +
                "\n  , executionResult=" + Hex.toHexString(executionResult) +
                "\n  , bloom=" + bloomFilter.toString() +
                "\n  , logs=" + logInfoList +
                ']';
    }

}
