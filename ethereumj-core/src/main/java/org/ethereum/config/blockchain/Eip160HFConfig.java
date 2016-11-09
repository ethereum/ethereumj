package org.ethereum.config.blockchain;

import org.ethereum.config.BlockchainConfig;

/**
 * Created by Anton Nashatyrev on 14.10.2016.
 */
public class Eip160HFConfig extends Eip150HFConfig {

    public Eip160HFConfig(BlockchainConfig parent) {
        super(parent);
    }

    @Override
    public boolean noEmptyAccounts() {
        return true;
    }
}
