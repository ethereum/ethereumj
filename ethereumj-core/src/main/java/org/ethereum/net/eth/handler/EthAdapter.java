package org.ethereum.net.eth.handler;

import org.ethereum.core.Transaction;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.sync.SyncStateName;
import org.ethereum.net.eth.sync.SyncStatistics;

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
    public void logSyncStats() {
    }

    @Override
    public void changeState(SyncStateName newState) {
    }

    @Override
    public boolean hasBlocksLack() {
        return false;
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
    public void setMaxHashesAsk(int maxHashesAsk) {
    }

    @Override
    public int getMaxHashesAsk() {
        return 0;
    }

    @Override
    public void setLastHashToAsk(byte[] lastHashToAsk) {
    }

    @Override
    public byte[] getLastHashToAsk() {
        return new byte[0];
    }

    @Override
    public byte[] getBestKnownHash() {
        return new byte[0];
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
    public void sendTransaction(Transaction tx) {
    }

    @Override
    public EthVersion getVersion() {
        return fromCode(UPPER);
    }
}
