package org.ethereum.samples;

import org.ethereum.db.BlockStore;
import org.ethereum.db.TransactionStore;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.listener.BlockReplayer;
import org.ethereum.publish.event.BlockAdded;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import static org.ethereum.publish.Subscription.to;
import static org.ethereum.publish.event.Events.Type.BLOCK_ADED;

public class BlockReplaySample extends SingleMinerNetSample {

    @Autowired
    private BlockStore blockStore;
    @Autowired
    private TransactionStore transactionStore;
    private BlockReplayer replay;

    @Override
    protected void onSampleReady() {
        ethereum.subscribe(to(BLOCK_ADED, this::enableReplay)
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
