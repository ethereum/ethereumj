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

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

import static java.lang.System.arraycopy;
import static java.math.BigInteger.valueOf;
import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.crypto.HashUtil.sha512;
import static org.ethereum.util.ByteUtil.*;
import static org.spongycastle.util.Arrays.reverse;

/**
 * The Ethash algorithm described in https://github.com/ethereum/wiki/wiki/Ethash
 *
 * This is the non-optimized Ethash implementation. It is left here for reference only
 * since the non-optimized version is slightly better for understanding the Ethash algorithm
 *
 * Created by Anton Nashatyrev on 27.11.2015.
 * @deprecated Use a faster version {@link EthashAlgo}, this class is for reference only
 */
public class EthashAlgoSlow {
    EthashParams params;

    public EthashAlgoSlow() {
        this(new EthashParams());
    }

    public EthashAlgoSlow(EthashParams params) {
        this.params = params;
    }

    public EthashParams getParams() {
        return params;
    }

    // Little-Endian !
    static long getWord(byte[] arr, int wordOff) {
        return ByteBuffer.wrap(arr, wordOff * 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFFFFFFL;
    }
    static void setWord(byte[] arr, int wordOff, long val) {
        ByteBuffer bb = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int) val);
        bb.rewind();
        bb.get(arr, wordOff * 4, 4);
    }

    public byte[][] makeCache(long cacheSize, byte[] seed) {
        int n = (int) (cacheSize / params.getHASH_BYTES());
        byte[][] o = new byte[n][];
        o[0] = sha512(seed);
        for (int i = 1; i < n; i++) {
            o[i] = sha512(o[i - 1]);
        }

        for (int cacheRound = 0; cacheRound < params.getCACHE_ROUNDS(); cacheRound++) {
            for (int i = 0; i < n; i++) {
                int v = (int) (getWord(o[i], 0) % n);
                o[i] = sha512(xor(o[(i - 1 + n) % n], o[v]));
            }
        }
        return o;
    }

    private static final long FNV_PRIME = 0x01000193;
    long fnv(long v1, long v2) {
        return ((v1 * FNV_PRIME) ^ v2) % (1L << 32);
    }

    byte[] fnv(byte[] b1, byte[] b2) {
        if (b1.length != b2.length || b1.length % 4 != 0) throw new RuntimeException();

        byte[] ret = new byte[b1.length];
        for (int i = 0; i < b1.length / 4; i++) {
            long i1 = getWord(b1, i);
            long i2 = getWord(b2, i);
            setWord(ret, i, fnv(i1, i2));
        }
        return ret;
    }

    public byte[] calcDatasetItem(byte[][] cache, int i) {
        int n = cache.length;
        int r = params.getHASH_BYTES() / params.getWORD_BYTES();
        byte[] mix = cache[i % n].clone();

        setWord(mix, 0, i ^ getWord(mix, 0));
        mix = sha512(mix);
        for (int j = 0; j < params.getDATASET_PARENTS(); j++) {
            long cacheIdx = fnv(i ^ j, getWord(mix, j % r));
            mix = fnv(mix, cache[(int) (cacheIdx % n)]);
        }
        return sha512(mix);
    }

    public byte[][] calcDataset(long fullSize, byte[][] cache) {
        byte[][] ret = new byte[(int) (fullSize / params.getHASH_BYTES())][];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = calcDatasetItem(cache, i);
        }
        return ret;
    }

    public Pair<byte[], byte[]> hashimoto(byte[] blockHeaderTruncHash, byte[] nonce, long fullSize, DatasetLookup lookup) {
//        if (nonce.length != 4) throw new RuntimeException("nonce.length != 4");

        int w = params.getMIX_BYTES() / params.getWORD_BYTES();
        int mixhashes = params.getMIX_BYTES() / params.getHASH_BYTES();
        byte[] s = sha512(merge(blockHeaderTruncHash, reverse(nonce)));
        byte[] mix = new byte[params.getMIX_BYTES()];
        for (int i = 0; i < mixhashes; i++) {
            arraycopy(s, 0, mix, i * s.length, s.length);
        }

        int numFullPages = (int) (fullSize / params.getMIX_BYTES());
        for (int i = 0; i < params.getACCESSES(); i++) {
            long p = fnv(i ^ getWord(s, 0), getWord(mix, i % w)) % numFullPages;
            byte[] newData = new byte[params.getMIX_BYTES()];
            for (int j = 0; j < mixhashes; j++) {
                byte[] lookup1 = lookup.lookup((int) (p * mixhashes + j));
                arraycopy(lookup1, 0, newData, j * lookup1.length, lookup1.length);
            }
            mix = fnv(mix, newData);
        }

        byte[] cmix = new byte[mix.length / 4];
        for (int i = 0; i < mix.length / 4; i += 4 /* ? */) {
            long fnv1 = fnv(getWord(mix, i), getWord(mix, i + 1));
            long fnv2 = fnv(fnv1, getWord(mix, i + 2));
            long fnv3 = fnv(fnv2, getWord(mix, i + 3));
            setWord(cmix, i / 4, fnv3);
        }

        return Pair.of(cmix, sha3(merge(s, cmix)));
    }

    public Pair<byte[], byte[]> hashimotoLight(long fullSize, final byte[][] cache, byte[] blockHeaderTruncHash,
                                               byte[]  nonce) {
        return hashimoto(blockHeaderTruncHash, nonce, fullSize, new DatasetLookup() {
            @Override
            public byte[] lookup(int idx) {
                return calcDatasetItem(cache, idx);
            }
        });
    }

    public Pair<byte[], byte[]> hashimotoFull(long fullSize, final byte[][] dataset, byte[] blockHeaderTruncHash,
                                              byte[]  nonce) {
        return hashimoto(blockHeaderTruncHash, nonce, fullSize, new DatasetLookup() {
            @Override
            public byte[] lookup(int idx) {
                return dataset[idx];
            }
        });
    }

    public long mine(long fullSize, byte[][] dataset, byte[] blockHeaderTruncHash, long difficulty) {
        BigInteger target = valueOf(2).pow(256).divide(valueOf(difficulty));
        long nonce = new Random().nextLong();
        while(!Thread.currentThread().isInterrupted()) {
            nonce++;
            Pair<byte[], byte[]> pair = hashimotoFull(fullSize, dataset, blockHeaderTruncHash, longToBytes(nonce));
            BigInteger h = new BigInteger(1, pair.getRight() /* ?? */);
            if (h.compareTo(target) < 0) break;
        }
        return nonce;
    }

    /**
     * This the slower miner version which uses only cache thus taking much less memory than
     * regular {@link #mine} method
     */
    public long mineLight(long fullSize, final byte[][] cache, byte[] blockHeaderTruncHash, long difficulty) {
        BigInteger target = valueOf(2).pow(256).divide(valueOf(difficulty));
        long nonce = new Random().nextLong();
        while(!Thread.currentThread().isInterrupted()) {
            nonce++;
            Pair<byte[], byte[]> pair = hashimotoLight(fullSize, cache, blockHeaderTruncHash, longToBytes(nonce));
            BigInteger h = new BigInteger(1, pair.getRight() /* ?? */);
            if (h.compareTo(target) < 0) break;
        }
        return nonce;
    }

    public byte[] getSeedHash(long blockNumber) {
        byte[] ret = new byte[32];
        for (int i = 0; i < blockNumber / params.getEPOCH_LENGTH(); i++) {
            ret = sha3(ret);
        }
        return ret;
    }

    private interface DatasetLookup {
        byte[] lookup(int idx);
    }
}
