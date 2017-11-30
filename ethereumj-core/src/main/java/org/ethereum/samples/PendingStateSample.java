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
package org.ethereum.samples;

import org.ethereum.core.Block;
import org.ethereum.core.PendingState;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.crypto.ECKey;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.math.BigInteger;
import java.util.*;

/**
 * PendingState is the ability to track the changes made by transaction immediately and not wait for
 * the block containing that transaction.
 *
 * This sample connects to the test network (it has a lot of free Ethers) and starts periodically
 * transferring funds to a random address. The pending state is monitored and you may see that
 * while the actual receiver balance remains the same right after transaction sent the pending
 * state reflects balance change immediately.
 *
 * While new blocks are arrived the sample monitors which of our transactions are included ot those blocks.
 * After each 5 transactions the sample stops sending transactions and waits for all transactions
 * are cleared (included to blocks) so the actual and pending receiver balances become equal.
 *
 * Created by Anton Nashatyrev on 05.02.2016.
 */
public class PendingStateSample extends TestNetSample {

    // remember here what transactions have been sent
    // removing transaction from here on block arrival
    private Map<ByteArrayWrapper, Transaction> pendingTxs = Collections.synchronizedMap(
            new HashMap<ByteArrayWrapper, Transaction>());

    @Autowired
    PendingState pendingState;

    // some random receiver
    private byte[] receiverAddress = new ECKey().getAddress();

    @Override
    public void onSyncDone() {
        ethereum.addListener(new EthereumListenerAdapter() {
            // listening here when the PendingState is updated with new transactions
            @Override
            public void onPendingTransactionsReceived(List<Transaction> transactions) {
                for (Transaction tx : transactions) {
                    PendingStateSample.this.onPendingTransactionReceived(tx);
                }
            }

            // when block arrives look for our included transactions
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                PendingStateSample.this.onBlock(block, receipts);
            }
        });

        new Thread(() -> {
            try {
                sendTransactions();
            } catch (Exception e) {
                logger.error("Error while sending transactions", e);
            }
        }, "PendingStateSampleThread").start();
    }

    /**
     * Periodically send value transfer transactions and each 5 transactions
     * wait for all sent transactions to be included into blocks
     */
    void sendTransactions() throws InterruptedException {
        // initial sender nonce needs to be retrieved from the repository
        // for further transactions we just do nonce++
        BigInteger nonce = ethereum.getRepository().getNonce(senderAddress);

        int weisToSend = 100;
        int count = 0;
        while(true) {
            if (count < 5) {
                Transaction tx = new Transaction(
                        ByteUtil.bigIntegerToBytes(nonce),
                        ByteUtil.longToBytesNoLeadZeroes(ethereum.getGasPrice()),
                        ByteUtil.longToBytesNoLeadZeroes(1_000_000),
                        receiverAddress,
                        ByteUtil.longToBytesNoLeadZeroes(weisToSend), new byte[0],
                        ethereum.getChainIdForNextBlock());
                tx.sign(ECKey.fromPrivate(receiverAddress));
                logger.info("<=== Sending transaction: " + tx);
                ethereum.submitTransaction(tx);

                nonce = nonce.add(BigInteger.ONE);
                count++;
            } else {
                if (pendingTxs.size() > 0) {
                    logger.info("Waiting for transaction clearing. " + pendingTxs.size() + " transactions remain.");
                } else {
                    logger.info("All transactions are included to blocks!");
                    count = 0;
                }
            }

            Thread.sleep(7000);
        }
    }

    /**
     *  The PendingState is updated with a new pending transactions.
     *  Prints the current receiver balance (based on blocks) and the pending balance
     *  which should immediately reflect receiver balance change
     */
    void onPendingTransactionReceived(Transaction tx) {
        logger.info("onPendingTransactionReceived: " + tx);
        if (Arrays.equals(tx.getSender(), senderAddress)) {
            BigInteger receiverBalance = ethereum.getRepository().getBalance(receiverAddress);
            BigInteger receiverBalancePending = pendingState.getRepository().getBalance(receiverAddress);
            logger.info(" + New pending transaction 0x" + Hex.toHexString(tx.getHash()).substring(0, 8));

            pendingTxs.put(new ByteArrayWrapper(tx.getHash()), tx);

            logger.info("Receiver pending/current balance: " + receiverBalancePending + " / " + receiverBalance +
                    " (" + pendingTxs.size() + " pending txs)");
        }
    }

    /**
     * For each block we are looking for our transactions and clearing them
     * The actual receiver balance is confirmed upon block arrival
     */
    public void onBlock(Block block, List<TransactionReceipt> receipts) {
        int cleared = 0;
        for (Transaction tx : block.getTransactionsList()) {
            ByteArrayWrapper txHash = new ByteArrayWrapper(tx.getHash());
            Transaction ptx = pendingTxs.get(txHash);
            if (ptx != null) {
                logger.info(" - Pending transaction cleared 0x" + Hex.toHexString(tx.getHash()).substring(0, 8) +
                        " in block " + block.getShortDescr());

                pendingTxs.remove(txHash);
                cleared++;
            }
        }
        BigInteger receiverBalance = ethereum.getRepository().getBalance(receiverAddress);
        BigInteger receiverBalancePending = pendingState.getRepository().getBalance(receiverAddress);
        logger.info("" + cleared + " transactions cleared in the block " + block.getShortDescr());
        logger.info("Receiver pending/current balance: " + receiverBalancePending + " / " + receiverBalance +
                " (" + pendingTxs.size() + " pending txs)");
    }


    public static void main(String[] args) throws Exception {
        sLogger.info("Starting EthereumJ!");

        class Config extends TestNetConfig{
            @Override
            @Bean
            public TestNetSample sampleBean() {
                return new PendingStateSample();
            }
        }

        // Based on Config class the BasicSample would be created by Spring
        // and its springInit() method would be called as an entry point
        EthereumFactory.createEthereum(Config.class);
    }
}
