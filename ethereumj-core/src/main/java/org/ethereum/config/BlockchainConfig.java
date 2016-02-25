package org.ethereum.config;

import org.ethereum.core.BlockHeader;
import org.ethereum.core.Transaction;
import org.ethereum.mine.MinerIfc;

import java.math.BigInteger;

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
    MinerIfc getMineAlgorithm();

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
}
