package org.ethereum.config.fork;

import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.blockchain.HomesteadConfig;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class TestNetForkConfig extends AbstractForkConfig {
    public TestNetForkConfig() {
        add(0, new FrontierConfig());
    }
}
