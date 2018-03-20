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
package org.ethereum.casper.core;
import org.ethereum.casper.config.CasperProperties;
import org.ethereum.core.Block;
import org.ethereum.core.CommonTransactionExecutor;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutionSummary;
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
        return CasperFacade.isVote(tx, ((CasperProperties) config).getCasperAddress()) || super.isSignatureValid();
    }

    @Override
    public void execute() {

        if (!readyToExecute) return;

        if (!localCall && !isCasperServiceTx()) {
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

    private boolean isCasperServiceTx() {
        return CasperFacade.isServiceTx(tx, ((CasperProperties) config).getCasperAddress());
    }

    @Override
    protected void payRewards(final TransactionExecutionSummary summary) {
        if (execError == null && isCasperServiceTx()) {
            // Return money to sender for service Casper tx
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
        if (getResult() != null && execError == null && isCasperServiceTx()) {
            gasUsed = 0;
        }
        return gasUsed;
    }
}
