package org.ethereum.core;

import org.ethereum.util.RLP;
import org.spongycastle.util.encoders.Hex;

/**
 * The transaction receipt is a tuple of three items 
 * comprising the transaction, together with the post-transaction state, 
 * and the cumulative gas used in the block containing the transaction receipt 
 * as of immediately after the transaction has happened,
 */
public class TransactionReceipt {

    private Transaction transaction;
    private byte[] postTxState;
    private byte[] cumulativeGas;

    /* Tx Receipt in encoded form */
    private byte[] rlpEncoded;

    public TransactionReceipt(Transaction transaction, byte[] postTxState, byte[] cumulativeGas) {
        this.transaction = transaction;
        this.postTxState = postTxState;
        this.cumulativeGas = cumulativeGas;
    }

    public byte[] getEncoded() {

        if(rlpEncoded != null) return rlpEncoded;

        byte[] transactionEl   = transaction.getEncoded();
        byte[] postTxStateEl   = RLP.encodeElement(this.postTxState);
        byte[] cumulativeGasEl = RLP.encodeElement(this.cumulativeGas);

        rlpEncoded = RLP.encodeList(transactionEl, postTxStateEl, cumulativeGasEl);

        return rlpEncoded;
    }

    @Override
    public String toString() {
        return "TransactionReceipt[" +
                "\n   " + transaction +
                "\n  , postTxState=" + Hex.toHexString(postTxState) +
                "\n  , cumulativeGas=" + Hex.toHexString(cumulativeGas) +
                ']';
    }
}
