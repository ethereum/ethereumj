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
package org.ethereum.sharding.domain;

import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.sharding.processing.consensus.GenesisTransition;
import org.ethereum.sharding.processing.db.TrieValidatorSet;
import org.ethereum.sharding.processing.db.ValidatorSet;
import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.sharding.service.ValidatorRepository;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.ethereum.crypto.HashUtil.randomHash;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Mikhail Kalinin
 * @since 05.09.2018
 */
public class GenesisTransitionTest {

    @Test
    public void testInitialValidatorSet() {

        Validator v1 = getRandomValidator();
        Validator v2 = getRandomValidator();
        Validator v3 = getRandomValidator();
        Validator v4 = getRandomValidator();

        BeaconGenesis genesis = new BeaconGenesis(getJson(v1, v3, v4));

        ValidatorSet validatorSet = new TrieValidatorSet(new HashMapDB<>());
        ValidatorRepository validatorRepository = new PredefinedValidatorRepository(v1, v2, v3, v4);

        GenesisTransition transition = new GenesisTransition(validatorRepository);
        transition.applyBlock(genesis, BeaconState.empty().withValidatorSet(validatorSet));

        checkValidatorSet(validatorSet, v1, v3, v4);
    }

    BeaconGenesis.Json getJson(Validator... validators) {
        BeaconGenesis.Json json = new BeaconGenesis.Json();
        json.mainChainRef = Hex.toHexString(randomHash());
        json.parentHash = Hex.toHexString(randomHash());
        json.randaoReveal = Hex.toHexString(randomHash());
        json.timestamp = System.currentTimeMillis() / 1000;
        json.validatorSet = new String[validators.length];

        for (int i = 0; i < validators.length; i++)
            json.validatorSet[i] = Hex.toHexString(validators[i].getPubKey());

        return json;
    }

    static class PredefinedValidatorRepository implements ValidatorRepository {

        List<Validator> validators = new ArrayList<>();

        public PredefinedValidatorRepository(Validator... validators) {
            this.validators = Arrays.asList(validators);
        }

        @Override
        public List<Validator> query(byte[] fromBlock, byte[] toBlock) {
            return validators;
        }

        @Override
        public List<Validator> query(byte[] toBlock) {
            return validators;
        }
    }

    void checkValidatorSet(ValidatorSet set, Validator... validators) {
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

    Validator getRandomValidator() {
        long shardId = new Random().nextInt();
        shardId = (shardId < 0 ? (-shardId) : shardId) % 1024;
        return new Validator(randomHash(), shardId,
                HashUtil.sha3omit12(randomHash()), randomHash());
    }
}
