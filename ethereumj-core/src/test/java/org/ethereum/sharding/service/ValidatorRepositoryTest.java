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
package org.ethereum.sharding.service;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.sharding.ShardingTestHelper;
import org.ethereum.sharding.domain.Validator;
import org.ethereum.util.ByteUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Mikhail Kalinin
 * @since 28.07.2018
 */
@Ignore
public class ValidatorRepositoryTest {

    @Test
    public void testQuery() {
        byte[] val1 = Hex.decode("1000000000000000000000000000000000000000000000000000000000000000");
        byte[] val2 = Hex.decode("2000000000000000000000000000000000000000000000000000000000000000");
        byte[] val3 = Hex.decode("3000000000000000000000000000000000000000000000000000000000000000");
        byte[] val4 = Hex.decode("4000000000000000000000000000000000000000000000000000000000000000");

        long shard = 0;
        byte[] withdrawalAddress = Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826");
        byte[] randao = new byte[32];

        ShardingTestHelper.ShardingBootstrap bootstrap = ShardingTestHelper.bootstrap();

        // register val1
        Transaction tx = bootstrap.depositContract.depositTx(val1, shard, withdrawalAddress, randao, bootstrap.depositAuthority);
        bootstrap.standaloneBlockchain.submitTransaction(tx);
        Block b1 = bootstrap.standaloneBlockchain.createBlock();

        // no validators registered
        Block b2 = bootstrap.standaloneBlockchain.createBlock();

        // two validators in block
        tx = bootstrap.depositContract.depositTx(val2, shard, withdrawalAddress, randao, bootstrap.depositAuthority);
        bootstrap.standaloneBlockchain.submitTransaction(tx);

        tx = bootstrap.depositContract.depositTx(val3, shard, withdrawalAddress, randao, bootstrap.depositAuthority);
        // increase nonce
        tx = new Transaction(ByteUtil.bytesToBigInteger(tx.getNonce()).add(BigInteger.ONE).toByteArray(),
                tx.getGasPrice(), tx.getGasLimit(), tx.getReceiveAddress(), tx.getValue(), tx.getData());
        bootstrap.depositAuthority.sign(tx);
        bootstrap.standaloneBlockchain.submitTransaction(tx);

        Block b3 = bootstrap.standaloneBlockchain.createBlock();

        // register val4
        tx = bootstrap.depositContract.depositTx(val4, shard, withdrawalAddress, randao, bootstrap.depositAuthority);
        bootstrap.standaloneBlockchain.submitTransaction(tx);
        Block b4 = bootstrap.standaloneBlockchain.createBlock();

        // query all
        List<Validator> res = bootstrap.validatorRepository.query(b1.getHash(), b4.getHash());
        checkQueryResult(res, new byte[][]{ val1, val2, val3, val4 });

        // query from second and third block
        res = bootstrap.validatorRepository.query(b2.getHash(), b4.getHash());
        checkQueryResult(res, new byte[][]{ val2, val3, val4 });

        res = bootstrap.validatorRepository.query(b3.getHash(), b4.getHash());
        checkQueryResult(res, new byte[][]{ val2, val3, val4 });

        // query from single block
        res = bootstrap.validatorRepository.query(b3.getHash(), b3.getHash());
        checkQueryResult(res, new byte[][]{ val2, val3 });

        // check upper block bound
        res = bootstrap.validatorRepository.query(b1.getHash(), b2.getHash());
        checkQueryResult(res, new byte[][]{ val1 });

        // check empty result
        res = bootstrap.validatorRepository.query(b2.getHash(), b2.getHash());
        checkQueryResult(res, new byte[][]{});
    }

    private void checkQueryResult(List<Validator> res, byte[][] pubKeys) {
        assertEquals(pubKeys.length, res.size());

        List<byte[]> resPubKeys = res.stream().map(Validator::getPubKey).collect(Collectors.toList());
        byte[][] arr = resPubKeys.toArray(new byte[resPubKeys.size()][]);

        for (int i = 0; i < pubKeys.length; i++) {
            assertArrayEquals(pubKeys[i], arr[i]);
        }
    }
}
