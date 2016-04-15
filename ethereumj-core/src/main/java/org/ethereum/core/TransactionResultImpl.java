package org.ethereum.core;

import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.Future;

/**
 * Created by Anton Nashatyrev on 14.04.2016.
 */
public class TransactionResultImpl implements TransactionResult {
    Transaction transaction;
    TransactionInfo txInfo;

    SettableFuture<TransactionInfo> nextResultFuture;
    SettableFuture<TransactionInfo> nextBlockResultFuture;
    SettableFuture<TransactionInfo> confirmedResultFuture;


    @Override
    public Transaction getTransaction() {
        return transaction;
    }

    @Override
    public TransactionInfo getLatestResult() {
        return txInfo;
    }

    @Override
    public Future<TransactionInfo> getNextResult() {
        if (nextResultFuture == null) {
            if (txInfo != null) {
                nextResultFuture = SettableFuture.create();
                nextResultFuture.set(txInfo);
            } else {
                nextResultFuture = SettableFuture.create();
            }
        } else {
            if (nextResultFuture.isDone()) {
                nextResultFuture = SettableFuture.create();
            }
        }
        return nextResultFuture;
    }

    @Override
    public Future<TransactionInfo> getNextBlockResult() {
        if (nextBlockResultFuture == null || nextBlockResultFuture.isDone()) {
            nextBlockResultFuture = SettableFuture.create();
        }
        return nextBlockResultFuture;
    }

    @Override
    public Future<TransactionInfo> getConfirmedResult(int confirmationBlocksCount) {
        if (confirmedResultFuture == null || confirmedResultFuture.isDone()) {
            confirmedResultFuture = SettableFuture.create();
        }
        return confirmedResultFuture;
    }

    public void updateResult(TransactionInfo txInfo) {

    }

    public void onNewBlock(Block block) {

    }
}
