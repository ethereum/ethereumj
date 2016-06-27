package org.ethereum.core;

import java.math.BigInteger;
import java.util.List;

public class BlockSummary {

    private final Block block;
    private final List<TransactionReceipt> txReceipts;
    private final List<TransactionExecutionSummary> txSummaries;

    public BlockSummary(Block block, List<TransactionReceipt> txReceipts, List<TransactionExecutionSummary> txSummaries) {
        this.block = block;
        this.txReceipts = txReceipts;
        this.txSummaries = txSummaries;
    }

    public Block getBlock() {
        return block;
    }

    public List<TransactionReceipt> getTxReceipts() {
        return txReceipts;
    }

    public List<TransactionExecutionSummary> getTxSummaries() {
        return txSummaries;
    }

    /**
     * The total of all transaction fees paid in this block - does not include mining or uncle inclusion rewards.
     */
    public BigInteger getTransactionFees() {
        BigInteger total = BigInteger.ZERO;
        for (TransactionExecutionSummary summary : txSummaries) {
            total = total.add(summary.getFee());
        }
        return total;
    }
}
