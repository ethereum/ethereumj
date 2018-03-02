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
package org.ethereum.core.casper;
import org.ethereum.core.Block;
import org.ethereum.core.CommonTransactionExecutor;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutionSummary;
import org.ethereum.casper.CasperHybridConsensusStrategy;
import org.ethereum.db.BlockStore;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;
import static org.ethereum.util.BIUtil.toBI;

public class CasperTransactionExecutor extends CommonTransactionExecutor {

    public CasperTransactionExecutor(Transaction tx, byte[] coinbase, Repository track, BlockStore blockStore,
                                     ProgramInvokeFactory programInvokeFactory, Block currentBlock) {

        super(tx, coinbase, track, blockStore, programInvokeFactory, currentBlock, new EthereumListenerAdapter(), 0);
    }

    public CasperTransactionExecutor(Transaction tx, byte[] coinbase, Repository track, BlockStore blockStore,
                                     ProgramInvokeFactory programInvokeFactory, Block currentBlock,
                                     EthereumListener listener, long gasUsedInTheBlock) {

        super(tx, coinbase, track, blockStore, programInvokeFactory, currentBlock, listener, gasUsedInTheBlock);
    }

    @Override
    public void init() {
        super.init();
        // Already failed on common validation
        if (execError != null) {
            return;
        }

        // More validations for Casper

        // EIP 208
        if (Arrays.equals(tx.getSender(), Transaction.NULL_SENDER) && toBI(tx.getNonce()).compareTo(BigInteger.ZERO) > 0) {
            execError(String.format("Null sender transaction should use 0 nonce, %s instead", toBI(tx.getNonce())));

            return;
        }

        // EIP 208
        if (Arrays.equals(tx.getSender(), Transaction.NULL_SENDER) &&
                (toBI(tx.getValue()).compareTo(BigInteger.ZERO) > 0 || toBI(tx.getGasPrice()).compareTo(BigInteger.ZERO) > 0)) {
            execError(String.format("Null sender transaction should have 0 value (actual %s), " +
                    "and 0 gasprice (actual: %s)", toBI(tx.getValue()), toBI(tx.getGasPrice())));

            return;
        }
    }

    @Override
    protected boolean isSignatureValid() {
        return isCasperVote() || super.isSignatureValid();
    }

    @Override
    public void execute() {

        if (!readyToExecute) return;

        if (!localCall && !isCasperVote()) {
            track.increaseNonce(tx.getSender());

            BigInteger txGasLimit = toBI(tx.getGasLimit());
            BigInteger txGasCost = toBI(tx.getGasPrice()).multiply(txGasLimit);
            track.addBalance(tx.getSender(), txGasCost.negate());

            if (logger.isInfoEnabled())
                logger.info("Paying: txGasCost: [{}], gasPrice: [{}], gasLimit: [{}]", txGasCost, toBI(tx.getGasPrice()), txGasLimit);
        }

        if (tx.isContractCreation()) {
            create();
        } else {
            call();
        }
    }

    private boolean isCasperVote() {
        if (!(commonConfig.consensusStrategy() instanceof CasperHybridConsensusStrategy))
            return false;
        return isCasperVote(tx, config.getCasperAddress());
    }

    public static boolean isCasperVote(Transaction transaction, byte[] casperAddress) {
        if (!Arrays.equals(transaction.getSender(), Transaction.NULL_SENDER))
            return false;
        if (casperAddress == null)
            return false;
        if (!Arrays.equals(transaction.getReceiveAddress(), casperAddress))
            return false;

        byte[] dataCopy = new byte[4];
        System.arraycopy(transaction.getData(), 0, dataCopy, 0, 4);
        return Arrays.equals(dataCopy, new byte[] {(byte) 0xe9, (byte) 0xdc, 0x06, 0x14});
    }

    @Override
    protected void payRewards(final TransactionExecutionSummary summary) {
        if (execError == null && isCasperVote()) {
            // Return money to sender for succesful Casper vote
            track.addBalance(tx.getSender(), summary.getFee());
            logger.info("Refunded successful Casper Vote from [{}]", Hex.toHexString(tx.getSender()));
        } else {
            // Transfer fees to miner
            track.addBalance(coinbase, summary.getFee());
            touchedAccounts.add(coinbase);
            logger.info("Pay fees to miner: [{}], feesEarned: [{}]", Hex.toHexString(coinbase), summary.getFee());
        }
    }

    @Override
    public long getGasUsed() {
        long gasUsed = super.getGasUsed();
        // Successful Casper vote 0 cost
        if (getResult() != null && execError == null && isCasperVote()) {
            gasUsed = 0;
        }
        return gasUsed;
    }
}
