package org.ethereum.mine;

import com.google.common.util.concurrent.ListenableFuture;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;

/**
 * The adapter of Ethash for MinerIfc
 *
 * Created by Anton Nashatyrev on 26.02.2016.
 */
public class EthashMiner implements MinerIfc {

    SystemProperties config;

    private int cpuThreads;
    private boolean fullMining = true;

    public EthashMiner(SystemProperties config) {
        this.config = config;
        cpuThreads = config.getMineCpuThreads();
        fullMining = config.isMineFullDataset();
    }

    @Override
    public ListenableFuture<Long> mine(Block block) {
        return fullMining ?
                Ethash.getForBlock(config, block.getNumber()).mine(block, cpuThreads) :
                Ethash.getForBlock(config, block.getNumber()).mineLight(block, cpuThreads);
    }

    @Override
    public boolean validate(BlockHeader blockHeader) {
        return Ethash.getForBlock(config, blockHeader.getNumber()).validate(blockHeader);
    }
}
