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
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.facade.Repository;
import org.ethereum.publish.event.BlockAdded;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static org.ethereum.publish.Subscription.to;

public class FollowAccount {

    public static void main(String[] args) {
        Ethereum ethereum = EthereumFactory.createEthereum();
        ethereum.subscribe(to(BlockAdded.class, blockSummary -> {
            byte[] cow = Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826");

            // Get snapshot some time ago - 10% blocks ago
            long bestNumber = ethereum.getBlockchain().getBestBlock().getNumber();
            long oldNumber = (long) (bestNumber * 0.9);

            Block oldBlock = ethereum.getBlockchain().getBlockByNumber(oldNumber);

            Repository repository = ethereum.getRepository();
            Repository snapshot = ethereum.getSnapshotTo(oldBlock.getStateRoot());

            BigInteger nonce_ = snapshot.getNonce(cow);
            BigInteger nonce = repository.getNonce(cow);

            System.err.printf(" #%d [cd2a3d9] => snapshot_nonce:%d latest_nonce:%d\n",
                    blockSummary.getBlock().getNumber(), nonce_, nonce);
        }));
    }
}
