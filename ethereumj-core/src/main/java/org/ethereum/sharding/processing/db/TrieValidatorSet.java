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
import org.ethereum.datasource.Serializer;
import org.ethereum.datasource.Source;
import org.ethereum.datasource.SourceCodec;
import org.ethereum.sharding.domain.Validator;
import org.ethereum.trie.Trie;
import org.ethereum.trie.TrieImpl;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;

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
 * @author Mikhail Kalinin
 * @since 04.09.2018
 */
public class TrieValidatorSet implements ValidatorSet {

    private static final byte[] SIZE_KEY = Hex.decode("ffffff");

    private Source<byte[], byte[]> underlyingSrc;
    private Source<byte[], byte[]> trieSrc;
    private Trie<byte[]> trie;
    private Source<Integer, Validator> validators;
    private int size;

    public TrieValidatorSet(Source<byte[], byte[]> src) {
        this(src, EMPTY_HASH);
    }

    public TrieValidatorSet(Source<byte[], byte[]> src, byte[] root) {
        this.underlyingSrc = src;
        // trie deletes ghost nodes by default, force keeping them in the source
        this.trieSrc = new NoDeleteSource<>(underlyingSrc);
        this.trie = new TrieImpl(trieSrc, root == EMPTY_HASH ? null : root);
        this.validators = new SourceCodec<>(trie, IndexSerializer, Validator.Serializer);

        // load size
        byte[] encodedSize = trie.get(SIZE_KEY);
        this.size = encodedSize == null ? 0 : RLP.decodeInt(encodedSize, 0);
    }

    @Override
    public synchronized Validator get(Integer index) {
        rangeCheck(index);
        return validators.get(index);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public synchronized int add(Validator validator) {
        int newIndex = size;
        validators.put(newIndex, validator);
        setSize(newIndex + 1);
        return newIndex;
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
        return new TrieValidatorSet(underlyingSrc, hash);
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
