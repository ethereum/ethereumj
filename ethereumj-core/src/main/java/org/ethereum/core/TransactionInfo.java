/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.core;

import org.ethereum.core.Bloom;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.vm.LogInfo;

import java.math.BigInteger;

/**
 * Contains Transaction execution info:
 * its receipt and execution context
 * If the transaction is still in pending state the context is the
 * hash of the parent block on top of which the transaction was executed
 * If the transaction is already mined into a block the context
 * is the containing block and the index of the transaction in that block
 *
 * Created by Ruben on 8/1/2016.
 */
public class TransactionInfo {

    TransactionReceipt receipt;
    byte[] blockHash;
    // user for pending transaction
    byte[] parentBlockHash;
    int index;

    public TransactionInfo(TransactionReceipt receipt, byte[] blockHash, int index) {
        this.receipt = receipt;
        this.blockHash = blockHash;
        this.index = index;
    }

    /**
     * Creates a pending tx info
     */
    public TransactionInfo(TransactionReceipt receipt) {
        this.receipt = receipt;
    }

    public TransactionInfo(byte[] rlp) {
        RLPList params = RLP.decode2(rlp);
        RLPList txInfo = (RLPList) params.get(0);
        RLPList receiptRLP = (RLPList) txInfo.get(0);
        RLPItem blockHashRLP  = (RLPItem) txInfo.get(1);
        RLPItem indexRLP = (RLPItem) txInfo.get(2);

        receipt = new TransactionReceipt(receiptRLP.getRLPData());
        blockHash = blockHashRLP.getRLPData();
        if (indexRLP.getRLPData() == null)
            index = 0;
        else
            index = new BigInteger(1, indexRLP.getRLPData()).intValue();
    }

    public void setTransaction(Transaction tx){
        receipt.setTransaction(tx);
    }

    /* [receipt, blockHash, index] */
    public byte[] getEncoded() {

        byte[] receiptRLP = this.receipt.getEncoded();
        byte[] blockHashRLP = RLP.encodeElement(blockHash);
        byte[] indexRLP = RLP.encodeInt(index);

        byte[] rlpEncoded = RLP.encodeList(receiptRLP, blockHashRLP, indexRLP);

        return rlpEncoded;
    }

    public TransactionReceipt getReceipt(){
        return receipt;
    }

    public byte[] getBlockHash() { return blockHash; }

    public byte[] getParentBlockHash() {
        return parentBlockHash;
    }

    public void setParentBlockHash(byte[] parentBlockHash) {
        this.parentBlockHash = parentBlockHash;
    }

    public int getIndex() { return index; }

    public boolean isPending() {
        return blockHash == null;
    }
}
