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

import org.ethereum.datasource.NoDeleteSource;
import org.ethereum.datasource.ObjectDataSource;
import org.ethereum.datasource.Serializer;
import org.ethereum.datasource.Source;
import org.ethereum.datasource.SourceCodec;
import org.ethereum.sharding.domain.Validator;
import org.ethereum.trie.Trie;
import org.ethereum.trie.TrieImpl;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.spongycastle.util.encoders.Hex;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

/**
 * Validator set implementation based on {@link Trie} structure.
 *
 * <p>
 *     Utilizes {@link Trie#getRootHash()} and {@link Trie#setRoot(byte[])} methods
 *     that makes it straightforward to implement {@link #getHash()} and {@link #getSnapshotTo(byte[])} methods.
 *
 * <p>
 *     A path in the trie is calculated from validator index that is 24-bit integer
 *     by taking its big-endian byte representation.
 *     According to the spec, max validator index is {@code 2**22 - 1}.
 *
 * <p>
 *     Size of validator set is kept in the same trie that is used to store validator items.
 *     Size item has a special path in the trie: {@code 0xffffff} which is higher than max index, thus, safe to be used.
 *
 * <p>
 *     {@link #getByPubKey(byte[])} method logic is supported with help of index {@code [pubKey: index]}.
 *
 * @author Mikhail Kalinin
 * @since 04.09.2018
 */
public class TrieValidatorSet implements ValidatorSet {

    private static final byte[] SIZE_KEY = Hex.decode("ffffff");

    private Source<byte[], byte[]> underlyingSrc;
    private Source<byte[], byte[]> indexSrc;
    private Source<byte[], byte[]> trieSrc;
    private Trie<byte[]> trie;
    private Source<Integer, Validator> validators;
    private Source<byte[], Integer> index;
    private int size;

    public TrieValidatorSet(Source<byte[], byte[]> src, Source<byte[], byte[]> indexSrc) {
        this(src, indexSrc, EMPTY_HASH);
    }

    public TrieValidatorSet(Source<byte[], byte[]> src, Source<byte[], byte[]> indexSrc, byte[] root) {
        this.underlyingSrc = src;
        this.indexSrc = indexSrc;
        // trie deletes ghost nodes by default, force keeping them in the source
        this.trieSrc = new NoDeleteSource<>(underlyingSrc);
        this.trie = new TrieImpl(trieSrc, root == EMPTY_HASH ? null : root);
        this.validators = new SourceCodec<>(trie, IndexSerializer, Validator.Serializer);

        // load size
        byte[] encodedSize = trie.get(SIZE_KEY);
        this.size = encodedSize == null ? 0 : RLP.decodeInt(encodedSize, 0);

        // index
        this.index = new ObjectDataSource<>(this.indexSrc, IndexSerializer, 0);
    }

    @Override
    public synchronized Validator get(Integer index) {
        rangeCheck(index);
        return validators.get(index);
    }

    @Nullable
    @Override
    public Validator getByPubKey(byte[] pubKey) {
        Integer idx = index.get(pubKey);
        return idx == null ? null : get(idx);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public synchronized int add(Validator validator) {
        int newIndex = size;

        validator = validator.withIndex(newIndex);
        validators.put(validator.getIndex(), validator);
        index.put(validator.getPubKey(), validator.getIndex());
        setSize(validator.getIndex() + 1);
        return validator.getIndex();
    }

    @Override
    public synchronized void put(Integer index, Validator validator) {
        rangeCheck(index);
        validators.put(index, validator);
    }

    @Override
    public synchronized byte[] getHash() {
        return trie.getRootHash();
    }

    @Override
    public synchronized boolean flush() {
        return trie.flush() | trieSrc.flush();
    }

    @Override
    public ValidatorSet getSnapshotTo(byte[] hash) {
        return new TrieValidatorSet(underlyingSrc, indexSrc, hash);
    }

    @Override
    public int[] getActiveIndices() {
        return IntStream.range(0, size).toArray();
    }

    void setSize(int size) {
        this.size = size;
        this.trie.put(SIZE_KEY, RLP.encodeInt(size));
    }

    static final Serializer<Integer, byte[]> IndexSerializer = new Serializer<Integer, byte[]>() {
        @Override
        public byte[] serialize(Integer index) {
            // MAX_VALIDATOR_COUNT = 2 ** 22,
            // drop highest byte due to its uselessness
            return ByteUtil.toInt24(index);
        }

        @Override
        public Integer deserialize(byte[] stream) {
            return stream == null ? null : ByteUtil.byteArrayToInt(stream);
        }
    };

    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException("Validator with idx=" + index + " doesn't exist");
    }
}
