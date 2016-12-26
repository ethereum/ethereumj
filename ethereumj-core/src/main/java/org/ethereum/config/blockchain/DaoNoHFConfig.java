package org.ethereum.config.blockchain;

/**
 * Created by Anton Nashatyrev on 18.07.2016.
 */
public class DaoNoHFConfig extends AbstractDaoConfig {

    public DaoNoHFConfig() {
        initDaoConfig(ETH_FORK_BLOCK_NUMBER, false);
    }

    public DaoNoHFConfig(long forkBlockNumber) {
        initDaoConfig(forkBlockNumber, false);
    }

    @Override
    public String toString() {
        return super.toString() + "(forkBlock:" + forkBlockNumber + ")";
    }
}
