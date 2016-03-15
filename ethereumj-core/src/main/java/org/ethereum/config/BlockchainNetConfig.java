package org.ethereum.config;

import org.ethereum.core.BlockHeader;

/**
 * Describes a set of configs for a specific blockchain depending on the block number
 * E.g. the main Ethereum net has at least FrontierConfig and HomesteadConfig depending on the block
 *
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public interface BlockchainNetConfig {

    /**
     * Get the config for the specific block
     */
    BlockchainConfig getConfigForBlock(long blockNumber);

    /**
     * Returns the constants common for all the blocks in this blockchain
     */
    Constants getCommonConstants();
}
