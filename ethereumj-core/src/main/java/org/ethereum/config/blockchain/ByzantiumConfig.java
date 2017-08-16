package org.ethereum.config.blockchain;

import org.ethereum.config.BlockchainConfig;

/**
 * Created by Anton Nashatyrev on 15.08.2017.
 */
public class ByzantiumConfig extends Eip160HFConfig {

    public ByzantiumConfig(BlockchainConfig parent) {
        super(parent);
    }

    @Override
    public boolean eip140() {
        return true;
    }
}
