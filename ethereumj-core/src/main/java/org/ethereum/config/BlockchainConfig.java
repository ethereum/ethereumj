package org.ethereum.config;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.core.*;
import org.ethereum.db.BlockStore;
import org.ethereum.db.RepositoryTrack;
import org.ethereum.mine.MinerIfc;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.GasCost;
import org.ethereum.vm.OpCode;
import org.ethereum.vm.program.Program;

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

    /**
     * Hardcode the block hashes. I.e. if the block #1920000 should have the hash 0x1111
     * the this method should return [{1920000, 0x1111}]
     */
    List<Pair<Long, byte[]>> blockHashConstraints();

    /**
     * EVM operations costs
     */
    GasCost getGasCost();

    /**
     * Calculates available gas to be passed for callee
     * Since EIP150
     * @param op  Opcode
     * @param requestedGas amount of gas requested by the program
     * @param availableGas available gas
     * @throws Program.OutOfGasException If passed args doesn't conform to limitations
     */
    DataWord getCallGas(OpCode op, DataWord requestedGas, DataWord availableGas) throws Program.OutOfGasException;

    /**
     * Calculates available gas to be passed for contract constructor
     * Since EIP150
     */
    DataWord getCreateGas(DataWord availableGas);

    /**
     * EIP161: https://github.com/ethereum/EIPs/issues/161
     */
    boolean noEmptyAccounts();

    /**
     * EIP155: https://github.com/ethereum/EIPs/issues/155
     */
    Integer getChainId();
}
