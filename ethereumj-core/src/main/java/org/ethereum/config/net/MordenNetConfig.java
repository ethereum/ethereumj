package org.ethereum.config.net;

import org.ethereum.config.blockchain.Eip150HFConfig;
import org.ethereum.config.blockchain.Eip160HFConfig;
import org.ethereum.config.blockchain.MordenConfig;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class MordenNetConfig extends BaseNetConfig {

    public MordenNetConfig() {
        add(0, new MordenConfig.Frontier());
        add(494_000, new MordenConfig.Homestead());
        add(1_783_000, new Eip150HFConfig(new MordenConfig.Homestead()));
        add(1_885_000, new Eip160HFConfig(new MordenConfig.Homestead()));

    }
}
