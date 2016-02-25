package org.ethereum.config.fork;

import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.blockchain.HomesteadConfig;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class MainForkConfig extends AbstractForkConfig {
    public static final MainForkConfig INSTANCE = new MainForkConfig();

    public MainForkConfig() {
        add(0, new FrontierConfig());
        add(1_150_000, new HomesteadConfig());
    }
}
