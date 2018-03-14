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
import org.ethereum.core.PendingStateImpl;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutor;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.util.ByteUtil;

public class CasperPendingStateImpl extends PendingStateImpl {

    public CasperPendingStateImpl(EthereumListener listener) {
        super(listener);
    }

    @Override
    protected boolean receiptIsValid(TransactionReceipt receipt) {
        boolean isValid = super.receiptIsValid(receipt);
        if (isValid) {
            return true;
        } else if (CasperFacade.isVote(receipt.getTransaction(), ((CasperProperties) config).getCasperAddress())) {
            return receipt.isSuccessful();
        }

        return false;
    }

    @Override
    protected String validate(Transaction tx) {
        try {
            tx.verify();
        } catch (Exception e) {
            return String.format("Invalid transaction: %s", e.getMessage());
        }

        if (CasperFacade.isVote(tx, ((CasperProperties) config).getCasperAddress())) {
            return null;  // Doesn't require more checks
        }

        if (config.getMineMinGasPrice().compareTo(ByteUtil.bytesToBigInteger(tx.getGasPrice())) > 0) {
            return "Too low gas price for transaction: " + ByteUtil.bytesToBigInteger(tx.getGasPrice());
        }

        return null;
    }


    @Override
    protected TransactionExecutor createTransactionExecutor(Transaction transaction, byte[] minerCoinbase,
                                                            Repository track, Block currentBlock) {
        return new CasperTransactionExecutor(transaction, minerCoinbase,
                track, blockStore, programInvokeFactory, currentBlock, new EthereumListenerAdapter(), 0)
                .withCommonConfig(commonConfig);
    }
}
