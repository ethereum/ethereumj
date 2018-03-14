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
package org.ethereum.db;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.crypto.HashUtil;

import java.math.BigInteger;

import java.util.List;

/**
 * @author Roman Mandeleil
 * @since 10.02.2015
 */
public class BlockStoreDummy implements BlockStore {

    @Override
    public byte[] getBlockHashByNumber(long blockNumber) {

        byte[] data = String.valueOf(blockNumber).getBytes();
        return HashUtil.sha3(data);
    }

    @Override
    public byte[] getBlockHashByNumber(long blockNumber, byte[] branchBlockHash) {
        return getBlockHashByNumber(blockNumber);
    }

    @Override
    public Block getChainBlockByNumber(long blockNumber) {
        return null;
    }

    @Override
    public Block getBlockByHash(byte[] hash) {
        return null;
    }

    @Override
    public boolean isBlockExist(byte[] hash) {
        return false;
    }

    @Override
    public List<byte[]> getListHashesEndWith(byte[] hash, long qty) {
        return null;
    }

    @Override
    public List<BlockHeader> getListHeadersEndWith(byte[] hash, long qty) {
        return null;
    }

    @Override
    public List<Block> getListBlocksEndWith(byte[] hash, long qty) {
        return null;
    }

    @Override
    public void saveBlock(Block block, BigInteger cummDifficulty, boolean mainChain) {

    }


    @Override
    public BigInteger getTotalDifficulty() {
        return null;
    }

    @Override
    public Block getBestBlock() {
        return null;
    }


    @Override
    public void flush() {
    }

    @Override
    public void load() {
    }

    @Override
    public long getMaxNumber() {
        return 0;
    }


    @Override
    public void reBranch(Block forkBlock) {

    }

    @Override
    public BigInteger getTotalDifficultyForHash(byte[] hash) {
        return null;
    }

    @Override
    public void close() {}
}
