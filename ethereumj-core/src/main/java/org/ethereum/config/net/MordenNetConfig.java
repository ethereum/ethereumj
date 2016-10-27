package org.ethereum.config.net;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.blockchain.Eip150HFConfig;
import org.ethereum.config.blockchain.MordenConfig;
import org.spongycastle.util.encoders.Hex;

import java.util.Collections;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class MordenNetConfig extends AbstractNetConfig {
    private static final long EIP150_HF_BLOCK = 1_783_000;
    private static final byte[] EIP150_HF_BLOCK_HASH =
            Hex.decode("f376243aeff1f256d970714c3de9fd78fa4e63cf63e32a51fe1169e375d98145");

    public MordenNetConfig() {
        add(0, new MordenConfig.Frontier());
        add(494_000, new MordenConfig.Homestead());
        add(EIP150_HF_BLOCK, new Eip150HFConfig(new MordenConfig.Homestead()) {
            @Override
            public List<Pair<Long, byte[]>> blockHashConstraints() {
                return Collections.singletonList(Pair.of(EIP150_HF_BLOCK, EIP150_HF_BLOCK_HASH));
            }
        });
    }
}
