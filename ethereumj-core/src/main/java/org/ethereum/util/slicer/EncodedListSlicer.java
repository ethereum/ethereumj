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
package org.ethereum.util.slicer;

import org.ethereum.core.Encoded;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.ethereum.datasource.MemSizeEstimator.ByteArrayEstimator;

/**
 * Utility method for slicing list or iterator by memory usage
 * Accepts one object in excess of configured max memory, but no more
 * @param <T> type of objects list/iterator made of, should implement {@link Encoded}
 */
public class EncodedListSlicer<T extends Encoded> {

    private List<T> entities = new ArrayList<>();
    private int maxSize;
    private Consumer<T> purgeConsumer;
    private Function<T, Long> memSizeEstimator = t -> ByteArrayEstimator.estimateSize(t.getEncoded()) + 16;
    private int sizeSum = 80;  // ArrayList skeleton

    /**
     * Create slicer with following parameters
     * Objects should be added later with {@link #add(Encoded)}
     * @param maxSize         Maximum size, result list could exceed it by no more than 1 element
     * @param purgeConsumer   Purge consumer, executed before object is added to the result
     */
    public EncodedListSlicer(int maxSize, Consumer<T> purgeConsumer) {
        this.maxSize = maxSize;
        this.purgeConsumer = purgeConsumer;
    }

    /**
     * Create slicer with following parameters
     * @param entities        Objects list, the result will be sliced from it
     * @param maxSize         Maximum size, result list could exceed it by no more than 1 element
     * @param purgeConsumer   Purge consumer, executed before object is added to the result
     */
    public EncodedListSlicer(List<T> entities, int maxSize, Consumer<T> purgeConsumer) {
        this.maxSize = maxSize;
        this.purgeConsumer = purgeConsumer;
        for (T entity : entities) {
            if (!add(entity)) break;
        }
    }

    /**
     * Create slicer with following parameters
     * @param entitiesIterator  Objects iterator, it will not be pulled after maxSize is filled
     * @param maxSize           Maximum size, result list could exceed it by no more than 1 element
     * @param purgeConsumer     Purge consumer, executed before object is added to the result
     */
    public EncodedListSlicer(Iterator<T> entitiesIterator, int maxSize, Consumer<T> purgeConsumer) {
        this.maxSize = maxSize;
        this.purgeConsumer = purgeConsumer;
        while (entitiesIterator.hasNext()) {
            T entity = entitiesIterator.next();
            if (!add(entity)) break;
        }
    }

    /**
     * Use custom object memory size estimator
     * @param memSizeEstimator  input: object, output: memory size
     * @return estimated memory usage by object
     */
    public EncodedListSlicer<T> withMemSizeEstimator(Function<T, Long> memSizeEstimator) {
        this.memSizeEstimator = memSizeEstimator;
        return this;
    }

    /**
     * Add entity to the slicer
     * @param entity new object
     * @return true if you could add more, otherwise false
     */
    public boolean add(T entity) {
        if (sizeSum >= maxSize) return false;
        purgeConsumer.accept(entity);
        sizeSum += memSizeEstimator.apply(entity);
        this.entities.add(entity);
        return sizeSum < maxSize;
    }

    /**
     * Get the result
     * @return sliced list
     */
    public List<T> getEntities() {
        return entities;
    }
}
