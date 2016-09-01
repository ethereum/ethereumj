package org.ethereum.config.net;

import org.ethereum.config.blockchain.DaoNoHFConfig;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.blockchain.HomesteadConfig;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class ETCNetConfig extends AbstractNetConfig {
    public static final ETCNetConfig INSTANCE = new ETCNetConfig();

    public ETCNetConfig() {
        add(0, new FrontierConfig());
        add(1_150_000, new HomesteadConfig());
        add(1_920_000, new DaoNoHFConfig());
    }
}
