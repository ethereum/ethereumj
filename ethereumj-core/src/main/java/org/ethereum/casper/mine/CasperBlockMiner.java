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
package org.ethereum.casper.mine;

import org.ethereum.casper.config.CasperProperties;
import org.ethereum.casper.core.CasperFacade;
import org.ethereum.casper.core.CasperTransactionExecutor;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.core.PendingState;
import org.ethereum.core.PendingStateImpl;
import org.ethereum.core.Transaction;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.mine.BlockMiner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CasperBlockMiner extends BlockMiner {
    @Autowired
    CasperFacade casper;

    public CasperBlockMiner(SystemProperties config, CompositeEthereumListener listener,
                            Blockchain blockchain, PendingState pendingState) {
        super(config, listener, blockchain, pendingState);
    }

    @Override
    protected boolean isAcceptableTx(Transaction tx) {
        if (CasperTransactionExecutor.isCasperVote(tx, ((CasperProperties) config).getCasperAddress())) {
            return true;
        }
        return super.isAcceptableTx(tx);
    }

    @Override
    protected Block getNewBlockForMining() {
        Block bestBlockchain = blockchain.getBestBlock();
        Block bestPendingState = ((PendingStateImpl) pendingState).getBestBlock();

        logger.debug("getNewBlockForMining best blocks: PendingState: " + bestPendingState.getShortDescr() +
                ", Blockchain: " + bestBlockchain.getShortDescr());

        // Casper txs should come after regular
        List<Transaction> pendingTxs = getAllPendingTransactions();
        pendingTxs.sort((tx1, tx2) -> {
            boolean tx1isVote = CasperTransactionExecutor.isCasperVote(tx1, casper.getAddress());
            boolean tx2isVote = CasperTransactionExecutor.isCasperVote(tx2, casper.getAddress());
            return Boolean.compare(tx1isVote, tx2isVote);
        });

        Block newMiningBlock = blockchain.createNewBlock(bestPendingState, pendingTxs,
                getUncles(bestPendingState));
        return newMiningBlock;
    }
}
