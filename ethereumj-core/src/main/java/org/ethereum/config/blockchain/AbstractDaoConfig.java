package org.ethereum.config.blockchain;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.validator.BlockHeaderRule;
import org.ethereum.validator.BlockHeaderValidator;
import org.ethereum.validator.ExtraDataPresenceRule;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Stan Reshetnyk on 26.12.16.
 */
public abstract class AbstractDaoConfig extends HomesteadConfig {

    /**
     * Hardcoded values from live network
     */
    public static final long ETH_FORK_BLOCK_NUMBER = 1_920_000;

    protected long forkBlockNumber;
    protected byte[] DAO_EXTRA_DATA = Hex.decode("64616f2d686172642d666f726b");

    protected List<Pair<Long, BlockHeaderValidator>> VALIDATOR;

    protected void initDaoConfig(long forkBlockNumber, boolean supportFork) {
        this.forkBlockNumber = forkBlockNumber;
        BlockHeaderRule rule = new ExtraDataPresenceRule(DAO_EXTRA_DATA, supportFork);
        VALIDATOR = Arrays.asList(Pair.of(forkBlockNumber, new BlockHeaderValidator(Arrays.asList(rule))));
    }
}
