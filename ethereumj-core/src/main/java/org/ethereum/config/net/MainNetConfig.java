package org.ethereum.config.net;

import org.ethereum.config.blockchain.DaoHFConfig;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.blockchain.HomesteadConfig;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class MainNetConfig extends AbstractNetConfig {
    public static final MainNetConfig INSTANCE = new MainNetConfig();

    public MainNetConfig() {
        add(0, new FrontierConfig());
        add(1_150_000, new HomesteadConfig());
        add(1_920_000, new DaoHFConfig());
    }
}
