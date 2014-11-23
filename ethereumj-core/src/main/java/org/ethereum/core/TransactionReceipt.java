package org.ethereum.core;

import org.ethereum.util.RLP;
import org.ethereum.vm.LogInfo;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

/**
 * The transaction receipt is a tuple of three items 
 * comprising the transaction, together with the post-transaction state, 
 * and the cumulative gas used in the block containing the transaction receipt 
 * as of immediately after the transaction has happened,
 *
 *
 */
public class TransactionReceipt {

    private byte[] postTxState;
    private byte[] cumulativeGas;
    private Bloom  bloomFilter;
    private List<LogInfo> logInfoList;

    /* Tx Receipt in encoded form */
    private byte[] rlpEncoded;

    public TransactionReceipt() {
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

    public long getCumulativeGasLong() {
        return new BigInteger(1, cumulativeGas).longValue();
    }


    public Bloom getBloomFilter() {
        return bloomFilter;
    }

    public List<LogInfo> getLogInfoList() {
        return logInfoList;
    }


    /* [postTxState, cumulativeGas, bloomFilter, logInfoList] */
    public byte[] getEncoded() {

        if(rlpEncoded != null) return rlpEncoded;

        byte[] postTxStateRLP   = RLP.encodeElement(this.postTxState);
        byte[] cumulativeGasRLP = RLP.encodeElement(this.cumulativeGas);
        byte[] bloomRLP         = RLP.encodeElement(this.bloomFilter.data);

        byte[][] logInfoListE = new byte[logInfoList.size()][];

        int i = 0;
        for (LogInfo logInfo : logInfoList){
            logInfoListE[i] = logInfo.getEncoded();
            ++i;
        }
        byte[] logInfoListRLP  = RLP.encodeList(logInfoListE);

        rlpEncoded = RLP.encodeList(postTxStateRLP, cumulativeGasRLP, bloomRLP, logInfoListRLP);

        return rlpEncoded;
    }

    public void setPostTxState(byte[] postTxState) {
        this.postTxState = postTxState;
    }

    public void setCumulativeGas(long cumulativeGas) {
        this.cumulativeGas = BigIntegers.asUnsignedByteArray(  BigInteger.valueOf( cumulativeGas ) );
    }

    public void setCumulativeGas(byte[] cumulativeGas) {
        this.cumulativeGas = cumulativeGas;
    }

    public void setBloomFilter(Bloom bloomFilter) {
        this.bloomFilter = bloomFilter;
    }

    public void setLogInfoList(List<LogInfo> logInfoList) {
        this.logInfoList = logInfoList;
    }

    @Override
    public String toString() {

        // todo: fix that

        return "TransactionReceipt[" +
                "\n  , postTxState=" + Hex.toHexString(postTxState) +
                "\n  , cumulativeGas=" + Hex.toHexString(cumulativeGas) +
                "\n  , bloom=" + bloomFilter.toString() +
                "\n  , logs=" + logInfoList +
                ']';
    }

}
