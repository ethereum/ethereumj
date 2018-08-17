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
package org.ethereum.sharding.processing.db;

import org.ethereum.datasource.DataSourceArray;
import org.ethereum.datasource.ObjectDataSource;
import org.ethereum.datasource.Serializer;
import org.ethereum.datasource.Source;
import org.ethereum.sharding.domain.Beacon;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.max;
import static org.ethereum.util.FastByteComparisons.equal;

/**
 * @author Mikhail Kalinin
 * @since 14.08.2018
 */
public class IndexedBeaconStore implements BeaconStore {

    Source<byte[], byte[]> blockSrc;
    Source<byte[], byte[]> indexSrc;

    DataSourceArray<List<ChainItem>> index;
    ObjectDataSource<Beacon> blocks;

    public IndexedBeaconStore(Source<byte[], byte[]> blockSrc, Source<byte[], byte[]> indexSrc) {
        this.blockSrc = blockSrc;
        this.indexSrc = indexSrc;
        this.blocks = new ObjectDataSource<>(blockSrc, Beacon.Serializer, BLOCKS_IN_MEM);
        this.index = new DataSourceArray<>(new ObjectDataSource<>(indexSrc, ChainItem.ListSerializer, BLOCKS_IN_MEM * 2));
    }

    @Override
    public synchronized Beacon getCanonicalHead() {
        ChainItem head = getCanonicalHeadItem();
        if (head != null) {
            return blocks.get(head.getHash());
        }

        return null;
    }

    @Override
    public synchronized BigInteger getCanonicalHeadScore() {
        ChainItem head = getCanonicalHeadItem();
        if (head != null) {
            return head.getChainScore();
        }

        return BigInteger.ZERO;
    }

    @Override
    public synchronized Beacon getByHash(byte[] hash) {
        return blocks.get(hash);
    }

    @Override
    public synchronized boolean exist(byte[] hash) {
        return blocks.get(hash) != null;
    }

    @Override
    public synchronized BigInteger getChainScore(byte[] hash) {
        ChainItem item = getChainItemByHash(hash);
        if (item == null)
            return BigInteger.ZERO;

        return item.getChainScore();
    }

    @Override
    public synchronized long getMaxNumber() {
        return ((long) index.size()) - 1L;
    }

    @Override
    public synchronized void save(Beacon block, BigInteger chainScore, boolean canonical) {
        ChainItem item = new ChainItem(chainScore, block.getHash(), block.getParentHash(), canonical);
        putIndexItem(block.getNumber(), item);
        blocks.put(block.getHash(), block);
    }

    @Override
    public synchronized void reorgTo(Beacon fork) {
        if (!exist(fork.getHash()))
            return;

        byte[] canonicalHead = getCanonicalHead().getHash();
        long canonicalHeadNum = getCanonicalHead().getNumber();
        byte[] forkHead = fork.getHash();
        long forkHeadNum = fork.getNumber();

        // scan for fork head
        for (long num = fork.getNumber() + 1; num <= getMaxNumber(); ++num) {
            List<ChainItem> generation = getGenerationByNumber(num);
            if (generation == null) break;

            boolean keepScanning = false;
            for (ChainItem item : generation) {
                if (equal(forkHead, item.getParentHash())) {
                    forkHead = item.getHash();
                    forkHeadNum = num;
                    keepScanning = true;
                }
            }

            if (!keepScanning) break;
        }

        // scan in reverse order unless common ancestor is found
        long num = max(canonicalHeadNum, forkHeadNum);
        for (; !equal(canonicalHead, forkHead) && num >= 0; --num) {
            List<ChainItem> generation = getGenerationByNumber(num);
            if (generation == null) break;

            // change canonical chain
            for (ChainItem item : generation) {
                if (equal(item.getHash(), canonicalHead)) {
                    item.canonical = false;
                    canonicalHead = item.getParentHash();
                }

                if (equal(item.getHash(), forkHead)) {
                    item.canonical = true;
                    forkHead = item.getParentHash();
                }
            }

            // update index
            index.set((int) num, generation);
        }
    }

    private void putIndexItem(long number, ChainItem newItem) {
        List<ChainItem> generation = getGenerationByNumber(number);
        putInGeneration(newItem, generation == null ? generation = new ArrayList<>() : generation);
        index.set((int) number, generation);
    }

    private void putInGeneration(ChainItem newItem, List<ChainItem> generation) {
        for (int i = 0; i < generation.size(); i++) {
            ChainItem item = generation.get(i);
            if (equal(item.getHash(), newItem.getHash()))
                generation.set(i, newItem);
        }

        generation.add(newItem);
    }

    private ChainItem getChainItemByHash(byte[] hash) {
        Beacon block = blocks.get(hash);
        if (block == null)
            return null;

        List<ChainItem> generation = getGenerationByNumber(block.getNumber());
        if (generation == null)
            return null;

        for (ChainItem item : generation) {
            if (equal(item.getHash(), hash))
                return item;
        }

        return null;
    }

    private ChainItem getCanonicalHeadItem() {
        for (long number = getMaxNumber(); number >= 0; --number) {
            ChainItem item = getCanonicalItem(number);
            if (item != null)
                return item;
        }

        return null;
    }

    private ChainItem getCanonicalItem(long number) {
        if (index.size() - 1 < number)
            return null;

        List<ChainItem> generation = getGenerationByNumber(number);
        if (generation == null)
            return null;

        for (ChainItem item : generation) {
            if (item.isCanonical())
                return item;
        }

        return null;
    }

    private List<ChainItem> getGenerationByNumber(long number) {
        if (number >= index.size())
            return null;

        return index.get((int) number);
    }

    static class ChainItem {
        private byte[] hash;
        private byte[] parentHash;
        private BigInteger score;
        private boolean canonical;

        public ChainItem(BigInteger score, byte[] hash, byte[] parentHash, boolean canonical) {
            this.hash = hash;
            this.parentHash = parentHash;
            this.score = score;
            this.canonical = canonical;
        }

        public ChainItem(byte[] encoded) {
            RLPList list = RLP.unwrapList(encoded);

            this.hash = list.get(0).getRLPData();
            this.parentHash = list.get(1).getRLPData();
            this.score = ByteUtil.bytesToBigInteger(list.get(2).getRLPData());
            this.canonical = ByteUtil.byteArrayToInt(list.get(3).getRLPData()) > 0;
        }

        public BigInteger getChainScore() {
            return score;
        }

        public byte[] getHash() {
            return hash;
        }

        public boolean isCanonical() {
            return canonical;
        }

        public byte[] getParentHash() {
            return parentHash;
        }

        public byte[] getEncoded() {
            return RLP.wrapList(hash, parentHash, ByteUtil.bigIntegerToBytes(score),
                    ByteUtil.intToBytes(canonical ? 1 : 0));
        }

        static final Serializer<List<ChainItem>, byte[]> ListSerializer = new org.ethereum.datasource.Serializer<List<ChainItem>, byte[]>() {

            @Override
            public byte[] serialize(List<ChainItem> list) {
                if (list == null) return null;
                byte[][] elements = list.stream().map(ChainItem::getEncoded).toArray(byte[][]::new);
                return RLP.wrapList(elements);
            }

            @Override
            public List<ChainItem> deserialize(byte[] bytes) {
                if (bytes == null) return null;
                return RLP.unwrapList(bytes).stream()
                        .map(e -> new ChainItem(e.getRLPData()))
                        .collect(Collectors.toList());
            }
        };
    }
}
