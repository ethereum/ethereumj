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

/**
 * Abstract Source implementation with underlying backing Source
 * The class has control whether the backing Source should be flushed
 * in 'cascade' manner
 *
 * Created by Anton Nashatyrev on 06.12.2016.
 */
public abstract class AbstractChainedSource<Key, Value, SourceKey, SourceValue> implements Source<Key, Value> {

    private Source<SourceKey, SourceValue> source;
    protected boolean flushSource;

    /**
     * Intended for subclasses which wishes to initialize the source
     * later via {@link #setSource(Source)} method
     */
    protected AbstractChainedSource() {
    }

    public AbstractChainedSource(Source<SourceKey, SourceValue> source) {
        this.source = source;
    }

    /**
     * Intended for subclasses which wishes to initialize the source later
     */
    protected void setSource(Source<SourceKey, SourceValue> src) {
        source = src;
    }

    public Source<SourceKey, SourceValue> getSource() {
        return source;
    }

    public void setFlushSource(boolean flushSource) {
        this.flushSource = flushSource;
    }

    /**
     * Invokes {@link #flushImpl()} and does backing Source flush if required
     * @return true if this or source flush did any changes
     */
    @Override
    public synchronized boolean flush() {
        boolean ret = flushImpl();
        if (flushSource) {
            ret |= getSource().flush();
        }
        return ret;
    }

    /**
     * Should be overridden to do actual source flush
     */
    protected abstract boolean flushImpl();
}
