package org.ethereum.sync;

/**
 * Manages sync measurements
 *
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
public class SyncStatistics {
    private long updatedAt;
    private long blocksCount;
    private long headersCount;
    private int emptyResponsesCount;
    private int headerBunchesCount;

    public SyncStatistics() {
        reset();
    }

    public void reset() {
        updatedAt = System.currentTimeMillis();
        blocksCount = 0;
        headersCount = 0;
        emptyResponsesCount = 0;
        headerBunchesCount = 0;
    }

    public void addBlocks(long cnt) {
        blocksCount += cnt;
        fixCommon(cnt);
    }

    public void addHeaders(long cnt) {
        headerBunchesCount++;
        headersCount += cnt;
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

    public long getHeadersCount() {
        return headersCount;
    }

    public long millisSinceLastUpdate() {
        return System.currentTimeMillis() - updatedAt;
    }

    public int getEmptyResponsesCount() {
        return emptyResponsesCount;
    }

    public int getHeaderBunchesCount() {
        return headerBunchesCount;
    }
}
