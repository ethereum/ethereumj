package org.ethereum.core;

import java.util.concurrent.Future;

/**
 * Keeps the track of Transaction execution steps
 * Initially the result is null when the transaction is not yet
 * propagated to the PendingState
 * Then it can subsequently produce several pending results
 * when transaction is not yet included to a block but executed
 * on top of imported blocks
 * Finally it should contain the result when the transaction is
 * included to a mined block. Note that there could be several
 * final states when the transaction is included into several
 * blocks on different chain forks
 * Also the transaction might not be included into any block
 * under certain circumstances
 *
 * Created by Anton Nashatyrev on 14.04.2016.
 */
public interface TransactionResult {

    Transaction getTransaction();

    /**
     * Returns the latest available execution result.
     * May return null if transaction is not yet handled by the PendingState
     */
    TransactionInfo getLatestResult();

    /**
     * Returns future pending result(s) and mined block result(s)
     */
    Future<TransactionInfo> getNextResult();

    /**
     * Returns future mined block result(s)
     */
    Future<TransactionInfo> getNextBlockResult();

    /**
     * Returns the future result when the transaction is confirmed by confirmationBlocksCount
     * blocks.
     * I.e. when there at least confirmationBlocksCount blocks mined on the main chain
     * after the block which contains this transaction
     */
    Future<TransactionInfo> getConfirmedResult(int confirmationBlocksCount);
}
