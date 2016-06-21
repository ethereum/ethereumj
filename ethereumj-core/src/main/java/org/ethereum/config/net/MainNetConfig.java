package org.ethereum.config.net;

import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.blockchain.HomesteadConfig;
import org.ethereum.config.blockchain.HomesteadDAOConfig;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class MainNetConfig extends AbstractNetConfig {
    public static final MainNetConfig INSTANCE = new MainNetConfig();

    public MainNetConfig() {
        add(0, new FrontierConfig());
        add(1_150_000, new HomesteadConfig());
        add(HomesteadDAOConfig.DAO_RESCUE_BLOCK, new HomesteadDAOConfig());
    }
}
