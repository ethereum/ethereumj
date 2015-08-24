package org.ethereum.net.eth.sync;

/**
 * Manages sync measurements
 *
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
public class SyncStatistics {
    private long updatedAt;
    private long blocksCount;
    private long hashesCount;
    private int emptyResponsesCount;

    public SyncStatistics() {
        reset();
    }

    public void reset() {
        updatedAt = System.currentTimeMillis();
        blocksCount = 0;
        hashesCount = 0;
        emptyResponsesCount = 0;
    }

    public void addBlocks(long cnt) {
        blocksCount += cnt;
        fixCommon(cnt);
    }

    public void addHashes(long cnt) {
        hashesCount += cnt;
        fixCommon(cnt);
    }

    private void fixCommon(long cnt) {
        if (cnt == 0) {
            emptyResponsesCount += 1;
        }
        updatedAt = System.currentTimeMillis();
    }

    public long getBlocksCount() {
        return blocksCount;
    }

    public long getHashesCount() {
        return hashesCount;
    }

    public long millisSinceLastUpdate() {
        return System.currentTimeMillis() - updatedAt;
    }

    public int getEmptyResponsesCount() {
        return emptyResponsesCount;
    }
}
