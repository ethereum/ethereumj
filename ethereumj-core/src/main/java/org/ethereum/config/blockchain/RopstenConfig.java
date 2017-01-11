package org.ethereum.config.blockchain;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.BlockchainConfig;
import org.spongycastle.util.encoders.Hex;

import java.util.Collections;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 21.11.2016.
 */
public class RopstenConfig extends Eip160HFConfig {

    // Check for 1 known block to exclude fake peers
    private static final long CHECK_BLOCK_NUMBER = 10;
    private static final byte[] CHECK_BLOCK_HASH = Hex.decode("b3074f936815a0425e674890d7db7b5e94f3a06dca5b22d291b55dcd02dde93e");

    public RopstenConfig(BlockchainConfig parent) {
        super(parent);
    }

    @Override
    public List<Pair<Long, byte[]>> blockHashConstraints() {
        return Collections.singletonList(Pair.of(CHECK_BLOCK_NUMBER, CHECK_BLOCK_HASH));
    }

    @Override
    public Integer getChainId() {
        return 3;
    }
}
