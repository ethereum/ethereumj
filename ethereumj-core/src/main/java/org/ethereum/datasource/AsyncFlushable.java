package org.ethereum.datasource;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Future;

/**
 * Represents cache Source which is able to do asynchronous flush
 *
 * Created by Anton Nashatyrev on 02.02.2017.
 */
public interface AsyncFlushable {

    /**
     * Does async flush, i.e. returns immediately while starts doing flush in a separate thread
     * This call may still block if the previous flush is not complete yet
     *
     * @return Future when the actual flush is complete
     */
    ListenableFuture<Boolean> flushAsync() throws InterruptedException;
}
