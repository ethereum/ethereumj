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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Special optimization when the majority of get requests to the slower underlying source
 * are targeted to missing entries. The BloomFilter handles most of these requests.
 *
 * WARN: can be used only when the backing source is initially empty or
 * if the backing source contains any values the BloomFilter instance passed should
 * be filled according to these values
 *
 * Created by Anton Nashatyrev on 16.01.2017.
 */
public class BloomedSource<Value> extends AbstractChainedSource<byte[], Value, byte[], Value> {
    private final static Logger logger = LoggerFactory.getLogger("sync");

    BloomFilter bloom;
    int hits = 0;
    int misses = 0;
    int falseMisses = 0;

    public BloomedSource(Source<byte[], Value> source) {
        super(source);
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

    public void startBlooming(BloomFilter filter) {
        bloom = filter;
    }

    public void stopBlooming() {
        bloom = null;
    }

    @Override
    public synchronized void put(byte[] key, Value val) {
        if (bloom != null) {
            bloom.add(key);
        }
        getSource().put(key, val);
    }

    @Override
    public synchronized Value get(byte[] key) {
        if (bloom == null) return getSource().get(key);

        if (!bloom.contains(key)) {
            hits++;
            return null;
        } else {
            Value ret = getSource().get(key);
            if (ret == null) falseMisses++; else misses++;
            return ret;
        }
    }

    @Override
    public void delete(byte[] key) {
        getSource().delete(key);
    }

    @Override
    protected boolean flushImpl() {
        return false;
    }
}
