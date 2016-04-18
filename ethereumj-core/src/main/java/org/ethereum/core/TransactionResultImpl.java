package org.ethereum.core;

import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.Future;

/**
 * Created by Anton Nashatyrev on 14.04.2016.
 */
public class TransactionResultImpl implements TransactionResult {
    Transaction transaction;
    TransactionInfo txInfo;

    @Override
    public Transaction getTransaction() {
        return transaction;
    }

    @Override
    public TransactionInfo getLatestResult() {
        return txInfo;
    }

    @Override
    public State getLatestState() {
        return null;
    }

    @Override
    public int getConfirmationBlocks() {
        return 0;
    }

    @Override
    public void addListener(Listener listener) {

    }

    @Override
    public void removeListener(Listener listener) {

    }

    public void updateResult(TransactionInfo txInfo) {

    }

    public void onNewBlock(Block block) {

    }
}
