package org.ethereum.core;

import org.ethereum.util.RLP;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 06/06/2014 04:03
 */

public class TransactionRecipe {

    private Transaction transaction;
    private byte[] postTxState;
    private byte[] cumulativeGas;

    /* Tx Recipe in encoded form */
    private byte[] rlpEncoded;


    public TransactionRecipe(Transaction transaction, byte[] postTxState, byte[] cumulativeGas) {
        this.transaction = transaction;
        this.postTxState = postTxState;
        this.cumulativeGas = cumulativeGas;
    }

    public byte[] getEncoded(){

        if(rlpEncoded != null) return rlpEncoded;

        byte[] transactionEl   = transaction.getEncoded();
        byte[] postTxStateEl   = RLP.encodeElement(this.postTxState);
        byte[] cumulativeGasEl = RLP.encodeElement(this.cumulativeGas);

        rlpEncoded = RLP.encodeList(transactionEl, postTxStateEl, cumulativeGasEl);

        return rlpEncoded;
    }

    @Override
    public String toString() {
        return "TransactionRecipe[" +
                "\n   " + transaction +
                "\n  , postTxState=" + Hex.toHexString(postTxState) +
                "\n  , cumulativeGas=" + Hex.toHexString(cumulativeGas) +
                ']';
    }
}
