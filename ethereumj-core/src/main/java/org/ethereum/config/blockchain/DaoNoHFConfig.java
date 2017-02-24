package org.ethereum.config.blockchain;

import org.ethereum.config.BlockchainConfig;

/**
 * Created by Anton Nashatyrev on 18.07.2016.
 */
public class DaoNoHFConfig extends AbstractDaoConfig {

    {
        supportFork = false;
    }

    public DaoNoHFConfig() {
        initDaoConfig(new HomesteadConfig(), ETH_FORK_BLOCK_NUMBER);
    }

    public DaoNoHFConfig(BlockchainConfig parent, long forkBlockNumber) {
        initDaoConfig(parent, forkBlockNumber);
    }

    @Override
    public String toString() {
        return super.toString() + "(forkBlock:" + forkBlockNumber + ")";
    }
}
