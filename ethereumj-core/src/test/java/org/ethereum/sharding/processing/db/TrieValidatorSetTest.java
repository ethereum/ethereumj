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

import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.sharding.domain.Validator;
import org.ethereum.util.FastByteComparisons;
import org.junit.Test;

import java.util.Random;

import static org.ethereum.crypto.HashUtil.randomHash;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Mikhail Kalinin
 * @since 05.09.2018
 */
public class TrieValidatorSetTest {

    @Test
    public void testBasics() {
        ValidatorSet set = emptySet();

        assertEquals(0, set.size());
        assertArrayEquals(ValidatorSet.EMPTY_HASH, set.getHash());

        Validator first = getRandomValidator();
        Validator second = getRandomValidator();

        int idx = set.add(first);
        assertEquals(0, idx);
        assertEquals(1, set.size());

        assert !FastByteComparisons.equal(ValidatorSet.EMPTY_HASH, set.getHash());

        assertValidatorEquals(first, set.get(0));
        assertValidatorEquals(first, set.getByPubKey(first.getPubKey()));

        idx = set.add(second);
        assertEquals(1, idx);
        assertEquals(2, set.size());

        assertValidatorEquals(first, set.get(0));
        assertValidatorEquals(first, set.getByPubKey(first.getPubKey()));
        assertValidatorEquals(second, set.get(1));
        assertValidatorEquals(second, set.getByPubKey(second.getPubKey()));

        assertNull(set.getByPubKey(randomHash()));
    }

    @Test
    public void testOutOfBounds() {
        ValidatorSet set = emptySet();

        try {
            set.get(0);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            set.put(0, getRandomValidator());
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        set.add(getRandomValidator());

        try {
            set.get(1);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            set.put(1, getRandomValidator());
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            set.get(100);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testValidatorUpdate() {
        ValidatorSet set = emptySet();

        Validator first = getRandomValidator();
        Validator second = getRandomValidator();

        set.add(first);
        set.add(second);

        Validator updatedFirst = getRandomValidator();
        Validator updatedSecond = getRandomValidator();

        set.put(0, updatedFirst);
        assertValidatorEquals(updatedFirst, set.get(0));
        assertValidatorEquals(second, set.get(1));

        set.put(1, updatedSecond);
        assertValidatorEquals(updatedFirst, set.get(0));
        assertValidatorEquals(updatedSecond, set.get(1));

        set.put(0, updatedSecond);
        set.put(1, updatedFirst);
        assertValidatorEquals(updatedSecond, set.get(0));
        assertValidatorEquals(updatedFirst, set.get(1));
    }

    @Test
    public void testSnapshotTo() {
        ValidatorSet set = emptySet();

        Validator first = getRandomValidator();
        Validator second = getRandomValidator();
        Validator third = getRandomValidator();

        set.add(first);
        set.add(second);

        byte[] revision1 = set.getHash();
        set.flush();

        Validator updatedSecond = getRandomValidator();
        set.put(1, updatedSecond);
        set.add(third);

        byte[] revision2 = set.getHash();
        set.flush();

        Validator updatedFirst = getRandomValidator();
        Validator forth = getRandomValidator();
        set.put(0, updatedFirst);
        set.add(forth);

        byte[] revision3 = set.getHash();
        set.flush();

        checkSnapshot(set.getSnapshotTo(revision1), first, second);
        checkSnapshot(set.getSnapshotTo(revision3), updatedFirst, updatedSecond, third, forth);
        checkSnapshot(set.getSnapshotTo(revision2), first, updatedSecond, third);
        checkSnapshot(set.getSnapshotTo(revision1), first, second);
        checkSnapshot(set.getSnapshotTo(revision2), first, updatedSecond, third);
        checkSnapshot(set.getSnapshotTo(revision3), updatedFirst, updatedSecond, third, forth);
    }

    void checkSnapshot(ValidatorSet set, Validator...validators) {
        assertEquals(validators.length, set.size());
        for (int i = 0; i < validators.length; i++) {
            assertValidatorEquals(validators[i], set.get(i));
        }
    }

    void assertValidatorEquals(Validator expected, Validator actual) {
        assertArrayEquals(expected.getPubKey(), actual.getPubKey());
        assertArrayEquals(expected.getWithdrawalAddress(), actual.getWithdrawalAddress());
        assertEquals(expected.getWithdrawalShard(), actual.getWithdrawalShard());
        assertArrayEquals(expected.getRandao(), actual.getRandao());
    }

    ValidatorSet emptySet() {
        return new TrieValidatorSet(new HashMapDB<>(), new HashMapDB<>());
    }

    Validator getRandomValidator() {
        long shardId = new Random().nextInt();
        shardId = (shardId < 0 ? (-shardId) : shardId) % 1024;
        return new Validator(randomHash(), shardId,
                HashUtil.sha3omit12(randomHash()), randomHash());
    }
}
