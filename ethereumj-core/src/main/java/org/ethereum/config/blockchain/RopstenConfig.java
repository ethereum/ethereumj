package org.ethereum.config.blockchain;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.BlockchainConfig;
import org.ethereum.validator.BlockCustomHashRule;
import org.ethereum.validator.BlockHeaderRule;
import org.ethereum.validator.BlockHeaderValidator;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 21.11.2016.
 */
public class RopstenConfig extends Eip160HFConfig {

    // Check for 1 known block to exclude fake peers
    private static final long CHECK_BLOCK_NUMBER = 10;
    private static final byte[] CHECK_BLOCK_HASH = Hex.decode("b3074f936815a0425e674890d7db7b5e94f3a06dca5b22d291b55dcd02dde93e");

    protected final List<Pair<Long, BlockHeaderValidator>> validator;

    public RopstenConfig(BlockchainConfig parent) {
        super(parent);
        BlockHeaderRule rule = new BlockCustomHashRule(CHECK_BLOCK_HASH);
        validator = Arrays.asList(Pair.of(CHECK_BLOCK_NUMBER, new BlockHeaderValidator(Arrays.asList(rule))));
    }

    @Override
    public List<Pair<Long, BlockHeaderValidator>> headerValidators() {
        return validator;
    }

    @Override
    public Integer getChainId() {
        return 3;
    }
}
