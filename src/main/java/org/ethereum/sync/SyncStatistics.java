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
    private int headerBunchesCount;

    public SyncStatistics() {
        reset();
    }

    public void reset() {
        updatedAt = System.currentTimeMillis();
        blocksCount = 0;
        headersCount = 0;
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
        updatedAt = System.currentTimeMillis();
    }

    public long getBlocksCount() {
        return blocksCount;
    }

    public long getHeadersCount() {
        return headersCount;
    }

    public long secondsSinceLastUpdate() {
        return (System.currentTimeMillis() - updatedAt) / 1000;
    }

    public int getHeaderBunchesCount() {
        return headerBunchesCount;
    }
}
