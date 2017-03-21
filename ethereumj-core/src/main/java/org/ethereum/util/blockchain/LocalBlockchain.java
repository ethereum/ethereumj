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
package org.ethereum.util.blockchain;

import org.ethereum.core.Block;

/**
 * This interface is implemented by the locally created blockchain
 * where block issuance can be controlled.
 *
 * All the pending transactions submitted via EasyBlockchain are
 * buffered and become part of the blockchain as soon as
 * a new block is generated
 *
 * Created by Anton Nashatyrev on 24.03.2016.
 */
public interface LocalBlockchain extends EasyBlockchain {

    /**
     * Creates a new block which includes all the transactions
     * created via EasyBlockchain since the last created block
     * The pending transaction list is cleared.
     * The current best block on the chain becomes a parent of the
     * created block
     */
    Block createBlock();

    /**
     * The same as previous but the block parent is specified explicitly
     * This is handy for test/experiments with the chain fork branches
     */
    Block createForkBlock(Block parent);
}
