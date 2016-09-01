package org.ethereum.config;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.db.BlockStore;
import org.ethereum.db.RepositoryTrack;
import org.ethereum.mine.MinerIfc;

import java.math.BigInteger;
import java.util.List;

/**
 * Describes constants and algorithms used for a specific blockchain at specific stage
 *
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public interface BlockchainConfig {

    /**
     * Get blockchain constants
     */
    Constants getConstants();

    /**
     * Returns the mining algorithm
     */
    MinerIfc getMineAlgorithm(SystemProperties config);

    /**
     * Calculates the difficulty for the block depending on the parent
     */
    BigInteger calcDifficulty(BlockHeader curBlock, BlockHeader parent);

    /**
     * Calculates transaction gas fee
     */
    long getTransactionCost(Transaction tx);

    /**
     * Validates Tx signature (introduced in Homestead)
     */
    boolean acceptTransactionSignature(Transaction tx);

    /**
     * Validates transaction by the changes made by it in the repository
     * @param blockStore
     * @param curBlock The block being imported
     * @param repositoryTrack The repository track changed by transaction
     * @return null if all is fine or String validation error
     */
    String validateTransactionChanges(BlockStore blockStore, Block curBlock, Transaction tx,
                                      RepositoryTrack repositoryTrack);


    /**
     * Prior to block processing performs some repository manipulations according
     * to HardFork rules.
     * This method is normally executes the logic on a specific hardfork block only
     * for other blocks it just does nothing
     */
    void hardForkTransfers(Block block, Repository repo);

    List<Pair<Long, byte[]>> blockHashConstraints();
}
