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
package org.ethereum.core;

import java.math.BigInteger;
import java.util.List;

public interface Blockchain {

    long getSize();

    BlockSummary add(Block block);

    ImportResult tryToConnect(Block block);

    void storeBlock(Block block, List<TransactionReceipt> receipts);

    Block getBlockByNumber(long blockNr);

    void setBestBlock(Block block);

    Block getBestBlock();

    boolean hasParentOnTheChain(Block block);

    void close();

    void updateTotalDifficulty(Block block);

    BigInteger getTotalDifficulty();

    void setTotalDifficulty(BigInteger totalDifficulty);

    byte[] getBestBlockHash();

    List<byte[]> getListOfHashesStartFrom(byte[] hash, int qty);

    List<byte[]> getListOfHashesStartFromBlock(long blockNumber, int qty);

    /**
     * Returns the transaction info stored in the blockchain
     * This doesn't involve pending transactions
     * If transaction was included to more than one block (from different forks)
     * the method returns TransactionInfo from the block on the main chain.
     */
    TransactionInfo getTransactionInfo(byte[] hash);

    Block getBlockByHash(byte[] hash);

    List<Chain> getAltChains();

    List<Block> getGarbage();

    void setExitOn(long exitOn);

    byte[] getMinerCoinbase();

    boolean isBlockExist(byte[] hash);

    List<BlockHeader> getListOfHeadersStartFrom(BlockIdentifier identifier, int skip, int limit, boolean reverse);

    List<byte[]> getListOfBodiesByHashes(List<byte[]> hashes);

    Block createNewBlock(Block parent, List<Transaction> transactions, List<BlockHeader> uncles);
}
