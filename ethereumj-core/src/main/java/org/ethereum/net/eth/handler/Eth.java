/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.net.eth.handler;

import com.google.common.util.concurrent.ListenableFuture;
import org.ethereum.core.*;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.message.EthMessageCodes;
import org.ethereum.sync.PeerState;
import org.ethereum.sync.SyncStatistics;

import java.math.BigInteger;
import java.util.List;

/**
 * Describes interface required by Eth peer clients
 *
 * @see org.ethereum.net.server.Channel
 *
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
public interface Eth {

    /**
     * @return true if StatusMessage was processed, false otherwise
     */
    boolean hasStatusPassed();

    /**
     * @return true if Status has succeeded
     */
    boolean hasStatusSucceeded();

    /**
     * Executes cleanups required to be done
     * during shutdown, e.g. disconnect
     */
    void onShutdown();

    /**
     * Puts sync statistics to log output
     */
    String getSyncStats();

    BlockIdentifier getBestKnownBlock();

    BigInteger getTotalDifficulty();

    /**
     * @return true if syncState is DONE_HASH_RETRIEVING, false otherwise
     */
    boolean isHashRetrievingDone();

    /**
     * @return true if syncState is HEADER_RETRIEVING, false otherwise
     */
    boolean isHashRetrieving();

    /**
     * @return true if syncState is IDLE, false otherwise
     */
    boolean isIdle();

    /**
     * @return sync statistics
     */
    SyncStatistics getStats();

    /**
     * Disables pending transaction processing
     */
    void disableTransactions();

    /**
     * Enables pending transaction processing
     */
    void enableTransactions();

    /**
     * Sends transaction to the wire
     *
     * @param tx sending transaction
     */
    void sendTransaction(List<Transaction> tx);

    /**
     *  Send GET_BLOCK_HEADERS message to the peer
     */
    ListenableFuture<List<BlockHeader>> sendGetBlockHeaders(long blockNumber, int maxBlocksAsk, boolean reverse);

    ListenableFuture<List<BlockHeader>> sendGetBlockHeaders(byte[] blockHash, int maxBlocksAsk, int skip, boolean reverse);

    /**
     *  Send GET_BLOCK_BODIES message to the peer
     */
    ListenableFuture<List<Block>> sendGetBlockBodies(List<BlockHeaderWrapper> headers);

    /**
     * Sends new block to the wire
     */
    void sendNewBlock(Block newBlock);

    /**
     * Sends new block hashes message to the wire
     */
    void sendNewBlockHashes(Block block);

    /**
     * @return protocol version
     */
    EthVersion getVersion();

    /**
     * Fires inner logic related to long sync done or undone event
     *
     * @param done true notifies that long sync is finished,
     *             false notifies that it's enabled again
     */
    void onSyncDone(boolean done);

    /**
     * Sends {@link EthMessageCodes#STATUS} message
     */
    void sendStatus();

    /**
     * Drops connection with remote peer.
     * It should be called when peer don't behave
     */
    void dropConnection();

    /**
     * Force peer to fetch block bodies
     *
     * @param headers related headers
     */
    void fetchBodies(List<BlockHeaderWrapper> headers);

}
