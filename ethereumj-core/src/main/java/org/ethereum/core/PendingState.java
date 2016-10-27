package org.ethereum.core;

import java.util.List;

/**
 * @author Mikhail Kalinin
 * @since 28.09.2015
 */
public interface PendingState extends org.ethereum.facade.PendingState {

    /**
     * Adds transactions received from the net to the list of wire transactions <br>
     * Triggers an update of pending state
     *
     * @param transactions txs received from the net
     * @return sublist of transactions with NEW_PENDING status
     */
    List<Transaction> addPendingTransactions(List<Transaction> transactions);

    /**
     * Adds transaction to the list of pending state txs  <br>
     * For the moment this list is populated with txs sent by our peer only <br>
     * Triggers an update of pending state
     *
     * @param tx transaction
     */
    void addPendingTransaction(Transaction tx);

    /**
     * It should be called on each block imported as <b>BEST</b> <br>
     * Does several things:
     * <ul>
     *     <li>removes block's txs from pending state and wire lists</li>
     *     <li>removes outdated wire txs</li>
     *     <li>updates pending state</li>
     * </ul>
     *
     * @param block block imported into blockchain as a <b>BEST</b> one
     */
    void processBest(Block block, List<TransactionReceipt> receipts);
}
