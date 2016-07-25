package org.ethereum.net.eth.handler;

import org.ethereum.core.*;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.sync.SyncState;
import org.ethereum.sync.SyncStatistics;

import java.math.BigInteger;
import java.util.List;

import static org.ethereum.net.eth.EthVersion.*;

/**
 * It's quite annoying to always check {@code if (eth != null)} before accessing it. <br>
 *
 * This adapter helps to avoid such checks. It provides meaningful answers to Eth client
 * assuming that Eth hasn't been initialized yet. <br>
 *
 * Check {@link org.ethereum.net.server.Channel} for example.
 *
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
public class EthAdapter implements Eth {

    private final SyncStatistics syncStats = new SyncStatistics();

    @Override
    public boolean hasStatusPassed() {
        return false;
    }

    @Override
    public boolean hasStatusSucceeded() {
        return false;
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public String getSyncStats() {
        return "";
    }

    @Override
    public boolean isHashRetrievingDone() {
        return false;
    }

    @Override
    public boolean isHashRetrieving() {
        return false;
    }

    @Override
    public boolean isIdle() {
        return true;
    }

    @Override
    public SyncStatistics getStats() {
        return syncStats;
    }

    @Override
    public void disableTransactions() {
    }

    @Override
    public void enableTransactions() {
    }

    @Override
    public void sendTransaction(List<Transaction> tx) {
    }

    @Override
    public void sendGetBlockHeaders(long blockNumber, int maxBlocksAsk, boolean reverse) {
    }

    @Override
    public void sendGetBlockBodies(List<BlockHeaderWrapper> headers) {
    }

    @Override
    public void sendNewBlock(Block newBlock) {
    }

    @Override
    public void sendNewBlockHashes(Block block) {

    }

    @Override
    public EthVersion getVersion() {
        return fromCode(UPPER);
    }

    @Override
    public void onSyncDone(boolean done) {
    }

    @Override
    public void sendStatus() {
    }

    @Override
    public void dropConnection() {
    }

    @Override
    public void fetchBodies(List<BlockHeaderWrapper> headers) {
    }

    @Override
    public BlockIdentifier getBestKnownBlock() {
        return null;
    }

    @Override
    public BigInteger getTotalDifficulty() {
        return BigInteger.ZERO;
    }
}
