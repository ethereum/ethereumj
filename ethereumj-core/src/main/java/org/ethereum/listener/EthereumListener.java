package org.ethereum.listener;

import org.ethereum.core.*;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;
import org.ethereum.vm.program.InternalTransaction;

import java.util.List;
import java.util.Set;

/**
 * @author Roman Mandeleil
 * @since 27.07.2014
 */
public interface EthereumListener {

    enum PendingTransactionState {
        /**
         * Transaction may be dropped due to:
         * - Invalid transaction (invalid nonce, low gas price, insufficient account funds,
         *         invalid signature)
         * - Timeout (when pending transaction is not included to any block for
         *         last [transaction.outdated.threshold] blocks
         * This is the final state
         */
        DROPPED,

        /**
         * The same as PENDING when transaction is just arrived
         * Next state can be either PENDING or INCLUDED
         */
        NEW_PENDING,

        /**
         * State when transaction is not included to any blocks (on the main chain), and
         * was executed on the last best block. The repository state is reflected in the PendingState
         * Next state can be either INCLUDED, DROPPED (due to timeout)
         * or again PENDING when a new block (without this transaction) arrives
         */
        PENDING,

        /**
         * State when the transaction is included to a block.
         * This could be the final state, however next state could also be
         * PENDING: when a fork became the main chain but doesn't include this tx
         * INCLUDED: when a fork became the main chain and tx is included into another
         *           block from the new main chain
         */
        INCLUDED;

        public boolean isPending() {
            return this == NEW_PENDING || this == PENDING;
        }
    }

    void trace(String output);

    void onNodeDiscovered(Node node);

    void onHandShakePeer(Channel channel, HelloMessage helloMessage);

    void onEthStatusUpdated(Channel channel, StatusMessage status);

    void onRecvMessage(Channel channel, Message message);

    void onSendMessage(Channel channel, Message message);

    void onBlock(BlockSummary blockSummary, boolean isBestBlock);

    void onPeerDisconnect(String host, long port);

    /**
     * @deprecated use onPendingTransactionUpdate filtering state NEW_PENDING
     * Will be removed in the next release
     */
    void onPendingTransactionsReceived(List<Transaction> transactions);

    /**
     * PendingState changes on either new pending transaction or new best block receive
     * When a new transaction arrives it is executed on top of the current pending state
     * When a new best block arrives the PendingState is adjusted to the new Repository state
     * and all transactions which remain pending are executed on top of the new PendingState
     */
    void onPendingStateChanged(PendingState pendingState);

    /**
     * Is called when PendingTransaction arrives, executed or dropped and included to a block
     *
     * @param txReceipt Receipt of the tx execution on the current PendingState
     * @param state Current state of pending tx
     * @param block The block which the current pending state is based on (for PENDING tx state)
     *              or the block which tx was included to (for INCLUDED state)
     */
    void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block);

    void onSyncDone();

    void onNoConnections();

    void onVMTraceCreated(String transactionHash, String trace);

    void onTransactionExecuted(TransactionExecutionSummary summary);

    void onPeerAddedToSyncPool(Channel peer);
}
