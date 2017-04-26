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
