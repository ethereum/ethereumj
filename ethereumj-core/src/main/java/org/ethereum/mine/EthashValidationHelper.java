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
import org.ethereum.core.BlockHeader;
import org.ethereum.crypto.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Maintain datasets of {@link EthashAlgo} for verification purposes.
 *
 * <p>
 *     Takes a burden of light dataset caching and keeping this cache up to date.
 *     Featured with full dataset lookup if such dataset is available (created for mining purposes),
 *     full dataset usage increases verification speed dramatically.
 *
 * <p>
 *     Entry point is {@link #ethashWorkFor(BlockHeader, byte[])}
 *
 * @author Mikhail Kalinin
 * @since 20.06.2018
 */
public class EthashValidationHelper {

    private static final int MAX_KEPT_EPOCHS = 2;

    private static final Logger logger = LoggerFactory.getLogger("ethash");

    LinkedList<Cache> caches = new LinkedList<>();
    EthashAlgo ethashAlgo = new EthashAlgo(Ethash.ethashParams);

    private ExecutorService executor;

    public EthashValidationHelper() {
        this.executor = Executors.newSingleThreadExecutor((r) -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setName("ethash-validation-helper");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Calculates ethash results for particular block and nonce.
     * It also maintains dataset cache.
     */
    public Pair<byte[], byte[]> ethashWorkFor(BlockHeader header, byte[] nonce) throws Exception {

        long fullSize = ethashAlgo.getParams().getFullSize(header.getNumber());
        byte[] hashWithoutNonce = HashUtil.sha3(header.getEncodedWithoutNonce());

        // lookup with full dataset if it's available
        Ethash cachedInstance = Ethash.cachedInstance;
        if (cachedInstance != null &&
                cachedInstance.epoch == epoch(header.getNumber()) &&
                cachedInstance.getFullData() != null) {
            return ethashAlgo.hashimotoFull(fullSize, cachedInstance.getFullData(), hashWithoutNonce, nonce);
        }

        Cache cache = getForBlock(header.getNumber());
        preCalculateCache(header.getNumber());

        return ethashAlgo.hashimotoLight(fullSize, cache.getDataset(), hashWithoutNonce, nonce);
    }

    Cache getForBlock(long blockNumber) {
        for (Cache cache : caches) {
            if (cache.isFor(blockNumber))
                return cache;
        }

        Cache cache = new Cache(blockNumber);
        addCache(cache);

        return cache;
    }

    void addCache(Cache cache) {
        if (caches.isEmpty()) {
            caches.push(cache);
            logger.info("Kept caches: cnt: {} epochs: {}...{}", caches.size(), caches.getFirst().epoch, caches.getLast().epoch);
            return;
        }

        // try to prepend
        if (caches.getFirst().epoch == cache.epoch + 1) {
            caches.addFirst(cache);
            if (caches.size() > MAX_KEPT_EPOCHS) {
                caches.pollLast();
            }
            logger.info("Kept caches: cnt: {} epochs: {}...{}", caches.size(), caches.getFirst().epoch, caches.getLast().epoch);
            return;
        }

        // try to append
        if (caches.getLast().epoch == cache.epoch - 1) {
            caches.addLast(cache);
            if (caches.size() > MAX_KEPT_EPOCHS) {
                caches.pollFirst();
            }
            logger.info("Kept caches: cnt: {} epochs: {}...{}", caches.size(), caches.getFirst().epoch, caches.getLast().epoch);
            return;
        }

        // otherwise clear and add
        caches.clear();
        caches.add(cache);

        logger.info("Kept caches: cnt: {} epochs: {}...{}", caches.size(), caches.getFirst().epoch, caches.getLast().epoch);
    }

    void preCalculateCache(long blockNumber) {
        if (caches.isEmpty())
            return;
        
        long base = blockNumber - caches.getLast().epoch * epochLength();
        if (base > epochLength() / 2 && base < epochLength()) {
            addCache(new Cache(blockNumber + epochLength()));
        }
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
}
