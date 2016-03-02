package org.ethereum.config.net;

import org.ethereum.config.blockchain.FrontierConfig;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class TestNetConfig extends AbstractNetConfig {
    public TestNetConfig() {
        add(0, new FrontierConfig());
    }
}
