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
package org.ethereum.mine;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.Constants;
import org.ethereum.core.BlockHeader;
import org.ethereum.crypto.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Maintains datasets of {@link EthashAlgo} for verification purposes.
 *
 * <p>
 *     Takes a burden of light dataset caching and provides convenient interface for keeping this cache up to date.
 *     Featured with full dataset lookup if such dataset is available (created for mining purposes),
 *     full dataset usage increases verification speed dramatically.
 *
 * <p>
 *     Entry point is {@link #ethashWorkFor(BlockHeader, byte[], boolean)}
 *
 * <p>
 *     Cache management interface: {@link #preCache(long)}, {@link CacheOrder}
 *
 * @author Mikhail Kalinin
 * @since 20.06.2018
 */
public class EthashValidationHelper {

    private static final int MAX_CACHED_EPOCHS = 2;

    private static final Logger logger = LoggerFactory.getLogger("ethash");

    public enum CacheOrder {
        direct,     /** cache is updated to fit main import process, toward big numbers */
        reverse     /** for maintaining reverse header validation, cache is updated to fit block validation with decreasing numbers */
    }

    List<Cache> caches = new CopyOnWriteArrayList<>();
    EthashAlgo ethashAlgo = new EthashAlgo(Ethash.ethashParams);
    long lastCachedEpoch = -1;

    private CacheStrategy cacheStrategy;

    private static ExecutorService executor;

    public EthashValidationHelper(CacheOrder cacheOrder) {
        this.cacheStrategy = createCacheStrategy(cacheOrder);

        if (executor == null)
            executor = Executors.newSingleThreadExecutor((r) -> {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setName("ethash-validation-helper");
                t.setDaemon(true);
                return t;
            });
    }

    /**
     * Calculates ethash results for particular block and nonce.
     *
     * @param cachedOnly flag that defined behavior of method when dataset has not been cached:
     *                   if set to true  - returns null immediately
     *                   if set to false - generates dataset for block epoch and then runs calculations on it
     */
    public Pair<byte[], byte[]> ethashWorkFor(BlockHeader header, byte[] nonce, boolean cachedOnly) throws Exception {

        long fullSize = ethashAlgo.getParams().getFullSize(header.getNumber());
        byte[] hashWithoutNonce = HashUtil.sha3(header.getEncodedWithoutNonce());

        // lookup with full dataset if it's available
        Ethash cachedInstance = Ethash.cachedInstance;
        if (cachedInstance != null &&
                cachedInstance.epoch == epoch(header.getNumber()) &&
                cachedInstance.getFullData() != null) {
            return ethashAlgo.hashimotoFull(fullSize, cachedInstance.getFullData(), hashWithoutNonce, nonce);
        }

        Cache cache = getCachedFor(header.getNumber());
        if (cache != null) {
            return ethashAlgo.hashimotoLight(fullSize, cache.getDataset(), hashWithoutNonce, nonce);
        } else if (!cachedOnly) {
            cache = new Cache(header.getNumber());
            return ethashAlgo.hashimotoLight(fullSize, cache.getDataset(), hashWithoutNonce, nonce);
        } else {
            return null;
        }
    }

    Cache getCachedFor(long blockNumber) {
        for (Cache cache : caches) {
            if (cache.isFor(blockNumber))
                return cache;
        }

        return null;
    }

    public void preCache(long blockNumber) {
        cacheStrategy.cache(blockNumber);
    }

    long epochLength() {
        return ethashAlgo.getParams().getEPOCH_LENGTH();
    }

    long epoch(long blockNumber) {
        return blockNumber / ethashAlgo.getParams().getEPOCH_LENGTH();
    }

    class Cache {
        CompletableFuture<int[]> dataset;
        long epoch;

        Cache(long blockNumber) {
            byte[] seed = ethashAlgo.getSeedHash(blockNumber);
            long size = ethashAlgo.getParams().getCacheSize(blockNumber);

            this.dataset = new CompletableFuture<>();
            executor.submit(() -> {
                int[] cache = ethashAlgo.makeCache(size, seed);
                this.dataset.complete(cache);
            });

            this.epoch = epoch(blockNumber);
        }

        boolean isFor(long blockNumber) {
            return epoch == epoch(blockNumber);
        }

        int[] getDataset() throws Exception {
            return dataset.get();
        }
    }

    private CacheStrategy createCacheStrategy(CacheOrder order) {
        switch (order) {
            case direct:    return new DirectCache();
            case reverse:   return new ReverseCache();
            default: throw new IllegalArgumentException("Unsupported cache strategy " + order.name());
        }
    }

    interface CacheStrategy {
        void cache(long blockNumber);
    }

    class ReverseCache implements CacheStrategy {

        @Override
        public void cache(long blockNumber) {

            // reset cache if it's outdated
            if (epoch(blockNumber) < lastCachedEpoch || lastCachedEpoch < 0) {
                reset(blockNumber);
                return;
            }

            // lock-free check
            if (blockNumber < epochLength() || epoch(blockNumber) - 1 >= lastCachedEpoch ||
                    blockNumber % epochLength() >= epochLength() / 2)
                return;

            synchronized (EthashValidationHelper.this) {
                if (blockNumber < epochLength() || epoch(blockNumber) - 1 >= lastCachedEpoch ||
                        blockNumber % epochLength() >= epochLength() / 2)
                    return;

                // cache previous epoch
                caches.add(new Cache(blockNumber - epochLength()));
                lastCachedEpoch -= 1;

                // remove redundant caches
                while (caches.size() > MAX_CACHED_EPOCHS)
                    caches.remove(0);

                logger.info("Kept caches: cnt: {} epochs: {}...{}",
                        caches.size(), caches.get(0).epoch, caches.get(caches.size() - 1).epoch);
            }
        }

        private void reset(long blockNumber) {
            synchronized (EthashValidationHelper.this) {
                caches.clear();
                caches.add(new Cache(blockNumber));

                if (blockNumber % epochLength() >= epochLength() / 2) {
                    caches.add(0, new Cache(blockNumber + epochLength()));
                } else if (blockNumber >= epochLength()) {
                    caches.add(new Cache(blockNumber - epochLength()));
                }

                lastCachedEpoch = caches.get(caches.size() - 1).epoch;

                logger.info("Kept caches: cnt: {} epochs: {}...{}",
                        caches.size(), caches.get(0).epoch, caches.get(caches.size() - 1).epoch);
            }
        }
    }


    class DirectCache implements CacheStrategy {

        @Override
        public void cache(long blockNumber) {

            // reset cache if it's outdated
            if (epoch(blockNumber) > lastCachedEpoch || lastCachedEpoch < 0) {
                reset(blockNumber);
                return;
            }

            // lock-free check
            if (epoch(blockNumber) + 1 <= lastCachedEpoch ||
                    blockNumber % epochLength() <= Constants.getLONGEST_CHAIN())
                return;

            synchronized (EthashValidationHelper.this) {
                if (epoch(blockNumber) + 1 <= lastCachedEpoch ||
                        blockNumber % epochLength() <= Constants.getLONGEST_CHAIN())
                    return;

                // cache next epoch
                caches.add(new Cache(blockNumber + epochLength()));
                lastCachedEpoch += 1;

                // remove redundant caches
                while (caches.size() > MAX_CACHED_EPOCHS)
                    caches.remove(0);

                logger.info("Kept caches: cnt: {} epochs: {}...{}",
                        caches.size(), caches.get(0).epoch, caches.get(caches.size() - 1).epoch);
            }
        }

        private void reset(long blockNumber) {
            synchronized (EthashValidationHelper.this) {
                caches.clear();
                caches.add(new Cache(blockNumber));

                if (blockNumber % epochLength() > Constants.getLONGEST_CHAIN()) {
                    caches.add(new Cache(blockNumber + epochLength()));
                } else if (blockNumber >= epochLength()) {
                    caches.add(0, new Cache(blockNumber - epochLength()));
                }

                lastCachedEpoch = caches.get(caches.size() - 1).epoch;

                logger.info("Kept caches: cnt: {} epochs: {}...{}",
                        caches.size(), caches.get(0).epoch, caches.get(caches.size() - 1).epoch);
            }
        }
    }
}
