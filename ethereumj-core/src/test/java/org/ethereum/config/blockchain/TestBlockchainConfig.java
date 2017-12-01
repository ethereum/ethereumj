/*
 * Copyright (c) [2017] [ <ether.camp> ]
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
 *
 *
 */

package org.ethereum.config.blockchain;

import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.Constants;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.db.BlockStore;
import org.ethereum.mine.MinerIfc;
import org.ethereum.validator.BlockHeaderValidator;

import java.math.BigInteger;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@SuppressWarnings("WeakerAccess")
class TestBlockchainConfig extends AbstractConfig {
    boolean eip161;
    boolean eip198;
    boolean eip212;
    boolean eip213;

    Constants constants = new Constants();
    MinerIfc minerIfc = new TestMinerIfc();
    BigInteger difficulty = BigInteger.valueOf(100);
    BigInteger difficultyMultiplier = BigInteger.valueOf(20);
    long transactionCost = 200L;
    boolean acceptTransactionSignature = true;
    String validateTransactionChanges = "test";
    byte[] extraData = new byte[]{};
    List<Pair<Long, BlockHeaderValidator>> headerValidators = newArrayList();

    TestBlockchainConfig() {
        this(false, false, false, false);
    }

    TestBlockchainConfig(boolean eip161, boolean eip198, boolean eip212, boolean eip213) {
        this.eip161 = eip161;
        this.eip198 = eip198;
        this.eip212 = eip212;
        this.eip213 = eip213;
    }

    @Override
    public Constants getConstants() {
        return constants;
    }

    @Override
    public MinerIfc getMineAlgorithm(SystemProperties config) {
        return minerIfc;
    }

    @Override
    public BigInteger calcDifficulty(BlockHeader curBlock, BlockHeader parent) {
        return difficulty;
    }

    @Override
    public BigInteger getCalcDifficultyMultiplier(BlockHeader curBlock, BlockHeader parent) {
        return difficultyMultiplier;
    }

    @Override
    public long getTransactionCost(Transaction tx) {
        return transactionCost;
    }

    @Override
    public boolean acceptTransactionSignature(Transaction tx) {
        return acceptTransactionSignature;
    }

    @Override
    public String validateTransactionChanges(BlockStore blockStore, Block curBlock, Transaction tx, Repository repository) {
        return validateTransactionChanges;
    }

    @Override
    public byte[] getExtraData(byte[] minerExtraData, long blockNumber) {
        return extraData;
    }

    @Override
    public List<Pair<Long, BlockHeaderValidator>> headerValidators() {
        return headerValidators;
    }

    @Override
    public boolean eip161() {
        return eip161;
    }

    @Override
    public boolean eip198() {
        return eip198;
    }

    @Override
    public boolean eip212() {
        return eip212;
    }

    @Override
    public boolean eip213() {
        return eip213;
    }

    public class TestMinerIfc implements MinerIfc {
        @Override
        public ListenableFuture<MiningResult> mine(Block block) {
            return null;
        }

        @Override
        public boolean validate(BlockHeader blockHeader) {
            return false;
        }
    }
}
