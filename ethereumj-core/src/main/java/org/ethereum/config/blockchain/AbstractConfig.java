package org.ethereum.config.blockchain;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.config.Constants;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.db.BlockStore;
import org.ethereum.db.RepositoryTrack;
import org.ethereum.mine.EthashMiner;
import org.ethereum.mine.MinerIfc;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static org.ethereum.util.BIUtil.max;

/**
 * BlockchainForkConfig is also implemented by this class - its (mostly testing) purpose to represent
 * the specific config for all blocks on the chain (kinda constant config).
 *
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public abstract class AbstractConfig implements BlockchainConfig, BlockchainNetConfig {
    protected Constants constants;
    protected MinerIfc miner;

    public AbstractConfig() {
        this(new Constants());
    }

    public AbstractConfig(Constants constants) {
        this.constants = constants;
    }

    @Override
    public Constants getConstants() {
        return constants;
    }

    @Override
    public BlockchainConfig getConfigForBlock(long blockHeader) {
        return this;
    }

    @Override
    public Constants getCommonConstants() {
        return getConstants();
    }

    @Override
    public MinerIfc getMineAlgorithm(SystemProperties config) {
        if (miner == null) miner = new EthashMiner(config);
        return miner;
    }

    @Override
    public BigInteger calcDifficulty(BlockHeader curBlock, BlockHeader parent) {
        BigInteger pd = parent.getDifficultyBI();
        BigInteger quotient = pd.divide(getConstants().getDIFFICULTY_BOUND_DIVISOR());

        BigInteger sign = getCalcDifficultyMultiplier(curBlock, parent);

        BigInteger fromParent = pd.add(quotient.multiply(sign));
        BigInteger difficulty = max(getConstants().getMINIMUM_DIFFICULTY(), fromParent);

        int periodCount = (int) (curBlock.getNumber() / getConstants().getEXP_DIFFICULTY_PERIOD());

        if (periodCount > 1) {
            difficulty = max(getConstants().getMINIMUM_DIFFICULTY(), difficulty.add(BigInteger.ONE.shiftLeft(periodCount - 2)));
        }

        return difficulty;
    }

    protected abstract BigInteger getCalcDifficultyMultiplier(BlockHeader curBlock, BlockHeader parent);

    @Override
    public boolean acceptTransactionSignature(Transaction tx) {
        return true;
    }

    @Override
    public String validateTransactionChanges(BlockStore blockStore, Block curBlock, Transaction tx,
                                               RepositoryTrack repositoryTrack) {
        return null;
    }

    @Override
    public void hardForkTransfers(Block block, Repository repo) {}

    @Override
    public List<Pair<Long, byte[]>> blockHashConstraints() {
        return Collections.emptyList();
    }
}
