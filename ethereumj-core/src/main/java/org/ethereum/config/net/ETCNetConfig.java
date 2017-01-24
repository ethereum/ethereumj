package org.ethereum.config.net;

import org.ethereum.config.blockchain.*;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class ETCNetConfig extends BaseNetConfig {
    public static final ETCNetConfig INSTANCE = new ETCNetConfig();

    public ETCNetConfig() {
        add(0, new FrontierConfig());
        add(1_150_000, new HomesteadConfig());
        add(1_920_000, new DaoNoHFConfig());
        add(2_500_000, new Eip150HFConfig(new DaoNoHFConfig()));
        add(3_000_000, new ETCFork3M(new DaoNoHFConfig()));
    }
}
