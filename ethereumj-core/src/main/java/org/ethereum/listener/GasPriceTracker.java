package org.ethereum.listener;

import org.ethereum.core.TransactionExecutionSummary;

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

    private long[] window = new long[512];
    private int idx = window.length - 1;
    private boolean filled = false;
    private long defaultPrice = 70_000_000_000L;

    private long lastVal;

    @Override
    public void onTransactionExecuted(TransactionExecutionSummary summary) {
        if (idx == 0) {
            idx = window.length - 1;
            filled = true;
            lastVal = 0;  // recalculate only 'sometimes'
        }
        window[idx--] = summary.getGasPrice().longValue();
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
