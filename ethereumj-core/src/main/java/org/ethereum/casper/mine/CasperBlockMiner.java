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

import org.ethereum.casper.core.CasperTransactionExecutor;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Blockchain;
import org.ethereum.core.PendingState;
import org.ethereum.core.Transaction;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.mine.BlockMiner;

public class CasperBlockMiner extends BlockMiner {

    public CasperBlockMiner(SystemProperties config, CompositeEthereumListener listener,
                            Blockchain blockchain, PendingState pendingState) {
        super(config, listener, blockchain, pendingState);
    }

    @Override
    protected boolean isAcceptableTx(Transaction tx) {
        if (CasperTransactionExecutor.isCasperVote(tx, config.getCasperAddress())) {
            return true;
        }
        return super.isAcceptableTx(tx);
    }
}
