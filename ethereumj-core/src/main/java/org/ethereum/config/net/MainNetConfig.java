package org.ethereum.config.net;

import org.ethereum.config.blockchain.*;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class MainNetConfig extends AbstractNetConfig {
    public static final MainNetConfig INSTANCE = new MainNetConfig();

    public MainNetConfig() {
        add(0, new FrontierConfig());
        add(1_150_000, new HomesteadConfig());
        add(1_920_000, new DaoHFConfig());
        add(2_463_000, new Eip150HFConfig(new DaoHFConfig()));
        add(2_675_000, new Eip160HFConfig(new DaoHFConfig()));
    }
}
