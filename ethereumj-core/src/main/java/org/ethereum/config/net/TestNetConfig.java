package org.ethereum.config.net;

import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.blockchain.HomesteadConfig;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class TestNetConfig extends BaseNetConfig {
    public TestNetConfig() {
        add(0, new FrontierConfig());
        add(1_150_000, new HomesteadConfig());
    }
}
