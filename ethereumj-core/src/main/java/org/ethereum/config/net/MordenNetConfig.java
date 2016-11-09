package org.ethereum.config.net;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.blockchain.DaoHFConfig;
import org.ethereum.config.blockchain.Eip150HFConfig;
import org.ethereum.config.blockchain.Eip160HFConfig;
import org.ethereum.config.blockchain.MordenConfig;
import org.spongycastle.util.encoders.Hex;

import java.util.Collections;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class MordenNetConfig extends AbstractNetConfig {

    public MordenNetConfig() {
        add(0, new MordenConfig.Frontier());
        add(494_000, new MordenConfig.Homestead());
        add(1_783_000, new Eip150HFConfig(new MordenConfig.Homestead()));
        add(2_900_000, new Eip160HFConfig(new MordenConfig.Homestead())); // TODO adjust after announcement

    }
}
