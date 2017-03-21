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
package org.ethereum.datasource;

import org.ethereum.crypto.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Special optimization when the majority of get requests to the slower underlying source
 * are targeted to missing entries. The BloomFilter handles most of these requests.
 *
 * Created by Anton Nashatyrev on 16.01.2017.
 */
public class BloomedSource extends AbstractChainedSource<byte[], byte[], byte[], byte[]> {
    private final static Logger logger = LoggerFactory.getLogger("db");

    private byte[] filterKey = HashUtil.sha3("filterKey".getBytes());

    QuotientFilter filter;
    int hits = 0;
    int misses = 0;
    int falseMisses = 0;
    boolean dirty = false;
    int maxBloomSize;

    public BloomedSource(Source<byte[], byte[]> source, int maxBloomSize) {
        super(source);
        this.maxBloomSize = maxBloomSize;
        byte[] filterBytes = source.get(filterKey);
        if (filterBytes != null) {
            if (filterBytes.length > 0) {
                filter = QuotientFilter.deserialize(filterBytes);
            } else {
                // filter size exceeded limit and is disabled forever
                filter = null;
            }
        } else {
            if (maxBloomSize > 0) {
                filter = QuotientFilter.create(50_000_000, 100_000);
            } else {
                // we can't re-enable filter later
                getSource().put(filterKey, new byte[0]);
            }
        }
//
//        new Thread() {
//            @Override
//            public void run() {
//                while(true) {
//                    synchronized (BloomedSource.this) {
//                        logger.debug("BloomedSource: hits: " + hits + ", misses: " + misses + ", false: " + falseMisses);
//                        hits = misses = falseMisses = 0;
//                    }
//
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {}
//                }
//            }
//        }.start();
    }

    public void startBlooming(QuotientFilter filter) {
        this.filter = filter;
    }

    public void stopBlooming() {
        filter = null;
    }

    @Override
    public void put(byte[] key, byte[] val) {
        if (filter != null) {
            filter.insert(key);
            dirty = true;
            if (filter.getAllocatedBytes() > maxBloomSize) {
                logger.info("Bloom filter became too large (" + filter.getAllocatedBytes() + " exceeds max threshold " + maxBloomSize + ") and is now disabled forever.");
                getSource().put(filterKey, new byte[0]);
                filter = null;
                dirty = false;
            }
        }
        getSource().put(key, val);
    }

    @Override
    public byte[] get(byte[] key) {
        if (filter == null) return getSource().get(key);

        if (!filter.maybeContains(key)) {
            hits++;
            return null;
        } else {
            byte[] ret = getSource().get(key);
            if (ret == null) falseMisses++;
            else misses++;
            return ret;
        }
    }

    @Override
    public void delete(byte[] key) {
        if (filter != null) filter.remove(key);
        getSource().delete(key);
    }

    @Override
    protected boolean flushImpl() {
        if (filter != null && dirty) {
            getSource().put(filterKey, filter.serialize());
            dirty = false;
            return true;
        } else {
            return false;
        }
    }
}
