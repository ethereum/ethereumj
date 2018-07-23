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
package org.ethereum.listener;

import org.ethereum.core.Block;
import org.ethereum.core.BlockSummary;
import org.ethereum.core.Transaction;
import org.ethereum.util.ByteUtil;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
 * Calculates a 'reasonable' Gas price based on statistics of the latest transaction's Gas prices.
 * This is an updated version that returns more accurate data
 * in networks with large number of transactions like Ethereum MainNet.
 * However it needs more CPU and memory resources for processing.
 *
 * Normally the price returned should be sufficient to execute a transaction since ~25%
 * (if {@link #getPercentileShare()} is not overridden) of the latest transactions were
 * executed at this or lower price.
 */
public class RecommendedGasPriceTracker extends EthereumListenerAdapter {

    private static final Long DEFAULT_PRICE = null;
    private static final int MIN_BLOCKS = 128;
    private static final int BLOCKS_RECOUNT = 1;
    private static final int MIN_TRANSACTIONS = 512;
    private static final int PERCENTILE_SHARE = 4;

    private LinkedList<long[]> blockGasPrices = new LinkedList<>();

    private int idx = 0;
    private Long recommendedGasPrice = getDefaultPrice();

    @Override
    public void onBlock(BlockSummary blockSummary) {
        onBlock(blockSummary.getBlock());
    }

    protected void onBlock(Block block) {
        if (onTransactions(block.getTransactionsList())) {
            ++idx;
            if (idx >= getBlocksRecount()) {
                Long newGasPrice = getGasPrice();
                if (newGasPrice != null) {
                    this.recommendedGasPrice = newGasPrice;
                }
                idx = 0;
            }
        }
    }

    private synchronized boolean onTransactions(List<Transaction> txs) {
        if (txs.isEmpty()) return false;

        long[] gasPrices = new long[txs.size()];
        for (int i = 0; i < txs.size(); ++i) {
            gasPrices[i] = ByteUtil.byteArrayToLong(txs.get(i).getGasPrice());
        }

        blockGasPrices.add(gasPrices);
        while (blockGasPrices.size() > getMinBlocks() &&
                (calcGasPricesSize() - blockGasPrices.getFirst().length) >= getMinTransactions()) {
            blockGasPrices.removeFirst();
        }
        return true;
    }

    private int calcGasPricesSize() {
        return blockGasPrices.stream().map(Array::getLength).mapToInt(Integer::intValue).sum();
    }

    private synchronized Long getGasPrice() {
        int size = calcGasPricesSize();
        // Don't override default value until we have minTransactions and minBlocks
        if (size < getMinTransactions() ||
                blockGasPrices.size() < getMinBlocks()) return null;

        long[] difficulties = new long[size];
        int index = 0;
        for (long[] currentBlock : blockGasPrices) {
            for (long currentDifficulty : currentBlock) {
                difficulties[index] = currentDifficulty;
                ++index;
            }
        }
        Arrays.sort(difficulties);

        return difficulties[difficulties.length/getPercentileShare()];
    }

    /**
     * Returns recommended gas price calculated with class settings
     * when enough data is gathered.
     * Until this {@link #getDefaultPrice()} is returned
     * @return recommended gas price for transaction
     */
    public Long getRecommendedGasPrice() {
        return recommendedGasPrice;
    }

    /**
     * Override to set your value
     *
     * Minimum number of blocks used for recommended gas price calculation
     * If minimum number of blocks includes less than {@link #getMinTransactions()} in total,
     * data for blocks before last {@link #getMinBlocks()} is used when available
     * @return minimum number of blocks
     */
    public int getMinBlocks() {
        return MIN_BLOCKS;
    }

    /**
     * Override to set your value
     *
     * Used when not enough data gathered
     * @return default transaction price
     */
    public Long getDefaultPrice() {
        return DEFAULT_PRICE;
    }

    /**
     * Override to set your value
     *
     * Recount every N blocks
     * @return number of blocks
     */
    public int getBlocksRecount() {
        return BLOCKS_RECOUNT;
    }

    /**
     * Override to set your value
     *
     * Required number of gasPrice data from transactions
     * to override default value on recount
     * @return minimum number of transactions for calculation
     */
    public int getMinTransactions() {
        return MIN_TRANSACTIONS;
    }

    /**
     * Override to set your value
     *
     * Defines lowest part share for difficulties slice
     * So 4 means lowest 25%, 8 lowest 12.5% etc
     * @return percentile share
     */
    public int getPercentileShare() {
        return PERCENTILE_SHARE;
    }
}
