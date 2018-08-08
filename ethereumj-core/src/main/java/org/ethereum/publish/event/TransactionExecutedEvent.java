package org.ethereum.publish.event;

import org.ethereum.core.TransactionExecutionSummary;

public class TransactionExecutedEvent extends Event<TransactionExecutionSummary> {
    public TransactionExecutedEvent(TransactionExecutionSummary executionSummary) {
        super(executionSummary);
    }
}
