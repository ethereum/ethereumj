package org.ethereum.config.fork;

import org.ethereum.config.blockchain.MordenConfig;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class MordenForkConfig extends AbstractForkConfig {
    public MordenForkConfig() {
        add(0, new MordenConfig.Frontier());
        add(1_000_000, new MordenConfig.Homestead());
    }
}
