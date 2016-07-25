package org.ethereum.core;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class BlockSummary {

    private final Block block;
    private final Map<byte[], BigInteger> rewards;
    private final List<TransactionReceipt> receipts;
    private final List<TransactionExecutionSummary> summaries;

    public BlockSummary(Block block, Map<byte[], BigInteger> rewards, List<TransactionReceipt> receipts, List<TransactionExecutionSummary> summaries) {
        this.block = block;
        this.rewards = rewards;
        this.receipts = receipts;
        this.summaries = summaries;
    }

    public Block getBlock() {
        return block;
    }

    public List<TransactionReceipt> getReceipts() {
        return receipts;
    }

    public List<TransactionExecutionSummary> getSummaries() {
        return summaries;
    }

    /**
     * All the mining rewards paid out for this block, including the main block rewards, uncle rewards, and transaction fees.
     */
    public Map<byte[], BigInteger> getRewards() {
        return rewards;
    }
}
