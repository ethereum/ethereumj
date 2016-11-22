package org.ethereum.config.blockchain;

import org.ethereum.config.Constants;

/**
 * Created by Anton Nashatyrev on 21.11.2016.
 */
public class RopstenConfig extends HomesteadConfig {

    public RopstenConfig() {
    }

    public RopstenConfig(Constants constants) {
        super(constants);
    }

    @Override
    public Integer getChainId() {
        return 3;
    }
}
