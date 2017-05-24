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

import org.ethereum.core.BlockSummary;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutionSummary;
import org.ethereum.util.ByteUtil;

import java.util.Arrays;

/**
 * Calculates a 'reasonable' Gas price based on statistics of the latest transaction's Gas prices
 *
 * Normally the price returned should be sufficient to execute a transaction since ~25% of the latest
 * transactions were executed at this or lower price.
 *
 * Created by Anton Nashatyrev on 22.09.2015.
 */
public class GasPriceTracker extends EthereumListenerAdapter {

    private static final long defaultPrice = 70_000_000_000L;

    private long[] window = new long[512];
    private int idx = window.length - 1;
    private boolean filled = false;

    private long lastVal;

    @Override
    public void onBlock(BlockSummary blockSummary) {
        for (Transaction tx : blockSummary.getBlock().getTransactionsList()) {
            onTransaction(tx);
        }
    }

    public void onTransaction(Transaction tx) {
        if (idx == -1) {
            idx = window.length - 1;
            filled = true;
            lastVal = 0;  // recalculate only 'sometimes'
        }
        window[idx--] = ByteUtil.byteArrayToLong(tx.getGasPrice());
    }

    public long getGasPrice() {
        if (!filled) {
            return defaultPrice;
        } else {
            if (lastVal == 0) {
                long[] longs = Arrays.copyOf(window, window.length);
                Arrays.sort(longs);
                lastVal = longs[longs.length / 4];  // 25% percentile
            }
            return lastVal;
        }
    }
}
