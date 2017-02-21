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
     * Flip the backing storage so the current state will be flushed
     * when call {@link #flushAsync()} and all the newer changes will
     * be collected to a new backing store and will be flushed only on
     * subsequent flush call
     *
     * The method is intended to make consistent flush from several
     * sources. I.e. at some point all the related Sources are flipped
     * synchronously first (this doesn't consume any time normally) and then
     * are flushed asynchronously
     *
     * This call may block until a previous flush is completed (if still in progress)
     *
     * @throws InterruptedException
     */
    void flipStorage() throws InterruptedException;

    /**
     * Does async flush, i.e. returns immediately while starts doing flush in a separate thread
     * This call may still block if the previous flush is not complete yet
     *
     * @return Future when the actual flush is complete
     */
    ListenableFuture<Boolean> flushAsync() throws InterruptedException;
}
