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

import org.ethereum.db.BlockStore;
import org.ethereum.db.TransactionStore;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.listener.BlockReplayer;
import org.ethereum.publish.event.BlockAdded;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import static org.ethereum.publish.Subscription.to;
import static org.ethereum.publish.event.Events.Type.BLOCK_ADDED;

public class BlockReplaySample extends SingleMinerNetSample {

    @Autowired
    private BlockStore blockStore;
    @Autowired
    private TransactionStore transactionStore;
    private BlockReplayer replay;

    @Override
    protected void onSampleReady() {
        ethereum.subscribe(to(BLOCK_ADDED, this::enableReplay)
                .oneOff(data -> data.getBlockSummary().getBlock().getNumber() % 50 == 0));
    }

    private void enableReplay(BlockAdded.Data data) {
        long startBlockNumber = data.getBlockSummary().getBlock().getNumber() - 25;
        this.replay = BlockReplayer.startFrom(startBlockNumber)
                .withStores(blockStore, transactionStore)
                .withHandler(this::onBlock)
                .replayAsyncAt(ethereum);
    }

    private void onBlock(BlockAdded.Data data) {
        long blockNumber = data.getBlockSummary().getBlock().getNumber();
        if (replay.isDone()) {
            logger.info("Live chain block #{} handled.", blockNumber);
        } else {
            logger.info("Replayed block #{} handled.", blockNumber);
        }
    }

    public static void main(String[] args) {

        class Config extends SingleMinerNetSample.Config {

            @Bean
            @Override
            public SingleMinerNetSample sample() {
                return new BlockReplaySample();
            }

        }

        EthereumFactory.createEthereum(Config.class);
    }
}
