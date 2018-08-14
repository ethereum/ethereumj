package org.ethereum.publish.event;

import org.ethereum.core.TransactionExecutionSummary;

public class TransactionExecuted extends Event<TransactionExecutionSummary> {
    public TransactionExecuted(TransactionExecutionSummary executionSummary) {
        super(executionSummary);
    }
}
