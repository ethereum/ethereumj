package org.ethereum.util.blockchain;

import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;

import java.math.BigInteger;

/**
 * Created by Arsalan on 2017-04-20.
 */
class PendingTx {
    ECKey sender;
    byte[] toAddress;
    BigInteger value;
    byte[] data;

    StandaloneBlockchain.SolidityContractImpl createdContract;
    StandaloneBlockchain.SolidityContractImpl targetContract;

    Transaction customTx;

    TransactionResult txResult = new TransactionResult();

    public PendingTx(ECKey txSender, byte[] toAddress, BigInteger value, byte[] data) {
        this.sender = txSender;
        this.toAddress = toAddress;
        this.value = value;
        this.data = data;
    }

    public PendingTx(ECKey txSender, byte[] toAddress, BigInteger value, byte[] data,
                     StandaloneBlockchain.SolidityContractImpl createdContract, StandaloneBlockchain.SolidityContractImpl targetContract, TransactionResult res) {
        this.sender = txSender;
        this.toAddress = toAddress;
        this.value = value;
        this.data = data;
        this.createdContract = createdContract;
        this.targetContract = targetContract;
        this.txResult = res;
    }

    public PendingTx(Transaction customTx) {
        this.customTx = customTx;
    }
}
