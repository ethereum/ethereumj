package org.ethereum.config.net;

import org.ethereum.config.blockchain.MordenConfig;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class MordenNetConfig extends AbstractNetConfig {
    public MordenNetConfig() {
        add(0, new MordenConfig.Frontier());
        add(494_000, new MordenConfig.Homestead());
    }
}
